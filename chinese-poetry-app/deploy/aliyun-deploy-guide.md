# Chinese Poetry App - Alibaba Cloud Docker Deployment Guide

> Target: Alibaba Cloud ECS (CentOS/Alibaba Cloud Linux), only Java environment pre-installed

---

## Step 1: Install Docker on Alibaba Cloud Server

SSH into your server:

```bash
ssh root@<your-server-ip>
```

Install Docker:

```bash
# Install dependencies
sudo yum install -y yum-utils device-mapper-persistent-data lvm2

# Add Docker repository (Alibaba Cloud mirror for faster download in China)
sudo yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

# Install Docker
sudo yum install -y docker-ce docker-ce-cli containerd.io

# Start Docker and enable auto-start
sudo systemctl start docker
sudo systemctl enable docker

# Verify installation
docker --version
```

Configure Docker mirror (significantly speeds up image pulls in China):

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://docker.m.daocloud.io"
  ]
}
EOF

sudo systemctl daemon-reload
sudo systemctl restart docker
```

Install Docker Compose:

```bash
# Download Docker Compose (use DaoCloud mirror for speed)
sudo curl -L "https://get.daocloud.io/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Grant execute permission
sudo chmod +x /usr/local/bin/docker-compose

# Verify
docker-compose --version
```

---

## Step 2: Transfer Project to Server

### Method A: Git (Recommended)

On the server:

```bash
# Install Git (if not present)
sudo yum install -y git

# Clone the repository
cd /home
git clone <your-git-repo-url> chinese-poetry-app
cd chinese-poetry-app
```

### Method B: SCP from Local Machine

On your local Windows machine (PowerShell):

```powershell
# Transfer the entire project
scp -r D:\code\AICode\chinese-poetry-app root@<your-server-ip>:/home/chinese-poetry-app
```

Or use the existing packaging script:

```powershell
# Run the packaging script first
powershell -ExecutionPolicy Bypass -File .\deploy\package-for-deployment.ps1

# Transfer the zip
scp chinese-poetry-app.zip root@<your-server-ip>:/home/
```

Then on the server:

```bash
cd /home
unzip chinese-poetry-app.zip -d chinese-poetry-app
```

---

## Step 3: Configure Environment Variable

Set the `DEEPSEEK_API_KEY` environment variable on the server:

```bash
# Add to /etc/profile for system-wide persistence
echo 'export DEEPSEEK_API_KEY="sk-xxxxxxxxxxxxxxxxxxxxxxxx"' | sudo tee -a /etc/profile

# Load immediately
source /etc/profile

# Verify
echo $DEEPSEEK_API_KEY
```

Alternatively, add to `~/.bashrc` for current user only:

```bash
echo 'export DEEPSEEK_API_KEY="sk-xxxxxxxxxxxxxxxxxxxxxxxx"' >> ~/.bashrc
source ~/.bashrc
```

---

## Step 4: Build and Start the Application

```bash
cd /home/chinese-poetry-app

# Build the Docker image
docker-compose build

# Start the container (runs in background)
docker-compose up -d
```

Verify the application is running:

```bash
# Check container status
docker-compose ps

# Check application logs
docker-compose logs -f

# Test the application
curl http://localhost:9000/test
```

---

## Step 5: Configure Firewall

Open port 9000 on the server firewall:

```bash
# If using firewalld (CentOS default)
sudo firewall-cmd --permanent --add-port=9000/tcp
sudo firewall-cmd --reload

# Verify
sudo firewall-cmd --list-ports
```

Also configure in Alibaba Cloud Console:
1. Go to ECS Console -> Security Groups
2. Add an inbound rule: Protocol TCP, Port 9000, Source 0.0.0.0/0

Now visit `http://<your-server-ip>:9000` in your browser.

---

## Daily Operations

### View Logs

```bash
# Application logs (real-time)
docker-compose logs -f

# Application log files on host
tail -f /home/chinese-poetry-app/logs/app.log
tail -f /home/chinese-poetry-app/logs/error.log
```

### Restart Application

```bash
cd /home/chinese-poetry-app
docker-compose restart
```

### Update Code and Redeploy

```bash
cd /home/chinese-poetry-app

# If using Git
git pull

# Rebuild and restart
docker-compose up -d --build
```

### Stop Application

```bash
cd /home/chinese-poetry-app
docker-compose down
```

### Modify API Key

```bash
# Edit /etc/profile or ~/.bashrc
vi /etc/profile

# Reload environment
source /etc/profile

# Restart container to apply changes
cd /home/chinese-poetry-app
docker-compose restart
```

---

## Troubleshooting

### Container won't start

```bash
# Check detailed logs
docker-compose logs --tail=50

# Check container status
docker ps -a
```

### Port already in use

```bash
# Find what's using port 9000
sudo lsof -i :9000

# Or change the port in docker-compose.yml:
# ports:
#   - "8080:9000"   # Map to host port 8080 instead
```

### Permission issues with logs directory

```bash
# Fix permissions
sudo chmod -R 777 /home/chinese-poetry-app/logs
```

### Health check failing

```bash
# Check if the app responds inside the container
docker exec chinese-poetry-app curl -s http://localhost:9000/test
```

### API Key not passed to container

```bash
# Verify environment variable is set
echo $DEEPSEEK_API_KEY

# Check if container received the variable
docker exec chinese-poetry-app env | grep DEEPSEEK
```

---

## File Overview

```
chinese-poetry-app/
  Dockerfile              # Docker image definition
  docker-compose.yml      # Container orchestration config (reads DEEPSEEK_API_KEY from host)
  .dockerignore           # Files excluded from Docker image
  requirements.txt        # Python dependencies (incl. gunicorn, gevent)
  config/
    constants.py          # Reads DEEPSEEK_API_KEY from environment variable
```
