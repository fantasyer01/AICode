-- ================================================
-- Audit Demo Database Initialization Script
-- ================================================
-- Database: MySQL 5.7+
-- Description: Creates all tables required for the audit demo project
-- ================================================

-- Set connection charset (Important for Chinese characters)
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Create database (if not exists)
CREATE DATABASE IF NOT EXISTS audit_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE audit_demo;

-- Ensure database uses utf8mb4
ALTER DATABASE audit_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ================================================
-- 1. Audit System Tables
-- ================================================

-- Audit Request Table
DROP TABLE IF EXISTS sys_audit_request;
CREATE TABLE sys_audit_request (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    business_type   VARCHAR(50) NOT NULL COMMENT 'Business type: USER_MGMT, ORDER_MGMT',
    business_desc   VARCHAR(200) COMMENT 'Business description',
    operation_type  VARCHAR(10) NOT NULL COMMENT 'Operation type: INSERT/UPDATE/DELETE',
    
    -- Method invocation info for callback
    bean_name       VARCHAR(100) COMMENT 'Spring bean name',
    method_name     VARCHAR(100) COMMENT 'Method name',
    param_types     TEXT COMMENT 'Parameter types (JSON array)',
    method_args     TEXT COMMENT 'Method arguments (JSON)',
    
    -- Snapshot for display
    snapshot_data   TEXT COMMENT 'Snapshot data for display',
    
    -- Status
    status          TINYINT DEFAULT 0 COMMENT 'Status: 0-pending 1-approved 2-rejected',
    
    -- Submitter info
    submit_user_id   BIGINT COMMENT 'Submitter ID',
    submit_user_name VARCHAR(50) COMMENT 'Submitter name',
    submit_time      DATETIME COMMENT 'Submit time',
    submit_remark    VARCHAR(500) COMMENT 'Submit remark',
    
    -- Auditor info
    audit_user_id    BIGINT COMMENT 'Auditor ID',
    audit_user_name  VARCHAR(50) COMMENT 'Auditor name',
    audit_time       DATETIME COMMENT 'Audit time',
    audit_remark     VARCHAR(500) COMMENT 'Audit remark',
    
    -- Timestamps
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    
    INDEX idx_status (status),
    INDEX idx_business_type (business_type),
    INDEX idx_submit_user (submit_user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Audit Request Table';


-- Audit History Table
DROP TABLE IF EXISTS sys_audit_history;
CREATE TABLE sys_audit_history (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    request_id      BIGINT NOT NULL COMMENT 'Audit request ID',
    action          VARCHAR(20) NOT NULL COMMENT 'Action: SUBMIT/APPROVE/REJECT',
    operator_id     BIGINT COMMENT 'Operator ID',
    operator_name   VARCHAR(50) COMMENT 'Operator name',
    operate_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Operation time',
    remark          VARCHAR(500) COMMENT 'Remark',
    snapshot_before TEXT COMMENT 'Snapshot before operation',
    
    INDEX idx_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Audit History Table';


-- ================================================
-- 2. Field Permission Table (Fine-grained Query Control)
-- ================================================

-- Field Permission Table
DROP TABLE IF EXISTS sys_field_permission;
CREATE TABLE sys_field_permission (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    table_name      VARCHAR(100) NOT NULL COMMENT 'Table name: biz_user, biz_order, biz_order_item',
    field_name      VARCHAR(100) NOT NULL COMMENT 'Field name in entity (camelCase)',
    field_label     VARCHAR(100) COMMENT 'Display label for admin UI',
    queryable       TINYINT DEFAULT 1 COMMENT 'Queryable: 1=allowed, 0=hidden',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    
    UNIQUE INDEX idx_table_field (table_name, field_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Field Permission Table';


-- ================================================
-- 3. Business Demo Tables
-- ================================================

-- User Table (用户表 - 单表演示)
DROP TABLE IF EXISTS biz_user;
CREATE TABLE biz_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username    VARCHAR(50) NOT NULL COMMENT '用户名',
    real_name   VARCHAR(50) COMMENT '真实姓名',
    email       VARCHAR(100) COMMENT '邮箱',
    phone       VARCHAR(20) COMMENT '手机号',
    department  VARCHAR(100) COMMENT '部门',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


-- Order Main Table (订单主表 - 多表演示)
DROP TABLE IF EXISTS biz_order;
CREATE TABLE biz_order (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no         VARCHAR(50) NOT NULL COMMENT '订单号',
    customer_name    VARCHAR(100) NOT NULL COMMENT '客户名称',
    customer_phone   VARCHAR(20) COMMENT '客户电话',
    shipping_address VARCHAR(500) COMMENT '收货地址',
    total_amount     DECIMAL(12,2) DEFAULT 0 COMMENT '订单总金额',
    status           TINYINT DEFAULT 0 COMMENT '状态: 0-待确认 1-已确认 2-已发货 3-已完成 4-已取消',
    remark           VARCHAR(500) COMMENT '备注',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE INDEX idx_order_no (order_no),
    INDEX idx_customer_name (customer_name),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';


-- Order Item Table (订单明细表 - 多表演示)
DROP TABLE IF EXISTS biz_order_item;
CREATE TABLE biz_order_item (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id      BIGINT NOT NULL COMMENT '订单ID（外键）',
    product_name  VARCHAR(200) NOT NULL COMMENT '商品名称',
    specification VARCHAR(100) COMMENT '商品规格',
    unit_price    DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity      INT NOT NULL DEFAULT 1 COMMENT '数量',
    subtotal      DECIMAL(12,2) COMMENT '小计金额',
    
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';


-- ================================================
-- 3. Demo Data (Optional)
-- ================================================

-- Note: If you still encounter encoding issues, run this command in MySQL first:
-- SET NAMES utf8mb4;

-- Insert some demo users
INSERT INTO biz_user (username, real_name, email, phone, department, status) VALUES
('zhangsan', 'Zhang San', 'zhangsan@example.com', '13800138001', 'Tech Dept', 1),
('lisi', 'Li Si', 'lisi@example.com', '13800138002', 'Marketing', 1),
('wangwu', 'Wang Wu', 'wangwu@example.com', '13800138003', 'Finance', 1),
('zhaoliu', 'Zhao Liu', 'zhaoliu@example.com', '13800138004', 'HR Dept', 0);

-- Insert some demo orders
INSERT INTO biz_order (order_no, customer_name, customer_phone, shipping_address, total_amount, status) VALUES
('ORD20240101001', 'Customer A Co.', '13900139001', 'Beijing Chaoyang District', 15000.00, 1),
('ORD20240101002', 'Customer B Co.', '13900139002', 'Shanghai Pudong District', 8500.00, 2);

-- Insert demo order items
INSERT INTO biz_order_item (order_id, product_name, specification, unit_price, quantity, subtotal) VALUES
(1, 'Laptop', 'ThinkPad X1 Carbon', 12000.00, 1, 12000.00),
(1, 'Wireless Mouse', 'Logitech MX Master 3', 800.00, 2, 1600.00),
(1, 'Mechanical Keyboard', 'HHKB Professional', 1400.00, 1, 1400.00),
(2, 'Monitor', 'Dell 27 inch 4K', 3500.00, 2, 7000.00),
(2, 'Keyboard Tray', 'Ergonomic', 500.00, 3, 1500.00);


-- ================================================
-- 4. Field Permission Initial Data
-- ================================================

-- biz_user fields
INSERT INTO sys_field_permission (table_name, field_name, field_label, queryable) VALUES
('biz_user', 'id', 'ID', 1),
('biz_user', 'username', 'Username', 1),
('biz_user', 'realName', 'Real Name', 1),
('biz_user', 'email', 'Email', 1),
('biz_user', 'phone', 'Phone', 1),
('biz_user', 'department', 'Department', 1),
('biz_user', 'status', 'Status', 1),
('biz_user', 'createTime', 'Create Time', 1),
('biz_user', 'updateTime', 'Update Time', 1);

-- biz_order fields
INSERT INTO sys_field_permission (table_name, field_name, field_label, queryable) VALUES
('biz_order', 'id', 'ID', 1),
('biz_order', 'orderNo', 'Order No', 1),
('biz_order', 'customerName', 'Customer Name', 1),
('biz_order', 'customerPhone', 'Customer Phone', 1),
('biz_order', 'shippingAddress', 'Shipping Address', 1),
('biz_order', 'totalAmount', 'Total Amount', 1),
('biz_order', 'status', 'Status', 1),
('biz_order', 'remark', 'Remark', 1),
('biz_order', 'createTime', 'Create Time', 1),
('biz_order', 'updateTime', 'Update Time', 1);

-- biz_order_item fields
INSERT INTO sys_field_permission (table_name, field_name, field_label, queryable) VALUES
('biz_order_item', 'id', 'ID', 1),
('biz_order_item', 'orderId', 'Order ID', 1),
('biz_order_item', 'productName', 'Product Name', 1),
('biz_order_item', 'specification', 'Specification', 1),
('biz_order_item', 'unitPrice', 'Unit Price', 1),
('biz_order_item', 'quantity', 'Quantity', 1),
('biz_order_item', 'subtotal', 'Subtotal', 1);


-- ================================================
-- Verification Queries
-- ================================================
-- You can run these queries to verify the setup:

-- SELECT * FROM sys_audit_request;
-- SELECT * FROM sys_audit_history;
-- SELECT * FROM biz_user;
-- SELECT * FROM biz_order;
-- SELECT * FROM biz_order_item;

-- ================================================
-- End of Script
-- ================================================
