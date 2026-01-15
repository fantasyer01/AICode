# Docker Deployment Guide

This document describes how to deploy the Moment application using Docker.

Supports both **Windows** and **Linux** platforms.

## Prerequisites

### Windows

- **Docker Desktop for Windows** 4.0+ (includes Docker Engine and Docker Compose)
- **WSL 2** (Windows Subsystem for Linux 2) - recommended backend for Docker Desktop
- Windows 10 version 2004+ or Windows 11

**Installation:**
1. Enable WSL 2: Open PowerShell as Administrator and run:
   ```powershell
   wsl --install
   ```
2. Download and install [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
3. In Docker Desktop Settings, ensure "Use WSL 2 based engine" is enabled

### Linux

- **Docker Engine** 20.10+
- **Docker Compose** 2.0+ (or docker-compose-plugin)

**Installation (Ubuntu/Debian):**
```bash
# Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Install Docker Compose plugin
sudo apt-get update
sudo apt-get install docker-compose-plugin

# Verify installation
docker --version
docker compose version
```

**Installation (CentOS/RHEL):**
```bash
# Install Docker
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
```

## Project Structure

```
moment/
├── backend/
│   ├── Dockerfile          # Backend Docker image definition
│   └── .dockerignore       # Files to exclude from backend image
├── frontend/
│   ├── Dockerfile          # Frontend Docker image definition
│   └── .dockerignore       # Files to exclude from frontend image
├── docker-compose.yml      # Multi-container orchestration
├── .env.example            # Environment variables template
├── .gitattributes          # Ensures LF line endings for Docker files
└── DOCKER_DEPLOYMENT.md    # This file
```

## Quick Start

### 1. Create Environment File

**Windows (PowerShell):**
```powershell
Copy-Item .env.example .env
```

**Windows (Command Prompt):**
```cmd
copy .env.example .env
```

**Linux/macOS:**
```bash
cp .env.example .env
```

Edit `.env` and set your values:

```bash
# MySQL Configuration
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_DATABASE=portfolio
MYSQL_USER=portfolio
MYSQL_PASSWORD=your_secure_password

# Backend Configuration
ALLOWED_ORIGINS=http://localhost:3000

# Frontend Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 2. Build and Start All Services

```bash
docker-compose up -d --build
```

### 3. Verify Services

Check all containers are running:

```bash
docker-compose ps
```

Expected output:
```
NAME              STATUS    PORTS
moment-mysql      Up        0.0.0.0:3306->3306/tcp
moment-backend    Up        0.0.0.0:8080->8080/tcp
moment-frontend   Up        0.0.0.0:3000->3000/tcp
```

### 4. Access the Application

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api/projects
- Health Check: http://localhost:8080/actuator/health

## Individual Service Operations

### Build Only

```bash
# Build all services
docker-compose build

# Build specific service
docker-compose build backend
docker-compose build frontend
```

### Start/Stop Services

```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d backend

# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes database data)
docker-compose down -v
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### Restart Services

```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart backend
```

## Production Deployment

### 1. Update Environment Variables

For production, ensure you set secure passwords and update the CORS origins:

```bash
MYSQL_ROOT_PASSWORD=<strong_password>
MYSQL_PASSWORD=<strong_password>
ALLOWED_ORIGINS=https://your-domain.com
NEXT_PUBLIC_API_URL=https://api.your-domain.com
```

### 2. Use Production-Ready Configuration

Create `docker-compose.prod.yml`:

```yaml
services:
  mysql:
    restart: always
    
  backend:
    restart: always
    environment:
      SPRING_PROFILES_ACTIVE: prod
      
  frontend:
    restart: always
```

Deploy with:

```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

### 3. Add Reverse Proxy (Recommended)

For production, add nginx as a reverse proxy. Create `nginx.conf`:

```nginx
upstream frontend {
    server frontend:3000;
}

upstream backend {
    server backend:8080;
}

server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://frontend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    location /api {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## Database Management

### Access MySQL CLI

```bash
docker-compose exec mysql mysql -u portfolio -p
```

### Backup Database

```bash
docker-compose exec mysql mysqldump -u portfolio -p portfolio > backup.sql
```

### Restore Database

```bash
docker-compose exec -T mysql mysql -u portfolio -p portfolio < backup.sql
```

## Troubleshooting

### Windows-Specific Issues

#### Docker Desktop Not Starting
1. Ensure WSL 2 is properly installed: `wsl --status`
2. Check Windows features are enabled:
   - Virtual Machine Platform
   - Windows Subsystem for Linux
3. Restart Docker Desktop

#### Line Ending Issues (CRLF vs LF)
If you encounter errors like `standard_init_linux.go: exec format error`:
```powershell
# Convert files to LF line endings
git config --global core.autocrlf input

# Re-clone or reset the repository
git rm --cached -r .
git reset --hard
```

#### Slow Performance on Windows
1. Store project files in WSL filesystem for better I/O:
   ```powershell
   # Access WSL filesystem
   cd \\wsl$\Ubuntu\home\username\projects
   ```
2. In Docker Desktop Settings > Resources > WSL Integration, enable your WSL distro

#### Port Already in Use
```powershell
# Find process using port
netstat -ano | findstr :3000
# Kill process by PID
taskkill /PID <pid> /F
```

### Linux-Specific Issues

#### Permission Denied
```bash
# Add user to docker group
sudo usermod -aG docker $USER
# Log out and back in, or run:
newgrp docker
```

#### Port Already in Use
```bash
# Find process using port
sudo lsof -i :3000
# Or
sudo netstat -tlnp | grep 3000
# Kill process
sudo kill -9 <pid>
```

### General Issues

### Container Won't Start

Check logs for errors:

```bash
docker-compose logs <service-name>
```

### Backend Cannot Connect to MySQL

1. Ensure MySQL is healthy: `docker-compose ps`
2. Check if MySQL is ready before backend starts
3. Verify database credentials match in `.env`

### Frontend Shows Connection Errors

1. Verify `NEXT_PUBLIC_API_URL` is correct
2. Check CORS configuration in `ALLOWED_ORIGINS`
3. Ensure backend is running and healthy

### Port Conflicts

If ports are in use, modify `docker-compose.yml`:

```yaml
services:
  frontend:
    ports:
      - "3001:3000"  # Change host port
```

### Clean Rebuild

```bash
# Remove all containers, images, and volumes
docker-compose down -v --rmi all

# Rebuild from scratch
docker-compose up -d --build
```

## Resource Management

### View Resource Usage

```bash
docker stats
```

### Limit Container Resources

Add to `docker-compose.yml`:

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
```
