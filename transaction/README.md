# Transaction Training Demonstration System

A comprehensive training platform for demonstrating database transaction concepts, including local transactions, distributed transactions, and database internal mechanisms.

## Overview

This system provides interactive demonstrations for:
- **Spring Framework Transactions**: Propagation behaviors, isolation levels, rollback rules
- **Distributed Transactions**: Seata framework (AT/TCC/SAGA modes), transaction patterns
- **Database Internals**: MVCC, Redo/Undo logs, WAL protocol, lock mechanisms

## Features

✅ **Multi-Database Support**: Switch between MySQL and Oracle via configuration  
✅ **20+ Transaction Scenarios**: Comprehensive coverage of transaction concepts  
✅ **Interactive UI**: Real-time execution results with code examples  
✅ **Visual Demonstrations**: Timeline views, state comparisons, log visualization  
✅ **Docker Support**: One-command deployment with Docker Compose  

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 17
- **Database**: MySQL 8.0+ / Oracle 19c+
- **Transaction Management**: Spring Transaction, Seata, ShardingSphere
- **API Documentation**: SpringDoc OpenAPI (Swagger)

### Frontend
- **Framework**: Vue.js 3.4
- **UI Library**: Element Plus 2.5
- **State Management**: Pinia
- **Build Tool**: Vite 5.0
- **Visualization**: ECharts

## Prerequisites

### For Local Development
- Java 17 or higher
- Node.js 18 or higher
- Maven 3.6+
- MySQL 8.0+ (or Oracle 19c+)

### For Docker Deployment
- Docker 20.10+
- Docker Compose 2.0+

## Quick Start with Docker

### Windows PowerShell

```powershell
# Clone the repository
cd D:\code\AICode\transaction

# Start all services (MySQL + Backend + Frontend)
docker-compose up -d

# Check service status
docker-compose ps

# View backend logs
docker-compose logs -f backend

# View frontend logs
docker-compose logs -f frontend
```

### Access the Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Stop Services
```powershell
docker-compose down

# Remove volumes (clean database)
docker-compose down -v
```

## Local Development Setup

### Backend Setup

```powershell
cd D:\code\AICode\transaction

# Configure database connection (edit application-mysql.yml or application-oracle.yml)
# Update: spring.datasource.url, username, password

# Run backend
.\mvnw spring-boot:run -Dspring-boot.run.profiles=mysql

# Or build and run
.\mvnw clean package
java -jar target\transaction-training-1.0.0.jar --spring.profiles.active=mysql
```

### Frontend Setup

```powershell
cd D:\code\AICode\transaction\frontend

# Install dependencies
npm install

# Run development server
npm run dev

# Build for production
npm run build
```

## Database Configuration

### MySQL Setup

```sql
-- Create database and user
CREATE DATABASE txdemo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'txdemo_user'@'%' IDENTIFIED BY 'txdemo_pass';
GRANT ALL PRIVILEGES ON txdemo.* TO 'txdemo_user'@'%';
FLUSH PRIVILEGES;
```

Update `application-mysql.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/txdemo?useSSL=false&serverTimezone=UTC
    username: txdemo_user
    password: txdemo_pass
```

### Oracle Setup

```sql
-- Create user and grant privileges
CREATE USER txdemo_user IDENTIFIED BY txdemo_pass;
GRANT CONNECT, RESOURCE, CREATE VIEW TO txdemo_user;
GRANT UNLIMITED TABLESPACE TO txdemo_user;
```

Update `application-oracle.yml`:
```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:ORCL
    username: txdemo_user
    password: txdemo_pass
```

### Switch Between Databases

**Method 1: Spring Profile**
```powershell
# Use MySQL
java -jar target\transaction-training-1.0.0.jar --spring.profiles.active=mysql

# Use Oracle
java -jar target\transaction-training-1.0.0.jar --spring.profiles.active=oracle
```

**Method 2: UI Selection**
- Use the database selector dropdown in the application header
- Changes take effect immediately

## Project Structure

