# Local Development Setup Guide (Without Docker)

Complete guide to run the Transaction Training System locally on Windows without Docker.

---

## Prerequisites

- Windows 10/11
- Java 17 or higher
- Node.js 18 or higher
- MySQL 8.0+

---

## Step 1: Install MySQL 8.0

### Download MySQL

```powershell
# Download MySQL Community Server from:
# https://dev.mysql.com/downloads/mysql/

# Or use Windows installer:
# https://dev.mysql.com/downloads/installer/
# Choose: mysql-installer-community-8.0.XX.msi
```

### Install MySQL

1. Run the installer
2. Choose "Developer Default" setup type
3. Set root password: `root_password` (remember this)
4. Complete installation
5. Verify installation:

```powershell
# Check MySQL service status
Get-Service MySQL80

# If not running, start it
Start-Service MySQL80

# Verify MySQL command
mysql --version
# Expected: mysql  Ver 8.0.XX
```

---

## Step 2: Create Database and User

### Connect to MySQL

```powershell
# Connect as root
mysql -u root -p
# Enter password: root_password
```

### Run Database Setup Script

```sql
-- Create database
CREATE DATABASE txdemo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'txdemo_user'@'localhost' IDENTIFIED BY 'txdemo_pass';
CREATE USER 'txdemo_user'@'%' IDENTIFIED BY 'txdemo_pass';

-- Grant privileges
GRANT ALL PRIVILEGES ON txdemo.* TO 'txdemo_user'@'localhost';
GRANT ALL PRIVILEGES ON txdemo.* TO 'txdemo_user'@'%';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify database
SHOW DATABASES;

-- Exit
EXIT;
```

### Verify Connection

```powershell
# Test connection with new user
mysql -u txdemo_user -p txdemo
# Enter password: txdemo_pass

# Should connect successfully
# Type: EXIT
```

---

## Step 3: Configure Backend Application

### Update Database Configuration

Open `D:\code\AICode\transaction\src\main\resources\application-mysql.yml` in VS Code:

```powershell
code D:\code\AICode\transaction\src\main\resources\application-mysql.yml
```

Verify/Update configuration:

```yaml
# MySQL Profile
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/txdemo?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: txdemo_user
    password: txdemo_pass
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      pool-name: TxDemoHikariPool-MySQL
  
  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: validate
  
  flyway:
    locations: classpath:db/migration/mysql
```

---

## Step 4: Start Backend Application

### Using Maven Wrapper (Recommended)

```powershell
cd D:\code\AICode\transaction

# Clean and build
.\mvnw clean package -DskipTests

# Run with MySQL profile
.\mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Alternative: Build JAR and Run

```powershell
cd D:\code\AICode\transaction

# Build
.\mvnw clean package -DskipTests

# Run JAR
java -jar target\transaction-training-1.0.0.jar --spring.profiles.active=mysql
```

### Expected Output

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.1)

2026-01-10 09:30:00.000  INFO --- [main] TransactionTrainingApplication : Starting TransactionTrainingApplication
2026-01-10 09:30:01.000  INFO --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
2026-01-10 09:30:01.000  INFO --- [main] TransactionTrainingApplication : Started TransactionTrainingApplication in 3.5 seconds
```

### Verify Backend

Open new PowerShell terminal:

```powershell
# Test API
Invoke-RestMethod -Uri "http://localhost:8080/api/database/status" | ConvertTo-Json

# Check Swagger UI in browser
Start-Process "http://localhost:8080/swagger-ui.html"
```

---

## Step 5: Start Frontend Application

### Install Dependencies

Open **new PowerShell terminal**:

```powershell
cd D:\code\AICode\transaction\frontend

# Install Node.js dependencies
npm install
```

### Start Development Server

```powershell
# Start Vite dev server
npm run dev
```

### Expected Output

```
  VITE v5.0.11  ready in 500 ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
  ➜  press h to show help
```

### Open Application

```powershell
# Open in default browser
Start-Process "http://localhost:3000"
```

---

## Step 6: Verify Complete Setup

### Check All Services

```powershell
# MySQL
Get-Service MySQL80
# Status: Running

# Backend
Invoke-RestMethod -Uri "http://localhost:8080/api/database/status"
# Should return database info

# Frontend
Start-Process "http://localhost:3000"
# Should open web interface
```

### Access Points

| Service | URL | Description |
|---------|-----|-------------|
| Frontend | http://localhost:3000 | Main web interface |
| Backend API | http://localhost:8080 | REST API endpoints |
| Swagger UI | http://localhost:8080/swagger-ui.html | API documentation |
| MySQL | localhost:3306 | Database (txdemo) |

