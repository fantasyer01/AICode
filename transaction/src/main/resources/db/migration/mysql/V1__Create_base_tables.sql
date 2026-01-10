-- MySQL Schema Migration
-- V1__Create_base_tables.sql

-- Account Table
CREATE TABLE account (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_name (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Account table for transaction demos';

-- Order Table
CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id),
    INDEX idx_account_id (account_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order table for transaction demos';

-- Inventory Table
CREATE TABLE inventory (
    inventory_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(200) NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_name (product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory table for transaction demos';

-- Transaction Log Table
CREATE TABLE transaction_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_type VARCHAR(100) NOT NULL,
    operation VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,
    details TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_executed_at (executed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Transaction execution log';

-- Redo Log Simulation Table
CREATE TABLE redo_log_simulation (
    log_sequence_number BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    before_image TEXT,
    after_image TEXT,
    log_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_log_time (log_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Redo log simulation for demos';

-- Undo Log Simulation Table
CREATE TABLE undo_log_simulation (
    undo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL,
    rollback_sql TEXT NOT NULL,
    rollback_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Undo log simulation for demos';

-- MVCC Version Chain Table
CREATE TABLE mvcc_version_chain (
    version_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    row_id BIGINT NOT NULL,
    transaction_id VARCHAR(100) NOT NULL,
    data_snapshot TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_row_id (row_id),
    INDEX idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MVCC version chain simulation';
