-- ============================================================================
-- 005_svm_fleet_office_register.sql
-- SVM FLEET — Moosapet, Hyderabad (500018)
-- Office Paper Register & Driver Commission Base
-- Contacts: 9391915013, 9959859347
-- ============================================================================

CREATE TABLE IF NOT EXISTS public.office_duty_logs (
    id                  uuid            PRIMARY KEY DEFAULT gen_random_uuid(),
    fleet_id            text            NOT NULL DEFAULT 'svm_fleet_hyderabad',
    shift_type          text            NOT NULL DEFAULT '12_HOURS'
                                        CHECK (shift_type IN ('12_HOURS', '24_HOURS')),

    -- 1. Office Register Timestamps
    login_date          text            NOT NULL,  -- e.g. '01/11/2026'
    login_time          text            NOT NULL,  -- e.g. '06:00 AM'
    logout_date         text            NOT NULL,  -- e.g. '01/11/2026'
    logout_time         text            NOT NULL,  -- e.g. '08:15 PM'

    -- 2. Vehicle & Driver Identification
    vehicle_no          text            NOT NULL,  -- e.g. 'TS09 UB 3368'
    driver_name         text            NOT NULL,  -- e.g. 'Suman'
    driver_phone        text,

    -- 3. Operations & Revenue
    trips               int             NOT NULL DEFAULT 0,
    earnings            numeric(10, 2)  NOT NULL DEFAULT 0.00,
    cash_collection     numeric(10, 2)  NOT NULL DEFAULT 0.00,
    expenditure         numeric(10, 2)  NOT NULL DEFAULT 0.00,
    commission_rent     numeric(10, 2)  NOT NULL DEFAULT 0.00,
    drive_pass          numeric(10, 2)  NOT NULL DEFAULT 0.00,
    advance             numeric(10, 2)  NOT NULL DEFAULT 0.00,

    -- 4. Payouts & Settlement
    payment_cash        numeric(10, 2)  NOT NULL DEFAULT 0.00,
    payment_online      numeric(10, 2)  NOT NULL DEFAULT 0.00,
    net_balance         numeric(10, 2)  NOT NULL DEFAULT 0.00,
    status              text            NOT NULL DEFAULT 'SETTLED'
                                        CHECK (status IN ('OPEN', 'SETTLED', 'PENDING_CASH')),
    remarks             text,

    created_at          timestamptz     NOT NULL DEFAULT now()
);

-- Indexes for lightning fast queries
CREATE INDEX IF NOT EXISTS idx_duty_logs_fleet ON public.office_duty_logs (fleet_id);
CREATE INDEX IF NOT EXISTS idx_duty_logs_vehicle ON public.office_duty_logs (vehicle_no);
CREATE INDEX IF NOT EXISTS idx_duty_logs_driver ON public.office_duty_logs (driver_name);
CREATE INDEX IF NOT EXISTS idx_duty_logs_created_at ON public.office_duty_logs (created_at DESC);

-- Enable Row Level Security (RLS)
ALTER TABLE public.office_duty_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow authenticated fleet owners full access to office_duty_logs"
    ON public.office_duty_logs
    FOR ALL
    TO authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "Allow anon read/write during demo / development"
    ON public.office_duty_logs
    FOR ALL
    TO anon
    USING (true)
    WITH CHECK (true);

-- Enable Realtime
ALTER PUBLICATION supabase_realtime ADD TABLE public.office_duty_logs;

-- Seed initial records from the physical paper register (Image 2)
INSERT INTO public.office_duty_logs 
(fleet_id, shift_type, login_date, login_time, logout_date, logout_time, vehicle_no, driver_name, trips, earnings, cash_collection, expenditure, commission_rent, payment_online, status, remarks)
VALUES
('svm_fleet_hyderabad', '12_HOURS', '01/11/2026', '06:00 AM', '01/11/2026', '08:15 PM', 'TS09 UB 3368', 'Suman', 16, 2950.00, 2503.00, 200.00, 1032.50, 278.00, 'SETTLED', '35% commission tier. Online payment via UPI.'),
('svm_fleet_hyderabad', '12_HOURS', '01/11/2026', '06:30 AM', '01/11/2026', '07:00 PM', 'TS09 UA 2271', 'Narendra', 7, 2271.00, 1850.00, 150.00, 0.00, 0.00, 'SETTLED', 'Trips below min 10. Rent settled in cash.'),
('svm_fleet_hyderabad', '12_HOURS', '01/11/2026', '05:45 AM', '01/11/2026', '06:30 PM', 'TS09 UC 2733', 'Pavan', 11, 2733.00, 2100.00, 100.00, 956.55, 450.00, 'SETTLED', '35% commission tier. On time.'),
('svm_fleet_hyderabad', '12_HOURS', '01/11/2026', '06:15 AM', '01/11/2026', '07:45 PM', 'TS09 UD 3320', 'Siva', 12, 3320.00, 2800.00, 250.00, 1162.00, 350.00, 'SETTLED', 'High collection shift.'),
('svm_fleet_hyderabad', '24_HOURS', '01/11/2026', '08:00 AM', '02/11/2026', '08:00 AM', 'TS09 UF 1840', 'Baleru', 19, 6800.00, 5200.00, 800.00, 2720.00, 1200.00, 'SETTLED', '24hr shift. 40% commission tier earned.')
ON CONFLICT DO NOTHING;