---

## Step 7: Database Schema Initialization

### Automatic Schema Creation

Flyway automatically runs migrations on startup. Verify:

```powershell
# Connect to database
mysql -u txdemo_user -p txdemo
# Password: txdemo_pass
```

```sql
-- Check tables
SHOW TABLES;
/*
Expected output:
+---------------------------+
| Tables_in_txdemo         |
+---------------------------+
| account                  |
| flyway_schema_history    |
| inventory                |
| mvcc_version_chain       |
| orders                   |
| redo_log_simulation      |
| transaction_log          |
| undo_log_simulation      |
+---------------------------+
*/

-- Verify sample data
SELECT * FROM account;
/*
Expected 5 accounts:
Alice Johnson   - 1000.00
Bob Smith       - 2000.00
Charlie Brown   - 1500.00
Diana Prince    - 3000.00
Eve Wilson      - 500.00
*/

-- Verify inventory
SELECT * FROM inventory;
/*
Expected 7 products with quantities
*/

EXIT;
```

### Manual Schema Verification (if needed)

If tables are missing, check Flyway status:

```sql
-- Check migration history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Should show:
-- V1__Create_base_tables.sql - Success
-- V2__Insert_sample_data.sql - Success
```

---

## Troubleshooting

### Issue: Backend Won't Start

```powershell
# Check Java version
java -version
# Must be 17 or higher

# Check if port 8080 is already in use
netstat -ano | findstr :8080

# Kill process if needed (replace PID)
Stop-Process -Id <PID> -Force

# Check MySQL connection
mysql -u txdemo_user -p txdemo
# Must connect successfully
```

### Issue: Frontend Won't Start

```powershell
# Check Node version
node --version
# Must be 18 or higher

# Clear cache and reinstall
cd D:\code\AICode\transaction\frontend
Remove-Item node_modules -Recurse -Force
Remove-Item package-lock.json -Force
npm install

# Check if port 3000 is in use
netstat -ano | findstr :3000
```

### Issue: Flyway Migration Fails

```sql
-- Check flyway_schema_history
mysql -u txdemo_user -p txdemo

SELECT * FROM flyway_schema_history;

-- If corrupted, repair:
DELETE FROM flyway_schema_history WHERE success = 0;

-- Drop and recreate database
DROP DATABASE txdemo;
CREATE DATABASE txdemo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON txdemo.* TO 'txdemo_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;

-- Restart backend to re-run migrations
```

### Issue: Cannot Connect to Database

```powershell
# Check MySQL service
Get-Service MySQL80

# Start if stopped
Start-Service MySQL80

# Test connection
mysql -u txdemo_user -p -h localhost txdemo

# Check firewall
Test-NetConnection -ComputerName localhost -Port 3306
```

---

## Complete Startup Script

Save as `start-local.ps1`:

```powershell
# Transaction Training System - Local Startup Script

Write-Host "=== Starting Transaction Training System ===" -ForegroundColor Green

# Check MySQL
Write-Host "`n1. Checking MySQL..." -ForegroundColor Yellow
$mysqlService = Get-Service MySQL80 -ErrorAction SilentlyContinue
if ($mysqlService.Status -ne 'Running') {
    Write-Host "Starting MySQL service..." -ForegroundColor Yellow
    Start-Service MySQL80
    Start-Sleep -Seconds 5
}
Write-Host "✓ MySQL is running" -ForegroundColor Green

# Start Backend
Write-Host "`n2. Starting Backend..." -ForegroundColor Yellow
$backendJob = Start-Job -ScriptBlock {
    Set-Location "D:\code\AICode\transaction"
    .\mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
}
Write-Host "✓ Backend starting (Job ID: $($backendJob.Id))" -ForegroundColor Green
Write-Host "Waiting for backend to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Test Backend
Write-Host "`n3. Testing Backend..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/database/status" -ErrorAction Stop
    Write-Host "✓ Backend is responding" -ForegroundColor Green
} catch {
    Write-Host "✗ Backend not ready yet, check logs" -ForegroundColor Red
}

# Start Frontend
Write-Host "`n4. Starting Frontend..." -ForegroundColor Yellow
$frontendJob = Start-Job -ScriptBlock {
    Set-Location "D:\code\AICode\transaction\frontend"
    npm run dev
}
Write-Host "✓ Frontend starting (Job ID: $($frontendJob.Id))" -ForegroundColor Green
Start-Sleep -Seconds 10

# Open Browser
Write-Host "`n5. Opening Application..." -ForegroundColor Yellow
Start-Process "http://localhost:3000"
Start-Process "http://localhost:8080/swagger-ui.html"

