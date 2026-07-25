-- Initialize test database and tables

USE simulator_db;

-- Create a simple test table
CREATE TABLE IF NOT EXISTS test_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    value VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert some initial test data
INSERT INTO test_data (name, value) VALUES 
    ('test1', 'Initial test value 1'),
    ('test2', 'Initial test value 2'),
    ('test3', 'Initial test value 3');

-- Create a table to log connection test results
CREATE TABLE IF NOT EXISTS connection_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    message VARCHAR(500),
    response_time_ms BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
