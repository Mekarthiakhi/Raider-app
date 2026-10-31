-- ============================================================================
-- 003_views_and_functions.sql
-- Fleet Ride Tracker — Views & Functions
--
-- Summary views for dashboards and charts, plus server-side functions
-- for device health checks and payout reconciliation.
-- ============================================================================


-- ============================================================================
-- VIEW 1: v_daily_summary
-- Aggregated ride metrics per vehicle, driver, platform, and calendar date.
-- Used for daily breakdown tables and detailed reporting.
-- ============================================================================
CREATE OR REPLACE VIEW public.v_daily_summary AS
SELECT
    r.vehicle_id,
    r.driver_id,
    r.platform,
    (r.accepted_at AT TIME ZONE 'Asia/Kolkata')::date  AS ride_date,
    count(*)                                             AS ride_count,
    coalesce(sum(r.fare), 0)                             AS total_fare,
    coalesce(sum(r.distance_km), 0)                      AS total_km,
    coalesce(sum(r.duration_min), 0)                      AS total_duration,
    CASE
        WHEN coalesce(sum(r.distance_km), 0) > 0
        THEN round(sum(r.fare) / sum(r.distance_km), 2)
        ELSE 0
    END                                                  AS avg_fare_per_km,
    count(*) FILTER (WHERE r.status = 'cancelled')       AS cancelled_count,
    count(*) FILTER (WHERE r.needs_review = true)        AS review_count
FROM public.rides r
GROUP BY
    r.vehicle_id,
    r.driver_id,
    r.platform,
    (r.accepted_at AT TIME ZONE 'Asia/Kolkata')::date;

COMMENT ON VIEW public.v_daily_summary IS
    'Daily aggregated ride metrics grouped by vehicle, driver, platform, and date (IST).';


-- ============================================================================
-- VIEW 2: v_weekly_summary
-- Same metrics grouped by ISO week.
-- ============================================================================
CREATE OR REPLACE VIEW public.v_weekly_summary AS
SELECT
    r.vehicle_id,
    r.driver_id,
    r.platform,
    date_trunc('week', r.accepted_at AT TIME ZONE 'Asia/Kolkata')::date  AS week_start,
    count(*)                                             AS ride_count,
    coalesce(sum(r.fare), 0)                             AS total_fare,
    coalesce(sum(r.distance_km), 0)                      AS total_km,
    coalesce(sum(r.duration_min), 0)                      AS total_duration,
    CASE
        WHEN coalesce(sum(r.distance_km), 0) > 0
        THEN round(sum(r.fare) / sum(r.distance_km), 2)
        ELSE 0
    END                                                  AS avg_fare_per_km,
    count(*) FILTER (WHERE r.status = 'cancelled')       AS cancelled_count,
    count(*) FILTER (WHERE r.needs_review = true)        AS review_count
FROM public.rides r
GROUP BY
    r.vehicle_id,
    r.driver_id,
    r.platform,
    date_trunc('week', r.accepted_at AT TIME ZONE 'Asia/Kolkata');

COMMENT ON VIEW public.v_weekly_summary IS
    'Weekly aggregated ride metrics grouped by vehicle, driver, platform, and ISO week start (IST).';


-- ============================================================================
-- VIEW 3: v_monthly_summary
-- Same metrics grouped by calendar month.
-- ============================================================================
CREATE OR REPLACE VIEW public.v_monthly_summary AS
SELECT
    r.vehicle_id,
    r.driver_id,
    r.platform,
    date_trunc('month', r.accepted_at AT TIME ZONE 'Asia/Kolkata')::date  AS month_start,
    count(*)                                             AS ride_count,
    coalesce(sum(r.fare), 0)                             AS total_fare,
    coalesce(sum(r.distance_km), 0)                      AS total_km,
    coalesce(sum(r.duration_min), 0)                      AS total_duration,
    CASE
        WHEN coalesce(sum(r.distance_km), 0) > 0
        THEN round(sum(r.fare) / sum(r.distance_km), 2)
        ELSE 0
    END                                                  AS avg_fare_per_km,
    count(*) FILTER (WHERE r.status = 'cancelled')       AS cancelled_count,
    count(*) FILTER (WHERE r.needs_review = true)        AS review_count
FROM public.rides r
GROUP BY
    r.vehicle_id,
    r.driver_id,
    r.platform,
    date_trunc('month', r.accepted_at AT TIME ZONE 'Asia/Kolkata');

COMMENT ON VIEW public.v_monthly_summary IS
    'Monthly aggregated ride metrics grouped by vehicle, driver, platform, and month start (IST).';


