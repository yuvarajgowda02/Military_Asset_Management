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
