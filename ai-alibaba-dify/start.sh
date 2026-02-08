#!/bin/bash

################################################################################
# AI Alibaba Dify Application Startup Script
# Description: Start the application with API keys passed as parameters
# Usage: ./start.sh [OPTIONS]
################################################################################

set -e

# Default values
PROFILE="prod"
PORT="8080"
JAR_FILE="target/ai-alibaba-dify-1.0.0.jar"
LOG_DIR="logs"
PID_FILE="application.pid"
LOG_FILE="${LOG_DIR}/startup.log"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

################################################################################
# Function: Print usage information
################################################################################
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

API Keys (command-line parameters or environment variables):
  --dashscope-api-key KEY        DashScope API Key (or env: SPRING_AI_DASHSCOPE_API_KEY)
  --deepseek-api-key KEY         DeepSeek API Key (or env: SPRING_AI_DEEPSEEK_API_KEY)
  --dify-default-api-key KEY     Dify Default App API Key (or env: DIFY_APPS_DEFAULT_API_KEY)
  --dify-default-app-id ID       Dify Default App ID (or env: DIFY_APPS_DEFAULT_APP_ID)
  --dify-cs-api-key KEY          Dify CS API Key (or env: DIFY_APPS_CUSTOMERSERVICE_API_KEY)
  --dify-cs-app-id ID            Dify CS App ID (or env: DIFY_APPS_CUSTOMERSERVICE_APP_ID)
  --dify-ts-api-key KEY          Dify TS API Key (or env: DIFY_APPS_TECHSUPPORT_API_KEY)
  --dify-ts-app-id ID            Dify TS App ID (or env: DIFY_APPS_TECHSUPPORT_APP_ID)
  --wecom-token TOKEN            WeCom Bot Token (or env: WECOM_BOT_TOKEN)
  --wecom-aes-key KEY            WeCom Bot AES Key (or env: WECOM_BOT_ENCODING_AES_KEY)
  --wecom-corp-id ID             WeCom Corp ID (or env: WECOM_BOT_CORP_ID)

Optional Settings:
  --profile PROFILE              Spring profile (dev/test/prod, default: prod)
  --port PORT                    Server port (default: 8080)
  --jar-file PATH                Path to JAR file (default: target/ai-alibaba-dify-1.0.0.jar)
  --log-dir DIR                  Log directory (default: logs)
  
Other Options:
  -h, --help                     Show this help message
  --stop                         Stop the running application
  --status                       Check application status
  --restart                      Restart the application

Priority:
  Command-line parameters > Environment variables > Configuration files
  
Examples:
  # Start with command-line parameters
  $0 --dify-default-api-key app-xxx --dify-default-app-id xxx-yyy-zzz

  # Start with environment variables (set in ~/.bashrc or /etc/environment)
  export DIFY_APPS_DEFAULT_API_KEY="app-xxx"
  export DIFY_APPS_DEFAULT_APP_ID="xxx-yyy-zzz"
  $0

  # Mixed: some from CLI, some from ENV
  export SPRING_AI_DASHSCOPE_API_KEY="sk-xxx"
  $0 --dify-default-api-key app-yyy --profile prod

  # Stop the application
  $0 --stop

  # Check status
  $0 --status

EOF
    exit 1
}

################################################################################
# Function: Print colored message
################################################################################
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

################################################################################
# Function: Check if application is running
################################################################################
is_running() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            return 0
        fi
    fi
    return 1
}

################################################################################
# Function: Stop the application
################################################################################
stop_application() {
    print_message "$YELLOW" "Stopping AI Alibaba Dify application..."
    
    if ! is_running; then
        print_message "$YELLOW" "Application is not running."
        return 0
    fi
    
    PID=$(cat "$PID_FILE")
    print_message "$YELLOW" "Sending SIGTERM to process $PID..."
    kill "$PID"
    
    # Wait for graceful shutdown (max 30 seconds)
    for i in {1..30}; do
        if ! ps -p "$PID" > /dev/null 2>&1; then
            print_message "$GREEN" "Application stopped successfully."
            rm -f "$PID_FILE"
            return 0
        fi
        sleep 1
    done
    
    # Force kill if still running
    if ps -p "$PID" > /dev/null 2>&1; then
        print_message "$YELLOW" "Forcing shutdown..."
        kill -9 "$PID"
        rm -f "$PID_FILE"
        print_message "$GREEN" "Application force stopped."
    fi
}