-- ============================================================================
-- VIEW 4: v_fleet_today
-- Real-time dashboard view: each vehicle's status, driver, today's metrics,
-- device health, and current ride status.
-- ============================================================================
CREATE OR REPLACE VIEW public.v_fleet_today AS
SELECT
    v.id                                     AS vehicle_id,
    v.fleet_id,
    v.reg_no,
    v.nickname,
    d_driver.name                            AS driver_name,
    -- Today's ride metrics (IST timezone)
    coalesce(today.rides_today, 0)           AS rides_today,
    coalesce(today.fare_today, 0)            AS fare_today,
    coalesce(today.km_today, 0)              AS km_today,
    -- Device health
    dev.is_online,
    dev.last_seen,
    dev.battery_level,
    -- Current status: last ride's status or 'idle'
    coalesce(latest_ride.status, 'idle')     AS current_status
FROM public.vehicles v
-- Join to driver via device (device links vehicle ↔ driver)
LEFT JOIN public.devices dev ON dev.vehicle_id = v.id
LEFT JOIN public.drivers d_driver ON d_driver.id = dev.driver_id
-- Today's aggregated metrics
LEFT JOIN LATERAL (
    SELECT
        count(*)              AS rides_today,
        coalesce(sum(fare), 0) AS fare_today,
        coalesce(sum(distance_km), 0) AS km_today
    FROM public.rides
    WHERE vehicle_id = v.id
      AND (accepted_at AT TIME ZONE 'Asia/Kolkata')::date = (now() AT TIME ZONE 'Asia/Kolkata')::date
) today ON true
-- Latest ride for current_status
LEFT JOIN LATERAL (
    SELECT status
    FROM public.rides
    WHERE vehicle_id = v.id
    ORDER BY accepted_at DESC
    LIMIT 1
) latest_ride ON true;

COMMENT ON VIEW public.v_fleet_today IS
    'Live dashboard view: vehicle, driver, today''s ride count/fare/km, device health, and current ride status.';


-- ============================================================================
-- VIEW 5: v_vehicle_daily_trend
-- Last 30 days of daily ride_count, total_fare, total_km per vehicle.
-- Used for sparkline charts on the dashboard.
-- ============================================================================
CREATE OR REPLACE VIEW public.v_vehicle_daily_trend AS
SELECT
    r.vehicle_id,
    (r.accepted_at AT TIME ZONE 'Asia/Kolkata')::date  AS ride_date,
    count(*)                                             AS ride_count,
    coalesce(sum(r.fare), 0)                             AS total_fare,
    coalesce(sum(r.distance_km), 0)                      AS total_km
FROM public.rides r
WHERE r.accepted_at >= (now() - interval '30 days')
GROUP BY
    r.vehicle_id,
    (r.accepted_at AT TIME ZONE 'Asia/Kolkata')::date
ORDER BY
    r.vehicle_id,
    ride_date;

COMMENT ON VIEW public.v_vehicle_daily_trend IS
    'Last 30 days of daily ride metrics per vehicle. Used for sparkline charts.';


-- ============================================================================
-- GRANT SELECT on views to authenticated (fleet owners)
-- ============================================================================
GRANT SELECT ON public.v_daily_summary       TO authenticated;
GRANT SELECT ON public.v_weekly_summary      TO authenticated;
GRANT SELECT ON public.v_monthly_summary     TO authenticated;
GRANT SELECT ON public.v_fleet_today         TO authenticated;
GRANT SELECT ON public.v_vehicle_daily_trend TO authenticated;


-- ============================================================================
-- FUNCTION 1: fn_check_device_online()
--
-- Called by a Supabase cron job or Edge Function every 5 minutes.
-- Marks devices offline if last_seen > 15 minutes ago, and creates
-- an alert for each device that transitions from online → offline.
--
-- Returns the number of devices marked offline.
-- ============================================================================
CREATE OR REPLACE FUNCTION public.fn_check_device_online()
RETURNS int
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    stale_device RECORD;
    marked_count int := 0;
BEGIN
    -- Find devices that are currently online but haven't been seen in 15+ minutes
    FOR stale_device IN
        SELECT
            dev.id          AS device_id,
            dev.vehicle_id,
            v.fleet_id,
            v.reg_no
        FROM public.devices dev
        JOIN public.vehicles v ON v.id = dev.vehicle_id
        WHERE dev.is_online = true
          AND dev.last_seen < (now() - interval '15 minutes')
    LOOP
        -- Mark the device offline
        UPDATE public.devices
        SET    is_online = false
        WHERE  id = stale_device.device_id;

        -- Create an alert for the fleet owner
        INSERT INTO public.alerts (fleet_id, vehicle_id, alert_type, message)
        VALUES (
            stale_device.fleet_id,
            stale_device.vehicle_id,
            'device_offline',
            format(
                'Device in vehicle %s went offline (last seen > 15 minutes ago).',
                stale_device.reg_no
            )
        );

        marked_count := marked_count + 1;
    END LOOP;

    RETURN marked_count;
END;
$$;

COMMENT ON FUNCTION public.fn_check_device_online() IS
    'Cron-callable function: marks stale devices offline and creates alerts. Returns count of devices marked offline.';


