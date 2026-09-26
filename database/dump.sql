-- ==========================================================
-- Military Asset Management System - Schema
-- Database: MySQL 8.x
-- ==========================================================

CREATE DATABASE IF NOT EXISTS military_asset_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE military_asset_db;

-- ----------------------------------------------------------
-- bases
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS bases (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(120) NOT NULL UNIQUE,
    location    VARCHAR(200),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- equipment_types
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS equipment_types (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(120) NOT NULL UNIQUE,
    category    ENUM('WEAPON','VEHICLE','AMMUNITION') NOT NULL,
    unit        VARCHAR(30) DEFAULT 'units',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- users
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(60) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(120),
    role            ENUM('ADMIN','BASE_COMMANDER','LOGISTICS_OFFICER') NOT NULL,
    base_id         BIGINT NULL,
    enabled         TINYINT(1) NOT NULL DEFAULT 1,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_base FOREIGN KEY (base_id) REFERENCES bases(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- purchases
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchases (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_id             BIGINT NOT NULL,
    equipment_type_id   BIGINT NOT NULL,
    quantity            INT NOT NULL,
    unit_cost           DECIMAL(14,2),
    total_cost          DECIMAL(16,2),
    purchase_date       DATE NOT NULL,
    remarks             VARCHAR(250),
    created_by          BIGINT,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchases_base FOREIGN KEY (base_id) REFERENCES bases(id),
    CONSTRAINT fk_purchases_equipment FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id),
    CONSTRAINT fk_purchases_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_purchases_date (purchase_date),
    INDEX idx_purchases_base_equipment (base_id, equipment_type_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- transfers
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS transfers (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_base_id        BIGINT NOT NULL,
    to_base_id          BIGINT NOT NULL,
    equipment_type_id   BIGINT NOT NULL,
    quantity            INT NOT NULL,
    transfer_date       DATE NOT NULL,
    status              ENUM('COMPLETED','PENDING','CANCELLED') NOT NULL DEFAULT 'COMPLETED',
    remarks             VARCHAR(250),
    created_by          BIGINT,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfers_from_base FOREIGN KEY (from_base_id) REFERENCES bases(id),
    CONSTRAINT fk_transfers_to_base FOREIGN KEY (to_base_id) REFERENCES bases(id),
    CONSTRAINT fk_transfers_equipment FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id),
    CONSTRAINT fk_transfers_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_transfers_date (transfer_date),
    INDEX idx_transfers_from (from_base_id),
    INDEX idx_transfers_to (to_base_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- assignments
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS assignments (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_id                     BIGINT NOT NULL,
    equipment_type_id           BIGINT NOT NULL,
    personnel_name              VARCHAR(120) NOT NULL,
    personnel_service_number    VARCHAR(60),
    quantity                    INT NOT NULL,
    assigned_date               DATE NOT NULL,
    returned_date               DATE NULL,
    status                      ENUM('ASSIGNED','RETURNED') NOT NULL DEFAULT 'ASSIGNED',
    remarks                     VARCHAR(250),
    created_by                  BIGINT,
    created_at                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assignments_base FOREIGN KEY (base_id) REFERENCES bases(id),
    CONSTRAINT fk_assignments_equipment FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id),
    CONSTRAINT fk_assignments_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_assignments_date (assigned_date),
    INDEX idx_assignments_base_equipment (base_id, equipment_type_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- expenditures
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS expenditures (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_id             BIGINT NOT NULL,
    equipment_type_id   BIGINT NOT NULL,
    quantity            INT NOT NULL,
    expended_date       DATE NOT NULL,
    reason              VARCHAR(250),
    created_by          BIGINT,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expenditures_base FOREIGN KEY (base_id) REFERENCES bases(id),
    CONSTRAINT fk_expenditures_equipment FOREIGN KEY (equipment_type_id) REFERENCES equipment_types(id),
    CONSTRAINT fk_expenditures_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_expenditures_date (expended_date),
    INDEX idx_expenditures_base_equipment (base_id, equipment_type_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- audit_logs  (transaction / API audit trail)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    username            VARCHAR(60),
    user_role           VARCHAR(30),
    method              VARCHAR(20) NOT NULL,
    endpoint            VARCHAR(250) NOT NULL,
    action              VARCHAR(60) NOT NULL,
    request_summary     TEXT,
    status_code         INT,
    ip_address          VARCHAR(60),
    timestamp           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_timestamp (timestamp),
    INDEX idx_audit_username (username)
) ENGINE=InnoDB;
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