################################################################################
# Function: Show application status
################################################################################
show_status() {
    if is_running; then
        PID=$(cat "$PID_FILE")
        print_message "$GREEN" "Application is running (PID: $PID)"
        
        # Show process info
        ps -p "$PID" -o pid,ppid,cmd,%mem,%cpu,etime
        
        # Show recent logs
        if [ -f "${LOG_DIR}/application.log" ]; then
            print_message "$YELLOW" "\nRecent logs:"
            tail -n 20 "${LOG_DIR}/application.log"
        fi
    else
        print_message "$YELLOW" "Application is not running."
        [ -f "$PID_FILE" ] && rm -f "$PID_FILE"
    fi
}

################################################################################
# Function: Load API keys from environment variables if not set
################################################################################
load_env_variables() {
    # Load from environment variables if command-line parameters not provided
    [ -z "$DASHSCOPE_API_KEY" ] && DASHSCOPE_API_KEY="${SPRING_AI_DASHSCOPE_API_KEY:-}"
    [ -z "$DEEPSEEK_API_KEY" ] && DEEPSEEK_API_KEY="${SPRING_AI_DEEPSEEK_API_KEY:-}"
    
    [ -z "$DIFY_DEFAULT_API_KEY" ] && DIFY_DEFAULT_API_KEY="${DIFY_APPS_DEFAULT_API_KEY:-}"
    [ -z "$DIFY_DEFAULT_APP_ID" ] && DIFY_DEFAULT_APP_ID="${DIFY_APPS_DEFAULT_APP_ID:-}"
    
    [ -z "$DIFY_CS_API_KEY" ] && DIFY_CS_API_KEY="${DIFY_APPS_CUSTOMERSERVICE_API_KEY:-}"
    [ -z "$DIFY_CS_APP_ID" ] && DIFY_CS_APP_ID="${DIFY_APPS_CUSTOMERSERVICE_APP_ID:-}"
    
    [ -z "$DIFY_TS_API_KEY" ] && DIFY_TS_API_KEY="${DIFY_APPS_TECHSUPPORT_API_KEY:-}"
    [ -z "$DIFY_TS_APP_ID" ] && DIFY_TS_APP_ID="${DIFY_APPS_TECHSUPPORT_APP_ID:-}"
    
    [ -z "$WECOM_TOKEN" ] && WECOM_TOKEN="${WECOM_BOT_TOKEN:-}"
    [ -z "$WECOM_AES_KEY" ] && WECOM_AES_KEY="${WECOM_BOT_ENCODING_AES_KEY:-}"
    [ -z "$WECOM_CORP_ID" ] && WECOM_CORP_ID="${WECOM_BOT_CORP_ID:-}"
}

################################################################################
# Function: Validate required parameters
################################################################################
validate_parameters() {
    local has_required=false
    
    # Check if at least one API key is provided
    if [ -n "$DASHSCOPE_API_KEY" ] || [ -n "$DEEPSEEK_API_KEY" ] || \
       [ -n "$DIFY_DEFAULT_API_KEY" ]; then
        has_required=true
    fi
    
    if [ "$has_required" = false ]; then
        print_message "$RED" "Error: At least one API key must be provided."
        print_message "$YELLOW" "Provide via command-line parameters or environment variables:"
        print_message "$YELLOW" "  --dashscope-api-key or SPRING_AI_DASHSCOPE_API_KEY"
        print_message "$YELLOW" "  --deepseek-api-key or SPRING_AI_DEEPSEEK_API_KEY"
        print_message "$YELLOW" "  --dify-default-api-key or DIFY_APPS_DEFAULT_API_KEY"
        print_message "$YELLOW" "\nRun '$0 --help' for more information."
        exit 1
    fi
    
    # Check if JAR file exists
    if [ ! -f "$JAR_FILE" ]; then
        print_message "$RED" "Error: JAR file not found: $JAR_FILE"
        print_message "$YELLOW" "Please build the project first: mvn clean package"
        exit 1
    fi
}

