-- MySQL Sample Data
-- V2__Insert_sample_data.sql

-- Insert sample accounts
INSERT INTO account (user_name, balance, version) VALUES
('Alice Johnson', 1000.00, 0),
('Bob Smith', 2000.00, 0),
('Charlie Brown', 1500.00, 0),
('Diana Prince', 3000.00, 0),
('Eve Wilson', 500.00, 0);

-- Insert sample inventory
INSERT INTO inventory (product_name, quantity, version) VALUES
('Laptop', 50, 0),
('Mouse', 200, 0),
('Keyboard', 150, 0),
('Monitor', 75, 0),
('Headphones', 100, 0),
('Webcam', 80, 0),
('USB Cable', 500, 0);

-- Insert sample orders
INSERT INTO orders (account_id, product_name, amount, status) VALUES
(1, 'Laptop', 999.99, 'PAID'),
(2, 'Mouse', 29.99, 'PAID'),
(3, 'Keyboard', 79.99, 'PENDING'),
(4, 'Monitor', 299.99, 'PAID'),
(5, 'Headphones', 49.99, 'CANCELLED');
