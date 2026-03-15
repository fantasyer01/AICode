# AI 往昔录 - One-Click Deployment Script
# Builds JAR locally and deploys to Aliyun ECS server via SSH

param(
    [string]$SshHost = "aliyun",
    [string]$RemotePath = "/home/app/ai-blog",
    [string]$BackupDir = "/home/app/backups",
    [string]$JarName = "ai-blog-1.0.0.jar",
    [switch]$SkipBuild,
    [switch]$SkipBackup,
    [switch]$DryRun
)

# Fix console encoding for Chinese characters
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$ErrorActionPreference = "Stop"

# Configuration
$ProjectRoot = (Get-Item $PSScriptRoot).Parent.FullName
$TargetDir = Join-Path $ProjectRoot "target"
$JarPath = Join-Path $TargetDir $JarName
$RemoteJarPath = "$RemotePath/$JarName"
$HealthCheckUrl = "http://localhost:9100/actuator/health"
$ServiceName = "ai-blog"

function Write-Step {
    param([string]$Message, [string]$Status = "INFO")
    $color = switch ($Status) {
        "INFO"    { "Cyan" }
        "SUCCESS" { "Green" }
        "WARNING" { "Yellow" }
        "ERROR"   { "Red" }
        default   { "White" }
    }
    $timestamp = Get-Date -Format "HH:mm:ss"
    Write-Host "[$timestamp] [$Status] $Message" -ForegroundColor $color
}

function Test-SshConnection {
    Write-Step "Testing SSH connection to $SshHost..."
    try {
        $result = ssh $SshHost "echo 'SSH_OK'" 2>&1
        if ($result -match "SSH_OK") {
            Write-Step "SSH connection successful" "SUCCESS"
            return $true
        }
    } catch {
        Write-Step "SSH connection failed: $_" "ERROR"
    }
    return $false
}

# Banner
Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "  AI Blog - JAR Deployment" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""
Write-Step "Project: $ProjectRoot"
Write-Step "Target: $SshHost`:$RemotePath"
Write-Host ""

if ($DryRun) {
    Write-Step "DRY RUN MODE - No changes will be made" "WARNING"
    Write-Host ""
}

# Step 0: Test SSH connection
if (-not (Test-SshConnection)) {
    Write-Step "Cannot connect to $SshHost. Please check your SSH config." "ERROR"
    exit 1
}

# Step 1: Build JAR
if (-not $SkipBuild) {
    Write-Step "Step 1/5: Building JAR with Maven..."

    if (-not $DryRun) {
        # Check Maven is available
        try {
            $mvnVersion = & mvn --version 2>&1 | Select-Object -First 1
            Write-Step "Maven: $mvnVersion"
        } catch {
            Write-Step "Maven is not installed or not in PATH" "ERROR"
            Write-Step "Please install Maven: https://maven.apache.org/download.cgi" "WARNING"
            exit 1
        }

        # Run Maven build
        Push-Location $ProjectRoot
        try {
            & mvn clean package -DskipTests
            if ($LASTEXITCODE -ne 0) {
                Write-Step "Maven build failed!" "ERROR"
                exit 1
            }
        } finally {
            Pop-Location
        }

        # Verify JAR file exists
        if (-not (Test-Path $JarPath)) {
            $foundJar = Get-ChildItem -Path $TargetDir -Filter "ai-blog-*.jar" -Exclude "*-sources.jar","*-javadoc.jar" | Select-Object -First 1
            if ($foundJar) {
                $JarPath = $foundJar.FullName
                $JarName = $foundJar.Name
                $RemoteJarPath = "$RemotePath/$JarName"
            } else {
                Write-Step "JAR file not found in $TargetDir" "ERROR"
                exit 1
            }
        }

        $JarSize = (Get-Item $JarPath).Length / 1MB
        Write-Step "JAR built: $JarName ($([math]::Round($JarSize, 2)) MB)" "SUCCESS"
    } else {
        Write-Step "[DryRun] Would build JAR with Maven" "WARNING"
    }
} else {
    Write-Step "Step 1/5: Skipping build (--SkipBuild)" "WARNING"
}

# Find JAR file (handle version changes)
if (-not (Test-Path $JarPath)) {
    $foundJar = Get-ChildItem -Path $TargetDir -Filter "ai-blog-*.jar" -Exclude "*-sources.jar","*-javadoc.jar" | Select-Object -First 1
    if ($foundJar) {
        $JarPath = $foundJar.FullName
        $JarName = $foundJar.Name
        $RemoteJarPath = "$RemotePath/$JarName"
    } else {
        Write-Step "JAR file not found in $TargetDir" "ERROR"
        exit 1
    }
}

Write-Step "JAR file: $JarName"

# Step 2: Upload JAR to server
Write-Step "Step 2/5: Uploading JAR to server..."
if (-not $DryRun) {
    scp $JarPath "${SshHost}:/tmp/$JarName"
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Upload failed!" "ERROR"
        exit 1
    }
}
Write-Step "JAR uploaded to /tmp/$JarName" "SUCCESS"

