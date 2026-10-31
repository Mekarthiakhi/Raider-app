-- ============================================================================
-- 002_rls_policies.sql
-- Fleet Ride Tracker — Row Level Security
--
-- Two actor types:
--   1. Fleet Owner  (authenticated role, auth.uid() = fleets.owner_id)
--   2. Device/Phone (anon role, identified by device_token in JWT claim)
--
-- Helper functions resolve the current user's fleet or device context.
-- ============================================================================


-- ============================================================================
-- HELPER FUNCTIONS
-- ============================================================================

-- get_fleet_id_for_user()
-- Returns the fleet id owned by the currently authenticated user.
-- Used by every RLS policy that gates on fleet ownership.
CREATE OR REPLACE FUNCTION public.get_fleet_id_for_user()
RETURNS uuid
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT id
    FROM   public.fleets
    WHERE  owner_id = auth.uid()
    LIMIT  1;
$$;

COMMENT ON FUNCTION public.get_fleet_id_for_user() IS
    'Returns the fleet id owned by auth.uid(). Used by RLS policies.';


-- get_device_vehicle_id()
-- Returns the vehicle_id associated with the device_token found in the JWT.
-- Devices authenticate with a Supabase anon key + a custom JWT claim
-- "device_token" set during device registration / edge-function auth.
CREATE OR REPLACE FUNCTION public.get_device_vehicle_id()
RETURNS uuid
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT vehicle_id
    FROM   public.devices
    WHERE  device_token = (
        -- Extract the device_token from the JWT's app_metadata or raw claims
        coalesce(
            current_setting('request.jwt.claims', true)::jsonb ->> 'device_token',
            current_setting('request.jwt.claims', true)::jsonb -> 'app_metadata' ->> 'device_token'
        )
    )
    LIMIT 1;
$$;

COMMENT ON FUNCTION public.get_device_vehicle_id() IS
    'Returns the vehicle_id for the current device, identified by JWT claim "device_token".';


-- get_device_id_from_token()
-- Returns the device id for the current device_token in the JWT.
CREATE OR REPLACE FUNCTION public.get_device_id_from_token()
RETURNS uuid
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT id
    FROM   public.devices
    WHERE  device_token = (
        coalesce(
            current_setting('request.jwt.claims', true)::jsonb ->> 'device_token',
            current_setting('request.jwt.claims', true)::jsonb -> 'app_metadata' ->> 'device_token'
        )
    )
    LIMIT 1;
$$;

COMMENT ON FUNCTION public.get_device_id_from_token() IS
    'Returns the device id for the current device_token in the JWT.';


-- ============================================================================
-- ENABLE RLS ON ALL TABLES
-- ============================================================================
ALTER TABLE public.fleets            ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.vehicles          ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.drivers           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.devices           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rides             ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ride_events       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payouts           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.remote_selectors  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.alerts            ENABLE ROW LEVEL SECURITY;


-- ============================================================================
-- FLEETS — Only the owner can manage their own fleet
-- ============================================================================

CREATE POLICY "fleet_owner_select" ON public.fleets
    FOR SELECT TO authenticated
    USING (owner_id = auth.uid());

CREATE POLICY "fleet_owner_insert" ON public.fleets
    FOR INSERT TO authenticated
    WITH CHECK (owner_id = auth.uid());

CREATE POLICY "fleet_owner_update" ON public.fleets
    FOR UPDATE TO authenticated
    USING (owner_id = auth.uid())
    WITH CHECK (owner_id = auth.uid());

CREATE POLICY "fleet_owner_delete" ON public.fleets
    FOR DELETE TO authenticated
    USING (owner_id = auth.uid());


-- ============================================================================
-- VEHICLES — Fleet owner manages vehicles in their fleet
-- ============================================================================

CREATE POLICY "vehicles_owner_select" ON public.vehicles
    FOR SELECT TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "vehicles_owner_insert" ON public.vehicles
    FOR INSERT TO authenticated
    WITH CHECK (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "vehicles_owner_update" ON public.vehicles
    FOR UPDATE TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user())
    WITH CHECK (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "vehicles_owner_delete" ON public.vehicles
    FOR DELETE TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user());


-- ============================================================================
-- DRIVERS — Fleet owner manages drivers
-- ============================================================================

