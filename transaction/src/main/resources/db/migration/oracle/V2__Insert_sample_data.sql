-- Oracle Sample Data
-- V2__Insert_sample_data.sql

-- Insert sample accounts
INSERT INTO account (user_name, balance, version) VALUES ('Alice Johnson', 1000.00, 0);
INSERT INTO account (user_name, balance, version) VALUES ('Bob Smith', 2000.00, 0);
INSERT INTO account (user_name, balance, version) VALUES ('Charlie Brown', 1500.00, 0);
INSERT INTO account (user_name, balance, version) VALUES ('Diana Prince', 3000.00, 0);
INSERT INTO account (user_name, balance, version) VALUES ('Eve Wilson', 500.00, 0);

-- Insert sample inventory
INSERT INTO inventory (product_name, quantity, version) VALUES ('Laptop', 50, 0);
INSERT INTO inventory (product_name, quantity, version) VALUES ('Mouse', 200, 0);
INSERT INTO inventory (product_name, quantity, version) VALUES ('Keyboard', 150, 0);
INSERT INTO inventory (product_name, quantity, version) VALUES ('Monitor', 75, 0);
INSERT INTO inventory (product_name, quantity, version) VALUES ('Headphones', 100, 0);
INSERT INTO inventory (product_name, quantity, version) VALUES ('Webcam', 80, 0);
INSERT INTO inventory (product_name, quantity, version) VALUES ('USB Cable', 500, 0);

-- Insert sample orders
INSERT INTO orders (account_id, product_name, amount, status) VALUES (1, 'Laptop', 999.99, 'PAID');
INSERT INTO orders (account_id, product_name, amount, status) VALUES (2, 'Mouse', 29.99, 'PAID');
INSERT INTO orders (account_id, product_name, amount, status) VALUES (3, 'Keyboard', 79.99, 'PENDING');
INSERT INTO orders (account_id, product_name, amount, status) VALUES (4, 'Monitor', 299.99, 'PAID');
INSERT INTO orders (account_id, product_name, amount, status) VALUES (5, 'Headphones', 49.99, 'CANCELLED');

COMMIT;