################################################################################
# Function: Start the application
################################################################################
start_application() {
    # Check if already running
    if is_running; then
        PID=$(cat "$PID_FILE")
        print_message "$YELLOW" "Application is already running (PID: $PID)"
        print_message "$YELLOW" "Use --stop to stop it first, or --restart to restart."
        exit 1
    fi
    
    # Load from environment variables
    load_env_variables
    
    # Validate parameters
    validate_parameters
    
    # Create log directory
    mkdir -p "$LOG_DIR"
    
    # Build Java command with parameters
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
    JAVA_CMD="java $JAVA_OPTS -jar $JAR_FILE"
    JAVA_CMD="$JAVA_CMD --spring.profiles.active=$PROFILE"
    JAVA_CMD="$JAVA_CMD --server.port=$PORT"
    
    # Add API keys as command line arguments
    [ -n "$DASHSCOPE_API_KEY" ] && JAVA_CMD="$JAVA_CMD --spring.ai.dashscope.api-key=$DASHSCOPE_API_KEY"
    [ -n "$DEEPSEEK_API_KEY" ] && JAVA_CMD="$JAVA_CMD --spring.ai.deepseek.api-key=$DEEPSEEK_API_KEY"
    
    [ -n "$DIFY_DEFAULT_API_KEY" ] && JAVA_CMD="$JAVA_CMD --dify.apps.default.api-key=$DIFY_DEFAULT_API_KEY"
    [ -n "$DIFY_DEFAULT_APP_ID" ] && JAVA_CMD="$JAVA_CMD --dify.apps.default.app-id=$DIFY_DEFAULT_APP_ID"
    
    [ -n "$DIFY_CS_API_KEY" ] && JAVA_CMD="$JAVA_CMD --dify.apps.customer-service.api-key=$DIFY_CS_API_KEY"
    [ -n "$DIFY_CS_APP_ID" ] && JAVA_CMD="$JAVA_CMD --dify.apps.customer-service.app-id=$DIFY_CS_APP_ID"
    
    [ -n "$DIFY_TS_API_KEY" ] && JAVA_CMD="$JAVA_CMD --dify.apps.tech-support.api-key=$DIFY_TS_API_KEY"
    [ -n "$DIFY_TS_APP_ID" ] && JAVA_CMD="$JAVA_CMD --dify.apps.tech-support.app-id=$DIFY_TS_APP_ID"
    
    [ -n "$WECOM_TOKEN" ] && JAVA_CMD="$JAVA_CMD --wecom.bot.token=$WECOM_TOKEN"
    [ -n "$WECOM_AES_KEY" ] && JAVA_CMD="$JAVA_CMD --wecom.bot.encoding-aes-key=$WECOM_AES_KEY"
    [ -n "$WECOM_CORP_ID" ] && JAVA_CMD="$JAVA_CMD --wecom.bot.corp-id=$WECOM_CORP_ID"
    
    print_message "$GREEN" "Starting AI Alibaba Dify application..."
    print_message "$YELLOW" "Profile: $PROFILE"
    print_message "$YELLOW" "Port: $PORT"
    print_message "$YELLOW" "JAR: $JAR_FILE"
    print_message "$YELLOW" "Logs: $LOG_DIR"
    
    # Show configuration source
    print_message "$YELLOW" "\nAPI Keys loaded:"
    [ -n "$DASHSCOPE_API_KEY" ] && print_message "$YELLOW" "  ✓ DashScope API Key"
    [ -n "$DEEPSEEK_API_KEY" ] && print_message "$YELLOW" "  ✓ DeepSeek API Key"
    [ -n "$DIFY_DEFAULT_API_KEY" ] && print_message "$YELLOW" "  ✓ Dify Default API Key"
    [ -n "$DIFY_DEFAULT_APP_ID" ] && print_message "$YELLOW" "  ✓ Dify Default App ID"
    [ -n "$DIFY_CS_API_KEY" ] && print_message "$YELLOW" "  ✓ Dify Customer Service API Key"
    [ -n "$DIFY_TS_API_KEY" ] && print_message "$YELLOW" "  ✓ Dify Tech Support API Key"
    [ -n "$WECOM_TOKEN" ] && print_message "$YELLOW" "  ✓ WeCom Token"
    echo
    
    # Start application in background
    nohup $JAVA_CMD > "$LOG_FILE" 2>&1 &
    
    # Save PID
    echo $! > "$PID_FILE"
    PID=$(cat "$PID_FILE")
    
    print_message "$GREEN" "Application started with PID: $PID"
    print_message "$YELLOW" "Waiting for application to be ready..."
    
    # Wait for application to start (check health endpoint)
    MAX_WAIT=60
    for i in $(seq 1 $MAX_WAIT); do
        if curl -s "http://localhost:${PORT}/actuator/health" > /dev/null 2>&1; then
            print_message "$GREEN" "✓ Application is ready!"
            print_message "$GREEN" "✓ Health check: http://localhost:${PORT}/actuator/health"
            print_message "$GREEN" "✓ API endpoint: http://localhost:${PORT}/api/v1/chat/send"
            print_message "$YELLOW" "\nView logs:"
            print_message "$YELLOW" "  tail -f ${LOG_DIR}/application.log"
            print_message "$YELLOW" "  tail -f ${LOG_FILE}"
            return 0
        fi
        
        # Check if process is still running
        if ! ps -p "$PID" > /dev/null 2>&1; then
            print_message "$RED" "✗ Application failed to start. Check logs:"
            print_message "$YELLOW" "  cat ${LOG_FILE}"
            rm -f "$PID_FILE"
            exit 1
        fi
        
        sleep 1
    done
    
    print_message "$YELLOW" "Application started but health check not responding yet."
    print_message "$YELLOW" "Check logs: tail -f ${LOG_FILE}"
}