# Step 3: Backup current deployment
if (-not $SkipBackup) {
    Write-Step "Step 3/5: Creating backup on server..."
    $backupCmd = @"
mkdir -p $BackupDir
if [ -f "$RemoteJarPath" ]; then
    BACKUP_NAME="${ServiceName}-backup-`$(date +%Y%m%d-%H%M%S).jar"
    cp "$RemoteJarPath" "$BackupDir/`$BACKUP_NAME"
    echo "Backup created: `$BACKUP_NAME"
    # Keep only last 5 backups
    ls -t $BackupDir/${ServiceName}-backup-*.jar 2>/dev/null | tail -n +6 | xargs -r rm
fi
"@
    $backupCmd = $backupCmd -replace "`r", ""
    if (-not $DryRun) {
        $result = ssh $SshHost $backupCmd
        Write-Host $result
    }
    Write-Step "Backup completed" "SUCCESS"
} else {
    Write-Step "Step 3/5: Skipping backup (--SkipBackup)" "WARNING"
}

# Step 4: Stop old service, deploy new JAR, and start
Write-Step "Step 4/5: Deploying and restarting service..."
$deployCmd = @"
mkdir -p $RemotePath
mkdir -p $RemotePath/logs
mkdir -p $RemotePath/data

# Stop current running instance
PID=`$(pgrep -f "$JarName" 2>/dev/null | grep -v pgrep | head -1)
if [ -n "`$PID" ]; then
    echo "Stopping running instance, PID: `$PID ..."
    kill `$PID
    sleep 3
    # Force kill if still running
    if kill -0 `$PID 2>/dev/null; then
        echo "Force killing PID `$PID ..."
        kill -9 `$PID
        sleep 1
    fi
    echo "Service stopped"
else
    echo "No running instance found"
fi

# Deploy new JAR
mv /tmp/$JarName $RemoteJarPath
chmod +x $RemoteJarPath

# Start the service
cd $RemotePath
nohup java -jar $RemoteJarPath \
    --spring.profiles.active=prod \
    --server.port=9100 \
    > $RemotePath/logs/startup.log 2>&1 &

NEW_PID=`$!
echo "Service started with PID: `$NEW_PID"
echo "Deploy path: $RemotePath"
"@
$deployCmd = $deployCmd -replace "`r", ""
if (-not $DryRun) {
    ssh $SshHost $deployCmd
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Deployment failed!" "ERROR"
        exit 1
    }
}
Write-Step "Service deployed and started" "SUCCESS"

# Step 5: Health check
Write-Step "Step 5/5: Verifying deployment..."
Write-Step "Waiting 15 seconds for service to start..."
if (-not $DryRun) {
    Start-Sleep -Seconds 15
}

$healthCmd = @"
MAX_RETRIES=6
RETRY_COUNT=0
while [ `$RETRY_COUNT -lt `$MAX_RETRIES ]; do
    RESPONSE=`$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9100/ 2>/dev/null)
    if [ "`$RESPONSE" = "200" ] || [ "`$RESPONSE" = "302" ]; then
        echo "HEALTH_OK"
        exit 0
    fi
    RETRY_COUNT=`$((RETRY_COUNT + 1))
    echo "Retry `$RETRY_COUNT/`$MAX_RETRIES [HTTP `$RESPONSE]..."
    sleep 5
done
echo "HEALTH_FAILED"
exit 1
"@
$healthCmd = $healthCmd -replace "`r", ""

if (-not $DryRun) {
    $healthResult = ssh $SshHost $healthCmd
    Write-Host $healthResult

    if ($healthResult -match "HEALTH_OK") {
        Write-Step "Health check passed!" "SUCCESS"
    } else {
        Write-Step "Health check failed! Check logs on server: $RemotePath/logs/" "ERROR"
        Write-Step "Rolling back to previous JAR..." "WARNING"

        # Attempt rollback
        $rollbackCmd = @"
cd $BackupDir
LATEST_BACKUP=`$(ls -t ${ServiceName}-backup-*.jar 2>/dev/null | head -1)
if [ -n "`$LATEST_BACKUP" ]; then
    PID=`$(pgrep -f "$JarName" 2>/dev/null)
    if [ -n "`$PID" ]; then
        kill `$PID
        sleep 3
    fi
    cp "$BackupDir/`$LATEST_BACKUP" "$RemoteJarPath"
    cd $RemotePath
    nohup java -jar $RemoteJarPath \
        --spring.profiles.active=prod \
        --server.port=9100 \
        > $RemotePath/logs/startup.log 2>&1 &
    echo "Rolled back to: `$LATEST_BACKUP"
fi
"@
        $rollbackCmd = $rollbackCmd -replace "`r", ""
        ssh $SshHost $rollbackCmd
        exit 1
    }
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Deployment Successful!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Step "Application URL: http://<server-ip>:9100"
Write-Step "Logs: $RemotePath/logs/"
Write-Step "Data: $RemotePath/data/"
Write-Host ""