```
transaction/
├── src/
│   ├── main/
│   │   ├── java/com/transaction/training/
│   │   │   ├── config/           # Database configuration
│   │   │   ├── controller/       # REST API controllers
│   │   │   ├── service/          # Demo service implementations
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── repository/       # Data repositories
│   │   │   └── dto/              # Data transfer objects
│   │   └── resources/
│   │       ├── db/migration/     # Flyway migration scripts
│   │       │   ├── mysql/        # MySQL-specific SQL
│   │       │   └── oracle/       # Oracle-specific SQL
│   │       └── application*.yml  # Configuration files
│   └── test/                     # Unit and integration tests
├── frontend/
│   ├── src/
│   │   ├── api/                  # API client
│   │   ├── components/           # Reusable components
│   │   ├── views/                # Page components
│   │   │   ├── spring/           # Spring transaction demos
│   │   │   ├── distributed/      # Distributed transaction demos
│   │   │   └── internals/        # Database internals demos
│   │   ├── router/               # Vue Router configuration
│   │   └── styles/               # Global styles
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## Demo Categories

### 1. Spring Transaction Propagation

Demonstrates 7 propagation behaviors:
- **REQUIRED**: Join existing or create new transaction
- **REQUIRES_NEW**: Always create new independent transaction
- **NESTED**: Execute within nested transaction (savepoint)
- **SUPPORTS**: Join if exists, otherwise non-transactional
- **NOT_SUPPORTED**: Always execute non-transactionally
- **MANDATORY**: Require existing transaction
- **NEVER**: Must execute without transaction

**API Endpoint**: `POST /api/demo/spring/propagation/{type}`

### 2. Transaction Isolation Levels

Demonstrates 4 isolation levels and read phenomena:
- **READ_UNCOMMITTED**: Allows dirty reads
- **READ_COMMITTED**: Prevents dirty reads
- **REPEATABLE_READ**: Prevents non-repeatable reads
- **SERIALIZABLE**: Complete isolation

**API Endpoint**: `POST /api/demo/spring/isolation/{level}`

### 3. Distributed Transactions

Demonstrates distributed transaction patterns:
- **Seata AT Mode**: Automatic transaction coordination
- **Seata TCC Mode**: Try-Confirm-Cancel pattern
- **Seata SAGA Mode**: Long transaction workflow
- **2PC/3PC Patterns**: Two/Three-phase commit

**API Endpoint**: `POST /api/demo/distributed/seata/{mode}`

### 4. Database Internals

Visual demonstrations of internal mechanisms:
- **MVCC**: Version chain and snapshot isolation
- **Redo Log**: Write-ahead logging and recovery
- **Undo Log**: Rollback mechanism
- **Lock Mechanisms**: Row locks, table locks, gap locks

**API Endpoint**: `POST /api/demo/internals/{type}`

## API Documentation

Access Swagger UI for interactive API testing:
```
http://localhost:8080/swagger-ui.html
```

### Key Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/database/profiles` | GET | List available database profiles |
| `/api/database/active` | GET | Get current active database |
| `/api/database/switch` | POST | Switch database profile |
| `/api/demo/spring/propagation/{type}` | POST | Run propagation demo |
| `/api/demo/spring/isolation/{level}` | POST | Run isolation demo |
| `/api/demo/distributed/seata/{mode}` | POST | Run Seata demo |
| `/api/demo/internals/{type}` | POST | Run internals demo |
| `/api/demo/reset` | POST | Reset demo data |
| `/api/demo/logs` | GET | Get execution logs |

## Usage Examples

### PowerShell Commands

```powershell
# Test propagation demo (REQUIRED)
Invoke-RestMethod -Uri "http://localhost:8080/api/demo/spring/propagation/required" -Method POST

# Test isolation demo (READ_COMMITTED)
Invoke-RestMethod -Uri "http://localhost:8080/api/demo/spring/isolation/read-committed" -Method POST

# Switch to Oracle database
$body = @{ profile = "oracle" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/database/switch" -Method POST -Body $body -ContentType "application/json"

# Reset demo data
Invoke-RestMethod -Uri "http://localhost:8080/api/demo/reset" -Method POST
```