-- ============================================================================
-- FUNCTION 2: fn_reconciliation_gap()
--
-- Trigger function that automatically computes tracked_fare_total and
-- reconciliation_gap whenever a payout record is inserted or updated.
--
-- tracked_fare_total = SUM(fare) from rides for the same vehicle + platform
--                      within the payout period.
-- reconciliation_gap = statement_amount − tracked_fare_total
-- ============================================================================
CREATE OR REPLACE FUNCTION public.fn_reconciliation_gap()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    computed_total numeric(12,2);
BEGIN
    -- Sum all completed ride fares for this vehicle/platform in the payout period
    SELECT coalesce(sum(r.fare), 0)
    INTO   computed_total
    FROM   public.rides r
    WHERE  r.vehicle_id = NEW.vehicle_id
      AND  r.platform   = NEW.platform
      AND  r.status     = 'completed'
      AND  (r.accepted_at AT TIME ZONE 'Asia/Kolkata')::date >= NEW.period_start
      AND  (r.accepted_at AT TIME ZONE 'Asia/Kolkata')::date <= NEW.period_end;

    -- Set the computed columns
    NEW.tracked_fare_total  := computed_total;
    NEW.reconciliation_gap  := coalesce(NEW.statement_amount, 0) - computed_total;

    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION public.fn_reconciliation_gap() IS
    'Trigger function: auto-computes tracked_fare_total and reconciliation_gap on payout INSERT/UPDATE.';


-- Attach the trigger to the payouts table
CREATE TRIGGER trg_reconciliation_gap
    BEFORE INSERT OR UPDATE ON public.payouts
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_reconciliation_gap();

COMMENT ON TRIGGER trg_reconciliation_gap ON public.payouts IS
    'Auto-computes fare totals and reconciliation gap before every payout insert/update.';


-- ============================================================================
-- BONUS FUNCTION: fn_upsert_ride()
--
-- Idempotent ride upsert using ride_key as the dedup key.
-- Devices call this instead of raw INSERT to avoid constraint violations.
-- ============================================================================
CREATE OR REPLACE FUNCTION public.fn_upsert_ride(
    p_vehicle_id    uuid,
    p_driver_id     uuid,
    p_device_id     uuid,
    p_platform      text,
    p_ride_key      text,
    p_status        text,
    p_accepted_at   timestamptz,
    p_completed_at  timestamptz    DEFAULT NULL,
    p_fare          numeric(10,2)  DEFAULT NULL,
    p_distance_km   numeric(8,2)   DEFAULT NULL,
    p_duration_min  numeric(8,2)   DEFAULT NULL,
    p_pickup        text           DEFAULT NULL,
    p_dropoff       text           DEFAULT NULL,
    p_confidence    numeric(3,2)   DEFAULT NULL,
    p_needs_review  boolean        DEFAULT false,
    p_raw_extracted jsonb          DEFAULT NULL
)
RETURNS uuid
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    result_id uuid;
BEGIN
    INSERT INTO public.rides (
        vehicle_id, driver_id, device_id, platform, ride_key, status,
        accepted_at, completed_at, fare, distance_km, duration_min,
        pickup, dropoff, confidence, needs_review, raw_extracted
    ) VALUES (
        p_vehicle_id, p_driver_id, p_device_id, p_platform, p_ride_key, p_status,
        p_accepted_at, p_completed_at, p_fare, p_distance_km, p_duration_min,
        p_pickup, p_dropoff, p_confidence, p_needs_review, p_raw_extracted
    )
    ON CONFLICT (ride_key) DO UPDATE SET
        status        = EXCLUDED.status,
        completed_at  = coalesce(EXCLUDED.completed_at, rides.completed_at),
        fare          = coalesce(EXCLUDED.fare, rides.fare),
        distance_km   = coalesce(EXCLUDED.distance_km, rides.distance_km),
        duration_min  = coalesce(EXCLUDED.duration_min, rides.duration_min),
        pickup        = coalesce(EXCLUDED.pickup, rides.pickup),
        dropoff       = coalesce(EXCLUDED.dropoff, rides.dropoff),
        confidence    = coalesce(EXCLUDED.confidence, rides.confidence),
        needs_review  = EXCLUDED.needs_review,
        raw_extracted = coalesce(EXCLUDED.raw_extracted, rides.raw_extracted),
        synced_at     = now()
    RETURNING id INTO result_id;

    RETURN result_id;
END;
$$;

COMMENT ON FUNCTION public.fn_upsert_ride IS
    'Idempotent ride upsert using ride_key. Returns the ride id. Safe to call multiple times.';

-- Grant execute to both roles (RLS on rides still applies for direct queries)
GRANT EXECUTE ON FUNCTION public.fn_upsert_ride TO authenticated, anon;
GRANT EXECUTE ON FUNCTION public.fn_check_device_online TO authenticated;
