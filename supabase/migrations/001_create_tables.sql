-- ============================================================================
-- 001_create_tables.sql
-- Fleet Ride Tracker — Core Schema
-- 
-- Creates all tables, constraints, and indexes for tracking rides, earnings,
-- and kilometres across a fleet of vehicles using Rapido, Uber, and Ola.
-- ============================================================================

-- Enable pgcrypto for gen_random_uuid() (Supabase enables this by default,
-- but we include it for standalone environments)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";


-- ============================================================================
-- 1. fleets — One record per fleet owner (multi-tenant root)
-- ============================================================================
CREATE TABLE public.fleets (
    id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id    uuid        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name        text        NOT NULL,
    created_at  timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE  public.fleets IS 'Top-level tenant. Each fleet belongs to one Supabase auth user.';
COMMENT ON COLUMN public.fleets.owner_id IS 'References auth.users — the fleet owner who manages vehicles and drivers.';


-- ============================================================================
-- 2. vehicles — Cars in the fleet
-- ============================================================================
CREATE TABLE public.vehicles (
    id          uuid    PRIMARY KEY DEFAULT gen_random_uuid(),
    fleet_id    uuid    NOT NULL REFERENCES public.fleets(id) ON DELETE CASCADE,
    reg_no      text    NOT NULL UNIQUE,
    nickname    text,
    status      text    NOT NULL DEFAULT 'active'
                        CHECK (status IN ('active', 'inactive', 'maintenance')),
    created_at  timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE  public.vehicles IS 'Each vehicle in the fleet. reg_no is the licence plate and must be unique.';
COMMENT ON COLUMN public.vehicles.status IS 'active = on road, inactive = parked/off, maintenance = in workshop.';


-- ============================================================================
-- 3. drivers — People who drive the fleet vehicles
-- ============================================================================
CREATE TABLE public.drivers (
    id                  uuid    PRIMARY KEY DEFAULT gen_random_uuid(),
    fleet_id            uuid    NOT NULL REFERENCES public.fleets(id) ON DELETE CASCADE,
    name                text    NOT NULL,
    phone               text    NOT NULL,
    consent_at          timestamptz,
    consent_revoked_at  timestamptz,
    created_at          timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE  public.drivers IS 'Fleet drivers. consent_at tracks when the driver agreed to data collection.';
COMMENT ON COLUMN public.drivers.consent_revoked_at IS 'Set when a driver revokes accessibility-service consent.';


-- ============================================================================
-- 4. devices — Android phones in vehicles running the tracker app
-- ============================================================================
CREATE TABLE public.devices (
    id              uuid    PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id      uuid    REFERENCES public.vehicles(id) ON DELETE SET NULL,
    driver_id       uuid    REFERENCES public.drivers(id) ON DELETE SET NULL,
    device_token    text    UNIQUE NOT NULL,
    device_model    text,
    android_version text,
    app_version     text,
    last_seen       timestamptz,
    battery_level   int,
    a11y_enabled    boolean NOT NULL DEFAULT false,
    is_online       boolean NOT NULL DEFAULT false,
    created_at      timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE  public.devices IS 'Android devices in vehicles. device_token is the unique JWT-subject or FCM token.';
COMMENT ON COLUMN public.devices.a11y_enabled IS 'Whether the accessibility service is currently enabled on the device.';
COMMENT ON COLUMN public.devices.is_online IS 'Set to false by fn_check_device_online() when last_seen > 15 min ago.';


-- ============================================================================
-- 5. rides — Every ride captured from Rapido / Uber / Ola
-- ============================================================================
CREATE TABLE public.rides (
    id              uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id      uuid        NOT NULL REFERENCES public.vehicles(id) ON DELETE CASCADE,
    driver_id       uuid        NOT NULL REFERENCES public.drivers(id) ON DELETE CASCADE,
    device_id       uuid        REFERENCES public.devices(id) ON DELETE SET NULL,
    platform        text        NOT NULL
                                CHECK (platform IN ('rapido', 'uber', 'ola')),
    ride_key        text        UNIQUE NOT NULL,
    status          text        NOT NULL
                                CHECK (status IN ('accepted', 'in_trip', 'completed', 'cancelled', 'orphaned')),
    accepted_at     timestamptz NOT NULL,
    completed_at    timestamptz,
    fare            numeric(10,2),
    distance_km     numeric(8,2),
    duration_min    numeric(8,2),
    pickup          text,
    dropoff         text,
    confidence      numeric(3,2)
                    CHECK (confidence >= 0 AND confidence <= 1),
    needs_review    boolean     NOT NULL DEFAULT false,
    review_note     text,
    reviewed_at     timestamptz,
    raw_extracted   jsonb,
    synced_at       timestamptz NOT NULL DEFAULT now(),
    created_at      timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE  public.rides IS 'Core ride table. One row per ride. ride_key ensures idempotent upserts from devices.';
COMMENT ON COLUMN public.rides.ride_key IS 'Idempotent dedup key: e.g. "rapido:AP12AB1234:2024-06-15T09:31". Prevents duplicate inserts.';
COMMENT ON COLUMN public.rides.confidence IS 'ML/extraction confidence score 0.00–1.00. Low confidence → needs_review=true.';
COMMENT ON COLUMN public.rides.raw_extracted IS 'Raw JSON blob captured from the accessibility event / screen scrape.';


-- ============================================================================
-- 6. ride_events — Granular event log for each ride lifecycle
-- ============================================================================
CREATE TABLE public.ride_events (
    id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    ride_id     uuid        NOT NULL REFERENCES public.rides(id) ON DELETE CASCADE,
    event_type  text        NOT NULL,
    occurred_at timestamptz NOT NULL,
    raw_json    jsonb,
    created_at  timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE  public.ride_events IS 'Append-only event log: offer_shown, accepted, pickup_started, trip_started, completed, cancelled.';
COMMENT ON COLUMN public.ride_events.event_type IS 'Free-form event name: offer_shown | accepted | pickup_started | trip_started | completed | cancelled.';


-- ============================================================================
-- 7. payouts — Platform payout reconciliation records
-- ============================================================================
CREATE TABLE public.payouts (
    id                    uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    fleet_id              uuid        NOT NULL REFERENCES public.fleets(id) ON DELETE CASCADE,
    vehicle_id            uuid        NOT NULL REFERENCES public.vehicles(id) ON DELETE CASCADE,
    platform              text        NOT NULL
                                      CHECK (platform IN ('rapido', 'uber', 'ola')),
    period_start          date        NOT NULL,
    period_end            date        NOT NULL,
    statement_amount      numeric(12,2),
    tracked_fare_total    numeric(12,2),
    reconciliation_gap    numeric(12,2),
    notes                 text,
    created_at            timestamptz NOT NULL DEFAULT now(),

    -- Prevent duplicate payout records for the same vehicle/platform/period
    CONSTRAINT payouts_unique_period UNIQUE (vehicle_id, platform, period_start, period_end)
);

COMMENT ON TABLE  public.payouts IS 'Reconciliation: compare platform statement_amount vs our tracked_fare_total.';
COMMENT ON COLUMN public.payouts.reconciliation_gap IS 'statement_amount − tracked_fare_total. Positive = platform paid more than tracked.';


-- ============================================================================
-- 8. reports — Generated report metadata (daily/weekly/monthly)
-- ============================================================================
CREATE TABLE public.reports (
    id              uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    fleet_id        uuid        NOT NULL REFERENCES public.fleets(id) ON DELETE CASCADE,
    kind            text        NOT NULL
                                CHECK (kind IN ('daily', 'weekly', 'monthly')),
    period_start    date        NOT NULL,
    period_end      date        NOT NULL,
    file_url        text,
    generated_at    timestamptz NOT NULL DEFAULT now(),
    metadata        jsonb
);

COMMENT ON TABLE  public.reports IS 'Metadata for generated PDF/CSV reports stored in Supabase Storage.';
COMMENT ON COLUMN public.reports.file_url IS 'Signed URL or Storage path to the generated report file.';


-- ============================================================================
-- 9. remote_selectors — OTA selector configs pushed to devices
-- ============================================================================
CREATE TABLE public.remote_selectors (
    id          uuid    PRIMARY KEY DEFAULT gen_random_uuid(),
    platform    text    NOT NULL
                        CHECK (platform IN ('rapido', 'uber', 'ola')),
    app_version text,
    screen_type text    NOT NULL
                        CHECK (screen_type IN ('offer', 'accepted', 'in_trip', 'completed', 'cancelled')),
    selectors   jsonb   NOT NULL,
    priority    int     NOT NULL DEFAULT 0,
    active      boolean NOT NULL DEFAULT true,
    updated_at  timestamptz NOT NULL DEFAULT now(),
    created_at  timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE  public.remote_selectors IS 'Remote selector configs so devices can parse ride screens without an app update.';
COMMENT ON COLUMN public.remote_selectors.selectors IS 'JSON: {viewIds:[], patterns:[], keywords:{en:[], hi:[], te:[]}}';
COMMENT ON COLUMN public.remote_selectors.screen_type IS 'Which screen this selector set applies to: offer, accepted, in_trip, completed, cancelled.';
COMMENT ON COLUMN public.remote_selectors.priority IS 'Higher priority selectors are tried first. Allows A/B testing selector strategies.';


-- ============================================================================
-- 10. alerts — Fleet-level and vehicle-level alerts
-- ============================================================================
CREATE TABLE public.alerts (
    id          uuid    PRIMARY KEY DEFAULT gen_random_uuid(),
    fleet_id    uuid    NOT NULL REFERENCES public.fleets(id) ON DELETE CASCADE,
    vehicle_id  uuid    REFERENCES public.vehicles(id) ON DELETE SET NULL,
    alert_type  text    NOT NULL,
    message     text    NOT NULL,
    is_read     boolean NOT NULL DEFAULT false,
    created_at  timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE  public.alerts IS 'Alerts for fleet owners: device_offline, low_battery, high_cancel_rate, reconciliation_mismatch.';
COMMENT ON COLUMN public.alerts.alert_type IS 'Alert category: device_offline | low_battery | high_cancel_rate | reconciliation_mismatch | consent_revoked.';


-- ============================================================================
-- INDEXES
-- ============================================================================

-- rides: primary query patterns
CREATE INDEX idx_rides_vehicle_accepted  ON public.rides (vehicle_id, accepted_at DESC);
CREATE INDEX idx_rides_driver            ON public.rides (driver_id);
CREATE INDEX idx_rides_platform          ON public.rides (platform);
CREATE INDEX idx_rides_needs_review      ON public.rides (needs_review) WHERE needs_review = true;
CREATE INDEX idx_rides_ride_key          ON public.rides (ride_key);

-- devices: lookup by vehicle and staleness checks
CREATE INDEX idx_devices_vehicle         ON public.devices (vehicle_id);
CREATE INDEX idx_devices_last_seen       ON public.devices (last_seen);

-- alerts: unread alerts per fleet
CREATE INDEX idx_alerts_fleet_unread     ON public.alerts (fleet_id, is_read) WHERE is_read = false;

-- ride_events: lookup by ride
CREATE INDEX idx_ride_events_ride        ON public.ride_events (ride_id);

-- payouts: lookup by fleet
CREATE INDEX idx_payouts_fleet           ON public.payouts (fleet_id);

-- vehicles/drivers: lookup by fleet (for JOINs in views)
CREATE INDEX idx_vehicles_fleet          ON public.vehicles (fleet_id);
CREATE INDEX idx_drivers_fleet           ON public.drivers (fleet_id);


-- ============================================================================
-- GRANTS — Allow authenticated and anon roles to interact via RLS
-- ============================================================================

-- Authenticated users (fleet owners) get full access gated by RLS policies
GRANT SELECT, INSERT, UPDATE, DELETE ON public.fleets            TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.vehicles          TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.drivers           TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.devices           TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.rides             TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.ride_events       TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.payouts           TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.reports           TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.remote_selectors  TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.alerts            TO authenticated;

-- Anon role (device tokens using apikey only) — limited access via RLS
GRANT SELECT, INSERT              ON public.rides              TO anon;
GRANT SELECT, INSERT              ON public.ride_events        TO anon;
GRANT SELECT, UPDATE              ON public.devices            TO anon;
GRANT SELECT                      ON public.remote_selectors   TO anon;

-- Grant usage on the public schema
GRANT USAGE ON SCHEMA public TO anon, authenticated;