### Using the UI

1. **Access the dashboard**: Navigate to http://localhost:3000
2. **Select database**: Use dropdown in header (MySQL or Oracle)
3. **Choose demo category**: Click on Spring Transactions, Distributed, or Internals
4. **Run demonstration**: Click button for specific scenario
5. **Review results**: Analyze steps, logs, code examples, and database state changes

## Training Workflow

### Recommended Training Sequence

1. **Introduction** (15 min)
   - System overview and architecture
   - Database connection verification
   - UI navigation tour

2. **Spring Transaction Basics** (45 min)
   - REQUIRED propagation demonstration
   - Isolation level comparison
   - Rollback rules and exception handling
   - Common pitfalls (self-invocation, transaction boundaries)

3. **Advanced Spring Transactions** (30 min)
   - REQUIRES_NEW and NESTED demonstrations
   - Read-only optimization
   - Timeout handling
   - Programmatic transactions

4. **Distributed Transactions** (45 min)
   - Seata AT mode (automatic)
   - TCC pattern implementation
   - SAGA long transaction workflow
   - Failure scenarios and compensation

5. **Database Internals** (45 min)
   - MVCC visualization
   - Redo/Undo log mechanisms
   - Lock types and deadlock scenarios
   - WAL protocol and durability

6. **Hands-on Practice** (30 min)
   - Participants run demos independently
   - Troubleshoot scenarios
   - Q&A session

## Troubleshooting

### Backend Won't Start

```powershell
# Check Java version
java -version  # Should be 17+

# Check if port 8080 is in use
netstat -ano | findstr :8080

# Check database connectivity
# Verify MySQL/Oracle is running and credentials are correct
```

### Frontend Build Errors

```powershell
# Clear node modules and reinstall
cd frontend
Remove-Item node_modules -Recurse -Force
Remove-Item package-lock.json -Force
npm install
```

### Database Connection Issues

1. Verify database is running:
```powershell
# MySQL
mysql -u txdemo_user -p -h localhost

# Check MySQL service (Windows)
Get-Service | Where-Object {$_.Name -like "*mysql*"}
```

2. Check Flyway migrations:
```powershell
# View migration status in database
SELECT * FROM flyway_schema_history;
```

3. Verify firewall rules allow connections on ports 3306 (MySQL) or 1521 (Oracle)

### Docker Issues

```powershell
# View all container logs
docker-compose logs

# Rebuild containers
docker-compose build --no-cache

# Remove all containers and volumes
docker-compose down -v
docker system prune -a
```

## Performance Tuning

### Backend Optimization

Edit `application.yml`:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # Increase for high concurrency
      minimum-idle: 10
      connection-timeout: 30000

logging:
  level:
    org.hibernate.SQL: INFO      # Reduce logging in production
```

### Frontend Optimization

```javascript
// vite.config.js - Production build optimization
export default defineConfig({
  build: {
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true       // Remove console logs
      }
    }
  }
})
```

## Security Considerations

⚠️ **This is a training system - NOT for production use**

- Default passwords are insecure - change them
- No authentication/authorization implemented
- CORS is open for all origins
- Database credentials are in plain text
- For production deployment, implement:
  - JWT authentication
  - Role-based access control
  - Secrets management (Vault, AWS Secrets Manager)
  - HTTPS/TLS encryption
  - API rate limiting

## Contributing

This is a training demonstration system. To add new demo scenarios:

1. Create service method in appropriate service class
2. Add controller endpoint
3. Create migration scripts for both MySQL and Oracle
4. Implement frontend view component
5. Update router configuration
6. Add API documentation annotations

## License

This project is for educational purposes. Use at your own discretion.

## Support

For issues or questions:
- Check Swagger documentation: http://localhost:8080/swagger-ui.html
- Review application logs
- Verify database connectivity and schema

## Version History

- **v1.0.0** (2026-01-09): Initial release
  - Spring transaction demos
  - Multi-database support
  - Docker deployment
  - Interactive UI

---

**Built with ❤️ for database transaction training**
