-- ============================================================================
-- Migration: 006_add_managers_hierarchy.sql
-- Description: 3-Tier Hierarchy (Owner -> Multiple Hub Managers -> 10-20+ Drivers per Manager)
-- Fleet: SVM FLEET Hyderabad (Moosapet, Kukatpally, Madhapur)
-- ============================================================================

-- 1. Create Managers Table
CREATE TABLE IF NOT EXISTS managers (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    hub_name VARCHAR(255) NOT NULL,
    pin_code VARCHAR(10) DEFAULT '1234',
    is_active BOOLEAN DEFAULT TRUE,
    owner_id VARCHAR(64) DEFAULT 'owner_akhilesh',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indices for fast phone search and hub queries
CREATE INDEX IF NOT EXISTS idx_managers_phone ON managers(phone);
CREATE INDEX IF NOT EXISTS idx_managers_hub ON managers(hub_name);
CREATE INDEX IF NOT EXISTS idx_managers_owner ON managers(owner_id);

-- 2. Add manager_id foreign keys to core tables
ALTER TABLE drivers 
    ADD COLUMN IF NOT EXISTS manager_id VARCHAR(64) REFERENCES managers(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_drivers_manager ON drivers(manager_id);

ALTER TABLE vehicles 
    ADD COLUMN IF NOT EXISTS manager_id VARCHAR(64) REFERENCES managers(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_vehicles_manager ON vehicles(manager_id);

ALTER TABLE rides 
    ADD COLUMN IF NOT EXISTS manager_id VARCHAR(64) REFERENCES managers(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_rides_manager ON rides(manager_id);

ALTER TABLE office_duty_logs 
    ADD COLUMN IF NOT EXISTS manager_id VARCHAR(64) REFERENCES managers(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS logged_by_role VARCHAR(32) DEFAULT 'MANAGER';
CREATE INDEX IF NOT EXISTS idx_office_duty_logs_manager ON office_duty_logs(manager_id);

-- 3. Seed 3 Real Hyderabad Fleet Hub Managers for SVM FLEET
INSERT INTO managers (id, name, phone, hub_name, pin_code, is_active, owner_id)
VALUES
    ('mgr_moosapet_01', 'Srinivas Rao', '9849012345', 'Moosapet Central Hub', '1234', TRUE, 'owner_akhilesh'),
    ('mgr_kukatpally_02', 'Venkatesh Naidu', '9701234567', 'Kukatpally Y-Junction Hub', '2345', TRUE, 'owner_akhilesh'),
    ('mgr_madhapur_03', 'Mohammed Farooq', '9989056789', 'Madhapur / Hitec City Hub', '3456', TRUE, 'owner_akhilesh')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    phone = EXCLUDED.phone,
    hub_name = EXCLUDED.hub_name,
    pin_code = EXCLUDED.pin_code;

