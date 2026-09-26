-- ==========================================================
-- Military Asset Management System - Seed / Demo Data
-- Run AFTER schema.sql
-- Passwords below are BCrypt-hashed. Plaintext (for demo login):
--   admin             / Admin@123           (ADMIN)
--   commander.alpha    / Commander@123       (BASE_COMMANDER - Fort Alpha)
--   logistics.alpha     / Logistics@123      (LOGISTICS_OFFICER - Fort Alpha)
--   commander.bravo    / Commander@123       (BASE_COMMANDER - Fort Bravo)
-- ==========================================================

USE military_asset_db;

-- ----------------------------------------------------------
-- Bases
-- ----------------------------------------------------------
INSERT INTO bases (id, name, location) VALUES
 (1, 'Fort Alpha',   'Northern Command'),
 (2, 'Fort Bravo',   'Eastern Command'),
 (3, 'Fort Charlie', 'Western Command');

-- ----------------------------------------------------------
-- Equipment types
-- ----------------------------------------------------------
INSERT INTO equipment_types (id, name, category, unit) VALUES
 (1, 'M4 Rifle',        'WEAPON',     'units'),
 (2, 'Humvee',           'VEHICLE',    'units'),
 (3, '5.56mm Rounds',    'AMMUNITION', 'rounds');

-- ----------------------------------------------------------
-- Users
-- ----------------------------------------------------------
INSERT INTO users (id, username, password_hash, full_name, role, base_id, enabled) VALUES
 (1, 'admin',            '$2b$10$MCaPj3qG8/aVGGgwgTRu1uwxTo/96L1MLVI0BHzERgYxqD/kNu4c2', 'System Administrator', 'ADMIN', NULL, 1),
 (2, 'commander.alpha',  '$2b$10$5OTUfFoIh6n3.IUm9R5vA.2t5mA7AC7SkxNYfPxX4hpcvmunYcZUa', 'Col. Sarah Mitchell', 'BASE_COMMANDER', 1, 1),
 (3, 'logistics.alpha',  '$2b$10$LZwM0QfClRlg/M6S/AwbM.19f2KaFqtYlvIhpbVChPRKt/nxSWQ5a', 'Lt. James Carter', 'LOGISTICS_OFFICER', 1, 1),
 (4, 'commander.bravo',  '$2b$10$5OTUfFoIh6n3.IUm9R5vA.2t5mA7AC7SkxNYfPxX4hpcvmunYcZUa', 'Col. Raj Patel', 'BASE_COMMANDER', 2, 1);

-- ----------------------------------------------------------
-- Purchases
-- ----------------------------------------------------------
INSERT INTO purchases (base_id, equipment_type_id, quantity, unit_cost, total_cost, purchase_date, remarks, created_by) VALUES
 (1, 1, 150, 1200.00, 180000.00, DATE_SUB(CURDATE(), INTERVAL 40 DAY), 'Initial procurement', 1),
 (1, 3, 50000, 0.35,  17500.00,  DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'Quarterly ammo resupply', 3),
 (2, 2, 10,   75000.00, 750000.00, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'Fleet expansion', 1);

-- ----------------------------------------------------------
-- Transfers
-- ----------------------------------------------------------
INSERT INTO transfers (from_base_id, to_base_id, equipment_type_id, quantity, transfer_date, status, remarks, created_by) VALUES
 (2, 1, 2, 3,  DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'COMPLETED', 'Reinforcement for training exercise', 1),
 (1, 3, 1, 20, DATE_SUB(CURDATE(), INTERVAL 5 DAY),  'COMPLETED', 'Support new deployment', 2);

-- ----------------------------------------------------------
-- Assignments
-- ----------------------------------------------------------
INSERT INTO assignments (base_id, equipment_type_id, personnel_name, personnel_service_number, quantity, assigned_date, status, created_by) VALUES
 (1, 1, 'Sgt. Daniel Reyes', 'SVC-10234', 1, DATE_SUB(CURDATE(), INTERVAL 8 DAY), 'ASSIGNED', 2),
 (1, 2, 'Cpl. Maria Gomez',  'SVC-10877', 1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'ASSIGNED', 2);

-- ----------------------------------------------------------
-- Expenditures
-- ----------------------------------------------------------
INSERT INTO expenditures (base_id, equipment_type_id, quantity, expended_date, reason, created_by) VALUES
 (1, 3, 5000, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'Live-fire training exercise', 3);
