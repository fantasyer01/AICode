# Quick Deployment Reference

## One-click build and deploy

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\deploy.ps1
```

## Build JAR only (no deploy)

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy\deploy.ps1 -SkipBuild:$false
# Or manually: mvn clean package -DskipTests
```

## Manual upload and deploy on server

```bash
# Upload JAR from local machine
scp target/ai-blog-1.0.0.jar aliyun:/tmp/

# SSH to server and run remote deploy script
ssh aliyun
bash /tmp/remote-deploy.sh
# Or if the script is already on the server:
# bash /home/app/ai-blog/deploy/remote-deploy.sh
```

## Server-side service management

```bash
# Check if service is running
pgrep -f "ai-blog-1.0.0.jar"

# View logs
tail -f /home/app/ai-blog/logs/startup.log

# Stop service
kill $(pgrep -f "ai-blog-1.0.0.jar")

# Start service
cd /home/app/ai-blog
nohup java -Xms256m -Xmx512m -jar ai-blog-1.0.0.jar --spring.profiles.active=prod --server.port=9100 > logs/startup.log 2>&1 &
```

## Full setup guide

See [aliyun-deploy-guide.md](aliyun-deploy-guide.md)
