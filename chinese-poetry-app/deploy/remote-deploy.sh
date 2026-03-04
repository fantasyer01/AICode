#!/bin/bash
# Chinese Poetry App - Remote Deployment Script
# Run this on the Aliyun server

set -e

# Configuration
DEPLOY_PATH="/home/app/chinese-poetry-app"
BACKUP_DIR="/home/app/backups"
PACKAGE_FILE="/tmp/chinese-poetry-app.zip"
HEALTH_CHECK_URL="http://localhost:9000/test"

log() {
    echo "[$(date '+%H:%M:%S')] $1"
}

error() {
    echo "[$(date '+%H:%M:%S')] ERROR: $1" >&2
    exit 1
}

# Check if package exists
if [ ! -f "$PACKAGE_FILE" ]; then
    error "Package file not found: $PACKAGE_FILE"
fi

log "Starting deployment..."

# Step 1: Create backup
log "Creating backup..."
mkdir -p "$BACKUP_DIR"
if [ -d "$DEPLOY_PATH" ]; then
    BACKUP_NAME="backup-$(date +%Y%m%d-%H%M%S).tar.gz"
    cd "$(dirname $DEPLOY_PATH)"
    tar -czf "$BACKUP_DIR/$BACKUP_NAME" "$(basename $DEPLOY_PATH)"
    log "Backup created: $BACKUP_NAME"
    
    # Keep only last 5 backups
    ls -t "$BACKUP_DIR"/*.tar.gz 2>/dev/null | tail -n +6 | xargs -r rm
fi

# Step 2: Stop current service
log "Stopping current service..."
if [ -d "$DEPLOY_PATH" ]; then
    cd "$DEPLOY_PATH"
    docker compose down 2>/dev/null || true
fi

# Step 3: Deploy new code (preserve persistent data: logs, static/images)
log "Deploying new code..."
mkdir -p "$(dirname $DEPLOY_PATH)"
cd "$(dirname $DEPLOY_PATH)"

# Preserve persistent data before removing old code
if [ -d "$DEPLOY_PATH/logs" ]; then
    mv "$DEPLOY_PATH/logs" /tmp/_deploy_preserve_logs
fi
if [ -d "$DEPLOY_PATH/static/images" ]; then
    mv "$DEPLOY_PATH/static/images" /tmp/_deploy_preserve_images
fi

rm -rf "$(basename $DEPLOY_PATH)"
unzip -q "$PACKAGE_FILE" -d "$(basename $DEPLOY_PATH)"
rm "$PACKAGE_FILE"

# Restore persistent data
if [ -d /tmp/_deploy_preserve_logs ]; then
    rm -rf "$DEPLOY_PATH/logs"
    mv /tmp/_deploy_preserve_logs "$DEPLOY_PATH/logs"
fi
if [ -d /tmp/_deploy_preserve_images ]; then
    mkdir -p "$DEPLOY_PATH/static"
    rm -rf "$DEPLOY_PATH/static/images"
    mv /tmp/_deploy_preserve_images "$DEPLOY_PATH/static/images"
fi

log "Code extracted to $DEPLOY_PATH"

# Step 4: Build and start Docker
log "Building Docker image..."
cd "$DEPLOY_PATH"
docker compose build

log "Starting containers..."
docker compose up -d

# Step 5: Health check
log "Waiting for service to start..."
sleep 10

MAX_RETRIES=5
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_CHECK_URL" 2>/dev/null || echo "000")
    if [ "$RESPONSE" = "200" ]; then
        log "Health check passed!"
        docker compose ps
        echo ""
        echo "========================================"
        echo "  Deployment Successful!"
        echo "========================================"
        exit 0
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    log "Health check retry $RETRY_COUNT/$MAX_RETRIES (HTTP $RESPONSE)..."
    sleep 3
done

# Health check failed - attempt rollback
log "Health check failed! Attempting rollback..."

LATEST_BACKUP=$(ls -t "$BACKUP_DIR"/*.tar.gz 2>/dev/null | head -1)
if [ -n "$LATEST_BACKUP" ]; then
    cd "$DEPLOY_PATH"
    docker compose down
    cd "$(dirname $DEPLOY_PATH)"
    rm -rf "$(basename $DEPLOY_PATH)"
    tar -xzf "$LATEST_BACKUP"
    cd "$DEPLOY_PATH"
    docker compose up -d
    log "Rolled back to: $(basename $LATEST_BACKUP)"
    error "Deployment failed, rolled back to previous version"
else
    error "Deployment failed and no backup available for rollback"
fi
