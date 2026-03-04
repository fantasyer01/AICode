# Chinese Poetry App - One-Click Deployment Script
# Deploys to Aliyun ECS server via SSH

param(
    [string]$SshHost = "aliyun",
    [string]$RemotePath = "/home/app/chinese-poetry-app",
    [string]$BackupDir = "/home/app/backups",
    [switch]$SkipPackage,
    [switch]$SkipBackup,
    [switch]$DryRun
)

# Fix console encoding for Chinese characters
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$ErrorActionPreference = "Stop"

# Configuration
$ProjectRoot = (Get-Item $PSScriptRoot).Parent.FullName
$PackageFile = "chinese-poetry-app.zip"
$PackagePath = Join-Path $ProjectRoot $PackageFile
$RemoteDeployScript = Join-Path $PSScriptRoot "remote-deploy.sh"
$HealthCheckUrl = "http://localhost:9000/test"

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
Write-Host "  Chinese Poetry App - Deployment" -ForegroundColor Magenta
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

# Step 1: Package the application
if (-not $SkipPackage) {
    Write-Step "Step 1/6: Packaging application..."
    
    $packageScript = Join-Path $ProjectRoot "deploy\package-for-deployment.ps1"
    if (Test-Path $packageScript) {
        if (-not $DryRun) {
            & powershell -ExecutionPolicy Bypass -File $packageScript
            if ($LASTEXITCODE -ne 0) {
                Write-Step "Packaging failed!" "ERROR"
                exit 1
            }
        }
        Write-Step "Package created: $PackageFile" "SUCCESS"
    } else {
        Write-Step "Package script not found: $packageScript" "ERROR"
        exit 1
    }
} else {
    Write-Step "Step 1/6: Skipping packaging (--SkipPackage)" "WARNING"
}

# Verify package exists
if (-not (Test-Path $PackagePath)) {
    Write-Step "Package file not found: $PackagePath" "ERROR"
    exit 1
}

# Step 2: Upload to server
Write-Step "Step 2/6: Uploading package to server..."
if (-not $DryRun) {
    scp $PackagePath "${SshHost}:/tmp/$PackageFile"
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Upload failed!" "ERROR"
        exit 1
    }
}
Write-Step "Package uploaded to /tmp/$PackageFile" "SUCCESS"

# Step 3: Backup current deployment
if (-not $SkipBackup) {
    Write-Step "Step 3/6: Creating backup on server..."
    $backupCmd = @"
mkdir -p $BackupDir
if [ -d "$RemotePath" ]; then
    BACKUP_NAME="backup-`$(date +%Y%m%d-%H%M%S).tar.gz"
    cd `$(dirname $RemotePath)
    tar -czf "$BackupDir/`$BACKUP_NAME" `$(basename $RemotePath)
    echo "Backup created: `$BACKUP_NAME"
    # Keep only last 5 backups
    ls -t $BackupDir/*.tar.gz 2>/dev/null | tail -n +6 | xargs -r rm
fi
"@
    $backupCmd = $backupCmd -replace "`r", ""
    if (-not $DryRun) {
        $result = ssh $SshHost $backupCmd
        Write-Host $result
    }
    Write-Step "Backup completed" "SUCCESS"
} else {
    Write-Step "Step 3/6: Skipping backup (--SkipBackup)" "WARNING"
}

# Step 4: Deploy new code (preserve persistent data: logs, static/images)
Write-Step "Step 4/6: Deploying new code..."
$deployCmd = @"
mkdir -p `$(dirname $RemotePath)
cd `$(dirname $RemotePath)
# Preserve persistent data before removing old code
if [ -d "$RemotePath/logs" ]; then
    mv "$RemotePath/logs" /tmp/_deploy_preserve_logs
fi
if [ -d "$RemotePath/static/images" ]; then
    mv "$RemotePath/static/images" /tmp/_deploy_preserve_images
fi
rm -rf `$(basename $RemotePath)
unzip -O UTF-8 -q /tmp/$PackageFile -d `$(basename $RemotePath)
rm /tmp/$PackageFile
# Restore persistent data
if [ -d /tmp/_deploy_preserve_logs ]; then
    rm -rf "$RemotePath/logs"
    mv /tmp/_deploy_preserve_logs "$RemotePath/logs"
fi
if [ -d /tmp/_deploy_preserve_images ]; then
    mkdir -p "$RemotePath/static"
    rm -rf "$RemotePath/static/images"
    mv /tmp/_deploy_preserve_images "$RemotePath/static/images"
fi
echo "Code deployed to $RemotePath"
"@
$deployCmd = $deployCmd -replace "`r", ""
if (-not $DryRun) {
    ssh $SshHost $deployCmd
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Deployment failed!" "ERROR"
        exit 1
    }
}
Write-Step "Code deployed successfully" "SUCCESS"

# Step 5: Build and restart Docker
Write-Step "Step 5/6: Building and restarting Docker containers..."
$dockerCmd = @"
cd $RemotePath
docker compose down 2>/dev/null || true
docker compose build --pull never
docker compose up -d
docker compose ps
"@
$dockerCmd = $dockerCmd -replace "`r", ""
if (-not $DryRun) {
    ssh -o ServerAliveInterval=30 -o ServerAliveCountMax=10 $SshHost $dockerCmd
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Docker restart failed!" "ERROR"
        exit 1
    }
}
Write-Step "Docker containers restarted" "SUCCESS"

# Step 6: Health check
Write-Step "Step 6/6: Verifying deployment..."
Write-Step "Waiting 10 seconds for service to start..."
if (-not $DryRun) {
    Start-Sleep -Seconds 10
}

$healthCmd = @"
MAX_RETRIES=5
RETRY_COUNT=0
while [ `$RETRY_COUNT -lt `$MAX_RETRIES ]; do
    RESPONSE=`$(curl -s -o /dev/null -w "%{http_code}" $HealthCheckUrl 2>/dev/null)
    if [ "`$RESPONSE" = "200" ]; then
        echo "HEALTH_OK"
        exit 0
    fi
    RETRY_COUNT=`$((RETRY_COUNT + 1))
    echo "Retry `$RETRY_COUNT/`$MAX_RETRIES [HTTP `$RESPONSE]..."
    sleep 3
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
        Write-Step "Health check failed! Rolling back..." "ERROR"
        
        # Attempt rollback
        $rollbackCmd = @"
cd $BackupDir
LATEST_BACKUP=`$(ls -t *.tar.gz 2>/dev/null | head -1)
if [ -n "`$LATEST_BACKUP" ]; then
    cd $RemotePath
    docker compose down
    cd `$(dirname $RemotePath)
    rm -rf `$(basename $RemotePath)
    tar -xzf "$BackupDir/`$LATEST_BACKUP"
    cd $RemotePath
    docker compose up -d
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
Write-Step "Application URL: http://<server-ip>:9000"
Write-Step "Health Check: $HealthCheckUrl"
Write-Host ""
