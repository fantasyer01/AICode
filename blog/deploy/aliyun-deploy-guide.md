# AI 往昔录 - Alibaba Cloud JAR Deployment Guide

> Target: Alibaba Cloud ECS (CentOS/Alibaba Cloud Linux), Java 17 JAR deployment (no Docker)
> Project: AI 往昔录

---

## Prerequisites

- Alibaba Cloud ECS instance
- Local machine with Maven and Java 17 installed
- SSH access configured (recommend adding SSH config alias `aliyun`)

---

## Step 1: Install Java 17 on Alibaba Cloud Server

SSH into your server:

```bash
ssh root@<your-server-ip>
```

Install Java 17:

```bash
# Option A: Install OpenJDK 17 via yum
sudo yum install -y java-17-openjdk java-17-openjdk-devel

# Option B: If yum repo doesn't have Java 17, manually install
# Download from https://adoptium.net/ or use Alibaba Dragonwell
cd /tmp
wget https://dragonwell.oss-cn-shanghai.aliyuncs.com/17.0.9.0.10%2B9/Alibaba_Dragonwell_Standard_17.0.9.0.10.9_x64_linux.tar.gz
sudo mkdir -p /usr/local/java
sudo tar -xzf Alibaba_Dragonwell_Standard_17*.tar.gz -C /usr/local/java/

# Add to PATH
echo 'export JAVA_HOME=/usr/local/java/jdk-17.0.9.0.10+9' | sudo tee -a /etc/profile
echo 'export PATH=$JAVA_HOME/bin:$PATH' | sudo tee -a /etc/profile
source /etc/profile
```

Verify installation:

```bash
java -version
# Should show Java 17.x
```

---

## Step 2: Configure SSH (Local Machine)

Add an SSH config entry for your Alibaba Cloud server (in `~/.ssh/config`):

```
Host aliyun
    HostName <your-server-ip>
    User root
    IdentityFile ~/.ssh/your-private-key
```

Test the connection:

```bash
ssh aliyun "echo 'SSH OK'"
```

---

## Step 3: Create Application Directory

On the server:

```bash
mkdir -p /home/app/ai-blog/logs
mkdir -p /home/app/ai-blog/data
mkdir -p /home/app/backups
```

---

## Step 4: Deploy (One-Click)

From your local Windows machine, run the one-click deployment script:

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\deploy.ps1
```

This will automatically:
1. Build the JAR locally with Maven
2. Upload the JAR to the server
3. Backup the current deployment
4. Stop the running instance
5. Deploy the new JAR and start the service
6. Run health checks

### Optional Parameters

```powershell
# Custom SSH host and remote path
.\deploy\deploy.ps1 -SshHost "my-aliyun" -RemotePath "/opt/ai-blog"

# Skip build (use existing JAR)
.\deploy\deploy.ps1 -SkipBuild

# Skip backup
.\deploy\deploy.ps1 -SkipBackup

# Dry run (preview only)
.\deploy\deploy.ps1 -DryRun
```

---

## Step 5: Configure Firewall

Open port 9100 on the server firewall:

```bash
# If using firewalld (CentOS default)
sudo firewall-cmd --permanent --add-port=9100/tcp
sudo firewall-cmd --reload

# Verify
sudo firewall-cmd --list-ports
```

Also configure in Alibaba Cloud Console:
1. Go to ECS Console -> Security Groups
2. Add an inbound rule: Protocol TCP, Port 9100, Source 0.0.0.0/0

Now visit `http://<your-server-ip>:9100` in your browser.

---

## Step 6: (Optional) Configure systemd Service

For production use, create a systemd service to auto-start on reboot:

```bash
sudo tee /etc/systemd/system/ai-blog.service <<-'EOF'
[Unit]
Description=AI 往昔录 Application
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/home/app/ai-blog
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /home/app/ai-blog/ai-blog-1.0.0.jar --spring.profiles.active=prod --server.port=9100
ExecStop=/bin/kill -TERM $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=append:/home/app/ai-blog/logs/startup.log
StandardError=append:/home/app/ai-blog/logs/startup.log

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable ai-blog
sudo systemctl start ai-blog
```

After configuring systemd, manage the service with:

```bash
sudo systemctl status ai-blog
sudo systemctl restart ai-blog
sudo systemctl stop ai-blog
journalctl -u ai-blog -f
```

---

## Daily Operations

### View Logs

```bash
# Application startup log
tail -f /home/app/ai-blog/logs/startup.log

# Application log files (if logback configured)
tail -f /home/app/ai-blog/logs/*.log
```

### Restart Service

```bash
# If using nohup (default)
PID=$(pgrep -f "ai-blog-1.0.0.jar")
kill $PID
cd /home/app/ai-blog
nohup java -Xms256m -Xmx512m -jar ai-blog-1.0.0.jar --spring.profiles.active=prod --server.port=9100 > logs/startup.log 2>&1 &

# If using systemd
sudo systemctl restart ai-blog
```

### Stop Service

```bash
# If using nohup
PID=$(pgrep -f "ai-blog-1.0.0.jar")
kill $PID

# If using systemd
sudo systemctl stop ai-blog
```

### Manual Rollback

```bash
cd /home/app/backups
# List available backups
ls -lt ai-blog-backup-*.jar

# Rollback to a specific backup
PID=$(pgrep -f "ai-blog-1.0.0.jar") && kill $PID
cp ai-blog-backup-YYYYMMDD-HHMMSS.jar /home/app/ai-blog/ai-blog-1.0.0.jar
cd /home/app/ai-blog
nohup java -Xms256m -Xmx512m -jar ai-blog-1.0.0.jar --spring.profiles.active=prod --server.port=9100 > logs/startup.log 2>&1 &
```

---

## Troubleshooting

### Service won't start

```bash
# Check startup log
cat /home/app/ai-blog/logs/startup.log

# Check if port 9100 is already in use
sudo lsof -i :9100
# or
sudo ss -tlnp | grep 9100
```

### Out of memory

```bash
# Increase JVM heap size in deploy.ps1 or remote-deploy.sh
# Change JAVA_OPTS to: -Xms512m -Xmx1024m
```

### Permission issues

```bash
# Fix ownership
sudo chown -R root:root /home/app/ai-blog
chmod +x /home/app/ai-blog/ai-blog-1.0.0.jar
```

### H2 Database issues

```bash
# Database file location
ls -la /home/app/ai-blog/data/

# If database is corrupted, remove and let Spring recreate
# WARNING: This will delete all data
rm /home/app/ai-blog/data/aiblog.*
```

---

## File Layout on Server

```
/home/app/ai-blog/
  ai-blog-1.0.0.jar          # Application JAR
  data/
    aiblog.mv.db              # H2 database file
    images/                   # Uploaded images
  logs/
    startup.log               # Application startup/runtime log

/home/app/backups/
  ai-blog-backup-*.jar        # Rolling backups (last 5 kept)
```