Write-Host "`n=== System Started Successfully ===" -ForegroundColor Green
Write-Host "`nAccess Points:" -ForegroundColor Cyan
Write-Host "  Frontend:   http://localhost:3000" -ForegroundColor White
Write-Host "  Backend:    http://localhost:8080" -ForegroundColor White
Write-Host "  Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "`nJob IDs:" -ForegroundColor Cyan
Write-Host "  Backend:  $($backendJob.Id)" -ForegroundColor White
Write-Host "  Frontend: $($frontendJob.Id)" -ForegroundColor White
Write-Host "`nTo stop services:" -ForegroundColor Cyan
Write-Host "  Stop-Job -Id $($backendJob.Id), $($frontendJob.Id)" -ForegroundColor White
Write-Host "  Remove-Job -Id $($backendJob.Id), $($frontendJob.Id)" -ForegroundColor White
```

### Run Startup Script

```powershell
# Make script executable (if needed)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Run script
.\start-local.ps1
```

---

## Shutdown Script

Save as `stop-local.ps1`:

```powershell
# Transaction Training System - Local Shutdown Script

Write-Host "=== Stopping Transaction Training System ===" -ForegroundColor Yellow

# Get all Spring Boot processes
$springProcesses = Get-Process java -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*spring-boot*" -or $_.CommandLine -like "*transaction-training*"
}

if ($springProcesses) {
    Write-Host "Stopping backend processes..." -ForegroundColor Yellow
    $springProcesses | Stop-Process -Force
    Write-Host "✓ Backend stopped" -ForegroundColor Green
}

# Get all Node processes (Vite)
$nodeProcesses = Get-Process node -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*vite*"
}

if ($nodeProcesses) {
    Write-Host "Stopping frontend processes..." -ForegroundColor Yellow
    $nodeProcesses | Stop-Process -Force
    Write-Host "✓ Frontend stopped" -ForegroundColor Green
}

# Stop background jobs
$jobs = Get-Job | Where-Object { $_.State -eq 'Running' }
if ($jobs) {
    Write-Host "Stopping background jobs..." -ForegroundColor Yellow
    $jobs | Stop-Job
    $jobs | Remove-Job
    Write-Host "✓ Jobs stopped" -ForegroundColor Green
}

Write-Host "`n=== System Stopped ===" -ForegroundColor Green
```

### Run Shutdown

```powershell
.\stop-local.ps1
```

---

## VS Code Integration

### Launch Configuration

Create `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot (MySQL)",
      "request": "launch",
      "cwd": "${workspaceFolder}",
      "mainClass": "com.transaction.training.TransactionTrainingApplication",
      "projectName": "transaction-training",
      "args": "--spring.profiles.active=mysql",
      "envFile": "${workspaceFolder}/.env"
    }
  ]
}
```

### Tasks Configuration

Create `.vscode/tasks.json`:

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Start Backend",
      "type": "shell",
      "command": ".\\mvnw spring-boot:run -Dspring-boot.run.profiles=mysql",
      "problemMatcher": [],
      "presentation": {
        "reveal": "always",
        "panel": "dedicated"
      }
    },
    {
      "label": "Start Frontend",
      "type": "shell",
      "command": "npm run dev",
      "options": {
        "cwd": "${workspaceFolder}/frontend"
      },
      "problemMatcher": [],
      "presentation": {
        "reveal": "always",
        "panel": "dedicated"
      }
    },
    {
      "label": "Start All",
      "dependsOn": ["Start Backend", "Start Frontend"],
      "problemMatcher": []
    }
  ]
}
```

### Run from VS Code

1. Open VS Code: `code D:\code\AICode\transaction`
2. Press `Ctrl+Shift+P`
3. Type: "Tasks: Run Task"
4. Select: "Start All"

---

## Quick Reference

### Daily Startup

```powershell
# Terminal 1 - Backend
cd D:\code\AICode\transaction
.\mvnw spring-boot:run -Dspring-boot.run.profiles=mysql

# Terminal 2 - Frontend
cd D:\code\AICode\transaction\frontend
npm run dev

# Open browser
Start-Process "http://localhost:3000"
```

### Reset Demo Data

```powershell
# Via API
Invoke-RestMethod -Uri "http://localhost:8080/api/demo/reset" -Method POST

# Or via UI
# Dashboard → Quick Actions → Reset Demo Data
```

### View Logs

```powershell
# Backend logs: Terminal output
# Or check: D:\code\AICode\transaction\logs\

# Frontend logs: Terminal output
```

---

**Setup complete! Access the application at http://localhost:3000 and start training.**