CREATE POLICY "drivers_owner_select" ON public.drivers
    FOR SELECT TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "drivers_owner_insert" ON public.drivers
    FOR INSERT TO authenticated
    WITH CHECK (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "drivers_owner_update" ON public.drivers
    FOR UPDATE TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user())
    WITH CHECK (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "drivers_owner_delete" ON public.drivers
    FOR DELETE TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user());


-- ============================================================================
-- DEVICES — Fleet owner full CRUD; devices can update their own record (heartbeat)
-- ============================================================================

CREATE POLICY "devices_owner_select" ON public.devices
    FOR SELECT TO authenticated
    USING (
        vehicle_id IN (
            SELECT id FROM public.vehicles WHERE fleet_id = public.get_fleet_id_for_user()
        )
    );

CREATE POLICY "devices_owner_insert" ON public.devices
    FOR INSERT TO authenticated
    WITH CHECK (
        vehicle_id IN (
            SELECT id FROM public.vehicles WHERE fleet_id = public.get_fleet_id_for_user()
        )
    );

CREATE POLICY "devices_owner_update" ON public.devices
    FOR UPDATE TO authenticated
    USING (
        vehicle_id IN (
            SELECT id FROM public.vehicles WHERE fleet_id = public.get_fleet_id_for_user()
        )
    )
    WITH CHECK (
        vehicle_id IN (
            SELECT id FROM public.vehicles WHERE fleet_id = public.get_fleet_id_for_user()
        )
    );

CREATE POLICY "devices_owner_delete" ON public.devices
    FOR DELETE TO authenticated
    USING (
        vehicle_id IN (
            SELECT id FROM public.vehicles WHERE fleet_id = public.get_fleet_id_for_user()
        )
    );

-- Device heartbeat: anon devices can SELECT and UPDATE their own device record
CREATE POLICY "devices_self_select" ON public.devices
    FOR SELECT TO anon
    USING (id = public.get_device_id_from_token());

CREATE POLICY "devices_self_update" ON public.devices
    FOR UPDATE TO anon
    USING (id = public.get_device_id_from_token())
    WITH CHECK (id = public.get_device_id_from_token());


-- ============================================================================
-- RIDES — Fleet owner full CRUD; devices can INSERT for their own vehicle
-- ============================================================================

CREATE POLICY "rides_owner_select" ON public.rides
    FOR SELECT TO authenticated
    USING (
        vehicle_id IN (
            SELECT id FROM public.vehicles WHERE fleet_id = public.get_fleet_id_for_user()
        )
    );

CREATE POLICY "rides_owner_insert" ON public.rides
    FOR INSERT TO authenticated
    WITH CHECK (
        vehicle_id IN (
            SELECT id FROM public.vehicles WHERE fleet_id = public.get_fleet_id_for_user()
        )
    );

CREATE POLICY "rides_owner_update" ON public.rides
    FOR UPDATE TO authenticated
    USING (
        vehicle_id IN (
            SELECT id FROM public.vehicles WHERE fleet_id = public.get_fleet_id_for_user()
        )
    )
    WITH CHECK (
        vehicle_id IN (
            SELECT id FROM public.vehicles WHERE fleet_id = public.get_fleet_id_for_user()
        )
    );

CREATE POLICY "rides_owner_delete" ON public.rides
    FOR DELETE TO authenticated
    USING (
        vehicle_id IN (
            SELECT id FROM public.vehicles WHERE fleet_id = public.get_fleet_id_for_user()
        )
    );

-- Device can INSERT rides only for its own vehicle
CREATE POLICY "rides_device_insert" ON public.rides
    FOR INSERT TO anon
    WITH CHECK (vehicle_id = public.get_device_vehicle_id());

-- Device can SELECT its own rides (needed for dedup checks on ride_key)
CREATE POLICY "rides_device_select" ON public.rides
    FOR SELECT TO anon
    USING (vehicle_id = public.get_device_vehicle_id());


-- ============================================================================
-- RIDE_EVENTS — Fleet owner full CRUD; devices can INSERT for their vehicle's rides
-- ============================================================================