################################################################################
# Main Script
################################################################################

# Parse command line arguments
ACTION="start"

while [[ $# -gt 0 ]]; do
    case $1 in
        --dashscope-api-key)
            DASHSCOPE_API_KEY="$2"
            shift 2
            ;;
        --deepseek-api-key)
            DEEPSEEK_API_KEY="$2"
            shift 2
            ;;
        --dify-default-api-key)
            DIFY_DEFAULT_API_KEY="$2"
            shift 2
            ;;
        --dify-default-app-id)
            DIFY_DEFAULT_APP_ID="$2"
            shift 2
            ;;
        --dify-cs-api-key)
            DIFY_CS_API_KEY="$2"
            shift 2
            ;;
        --dify-cs-app-id)
            DIFY_CS_APP_ID="$2"
            shift 2
            ;;
        --dify-ts-api-key)
            DIFY_TS_API_KEY="$2"
            shift 2
            ;;
        --dify-ts-app-id)
            DIFY_TS_APP_ID="$2"
            shift 2
            ;;
        --wecom-token)
            WECOM_TOKEN="$2"
            shift 2
            ;;
        --wecom-aes-key)
            WECOM_AES_KEY="$2"
            shift 2
            ;;
        --wecom-corp-id)
            WECOM_CORP_ID="$2"
            shift 2
            ;;
        --profile)
            PROFILE="$2"
            shift 2
            ;;
        --port)
            PORT="$2"
            shift 2
            ;;
        --jar-file)
            JAR_FILE="$2"
            shift 2
            ;;
        --log-dir)
            LOG_DIR="$2"
            LOG_FILE="${LOG_DIR}/startup.log"
            shift 2
            ;;
        --stop)
            ACTION="stop"
            shift
            ;;
        --status)
            ACTION="status"
            shift
            ;;
        --restart)
            ACTION="restart"
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            print_message "$RED" "Unknown option: $1"
            usage
            ;;
    esac
done

# Execute action
case $ACTION in
    start)
        start_application
        ;;
    stop)
        stop_application
        ;;
    status)
        show_status
        ;;
    restart)
        stop_application
        sleep 2
        start_application
        ;;
    *)
        print_message "$RED" "Unknown action: $ACTION"
        usage
        ;;
esac

exit 0
