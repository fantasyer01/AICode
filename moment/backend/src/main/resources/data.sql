-- Create user
-- CREATE USER 'portfolio'@'localhost' IDENTIFIED BY 'portfolio';
-- CREATE USER 'portfolio'@'%' IDENTIFIED BY 'portfolio';

-- Grant privileges
-- GRANT ALL PRIVILEGES ON portfolio.* TO 'portfolio'@'localhost';
-- GRANT ALL PRIVILEGES ON portfolio.* TO 'portfolio'@'%';

-- Apply changes
-- FLUSH PRIVILEGES;

CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    date VARCHAR(7) NOT NULL,
    year INT NOT NULL,
    tools JSON NOT NULL,
    color VARCHAR(10) NOT NULL,
    link VARCHAR(500),
    github VARCHAR(500),
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project_category (category),
    INDEX idx_project_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Sample test data for the Portfolio Showcase application
-- This data matches the frontend mock data

INSERT INTO projects (title, description, category, date, year, tools, color, link, github, image_url) VALUES ('AI Chat Interface', 'A modern conversational AI interface with real-time streaming responses and beautiful animations.', 'AI', '2024-01', 2024, '["React", "OpenAI", "Framer Motion"]', '0', NULL, '#', 'ai-chat.jpg');

INSERT INTO projects (title, description, category, date, year, tools, color, link, github, image_url) VALUES ('E-commerce Dashboard', 'Comprehensive analytics dashboard for tracking sales, inventory, and customer insights.', 'WEB', '2024-02', 2024, '["Next.js", "Tailwind", "Prisma"]', '1', '#', NULL, 'ecommerce-dashboard.jpg');

INSERT INTO projects (title, description, category, date, year, tools, color, link, github, image_url) VALUES ('Smart Data Visualizer', 'Transform complex datasets into beautiful, interactive visualizations with AI assistance.', 'DATA', '2023-11', 2023, '["D3.js", "Python", "TensorFlow"]', '2', NULL, '#', 'data-visualizer.jpg');

INSERT INTO projects (title, description, category, date, year, tools, color, link, github, image_url) VALUES ('Fitness Tracking App', 'Mobile-first fitness application with AI-powered workout recommendations.', 'MOBILE', '2023-09', 2023, '["React Native", "Firebase", "HealthKit"]', '3', '#', NULL, 'fitness-app.jpg');

INSERT INTO projects (title, description, category, date, year, tools, color, link, github, image_url) VALUES ('Code Documentation Bot', 'Automated documentation generator that analyzes codebases and creates comprehensive docs.', 'AI', '2023-06', 2023, '["GPT-4", "LangChain", "Vector DB"]', '4', NULL, '#', 'doc-bot.jpg');

INSERT INTO projects (title, description, category, date, year, tools, color, link, github, image_url) VALUES ('Portfolio Generator', 'AI-assisted tool for creating stunning portfolio websites from templates.', 'WEB', '2023-03', 2023, '["Vue.js", "Node.js", "MongoDB"]', '0', '#', NULL, 'portfolio-generator.jpg');