CREATE POLICY "ride_events_owner_select" ON public.ride_events
    FOR SELECT TO authenticated
    USING (
        ride_id IN (
            SELECT r.id FROM public.rides r
            JOIN public.vehicles v ON r.vehicle_id = v.id
            WHERE v.fleet_id = public.get_fleet_id_for_user()
        )
    );

CREATE POLICY "ride_events_owner_insert" ON public.ride_events
    FOR INSERT TO authenticated
    WITH CHECK (
        ride_id IN (
            SELECT r.id FROM public.rides r
            JOIN public.vehicles v ON r.vehicle_id = v.id
            WHERE v.fleet_id = public.get_fleet_id_for_user()
        )
    );

CREATE POLICY "ride_events_owner_update" ON public.ride_events
    FOR UPDATE TO authenticated
    USING (
        ride_id IN (
            SELECT r.id FROM public.rides r
            JOIN public.vehicles v ON r.vehicle_id = v.id
            WHERE v.fleet_id = public.get_fleet_id_for_user()
        )
    )
    WITH CHECK (
        ride_id IN (
            SELECT r.id FROM public.rides r
            JOIN public.vehicles v ON r.vehicle_id = v.id
            WHERE v.fleet_id = public.get_fleet_id_for_user()
        )
    );

CREATE POLICY "ride_events_owner_delete" ON public.ride_events
    FOR DELETE TO authenticated
    USING (
        ride_id IN (
            SELECT r.id FROM public.rides r
            JOIN public.vehicles v ON r.vehicle_id = v.id
            WHERE v.fleet_id = public.get_fleet_id_for_user()
        )
    );

-- Device can INSERT ride_events for rides belonging to its own vehicle
CREATE POLICY "ride_events_device_insert" ON public.ride_events
    FOR INSERT TO anon
    WITH CHECK (
        ride_id IN (
            SELECT id FROM public.rides
            WHERE vehicle_id = public.get_device_vehicle_id()
        )
    );

-- Device can SELECT ride_events for its own vehicle's rides
CREATE POLICY "ride_events_device_select" ON public.ride_events
    FOR SELECT TO anon
    USING (
        ride_id IN (
            SELECT id FROM public.rides
            WHERE vehicle_id = public.get_device_vehicle_id()
        )
    );


-- ============================================================================
-- PAYOUTS — Fleet owner only
-- ============================================================================

CREATE POLICY "payouts_owner_select" ON public.payouts
    FOR SELECT TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "payouts_owner_insert" ON public.payouts
    FOR INSERT TO authenticated
    WITH CHECK (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "payouts_owner_update" ON public.payouts
    FOR UPDATE TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user())
    WITH CHECK (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "payouts_owner_delete" ON public.payouts
    FOR DELETE TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user());


-- ============================================================================
-- REPORTS — Fleet owner only
-- ============================================================================

CREATE POLICY "reports_owner_select" ON public.reports
    FOR SELECT TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "reports_owner_insert" ON public.reports
    FOR INSERT TO authenticated
    WITH CHECK (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "reports_owner_update" ON public.reports
    FOR UPDATE TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user())
    WITH CHECK (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "reports_owner_delete" ON public.reports
    FOR DELETE TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user());


-- ============================================================================
-- REMOTE_SELECTORS — Fleet owner full CRUD; all devices can SELECT (read-only)
-- ============================================================================

CREATE POLICY "selectors_owner_all" ON public.remote_selectors
    FOR ALL TO authenticated
    USING (true)
    WITH CHECK (true);

-- All devices (anon) can read active selectors — they need these to parse screens
CREATE POLICY "selectors_device_select" ON public.remote_selectors
    FOR SELECT TO anon
    USING (active = true);


-- ============================================================================
-- ALERTS — Fleet owner only
-- ============================================================================

CREATE POLICY "alerts_owner_select" ON public.alerts
    FOR SELECT TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "alerts_owner_insert" ON public.alerts
    FOR INSERT TO authenticated
    WITH CHECK (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "alerts_owner_update" ON public.alerts
    FOR UPDATE TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user())
    WITH CHECK (fleet_id = public.get_fleet_id_for_user());

CREATE POLICY "alerts_owner_delete" ON public.alerts
    FOR DELETE TO authenticated
    USING (fleet_id = public.get_fleet_id_for_user());
