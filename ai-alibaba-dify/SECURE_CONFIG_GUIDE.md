# Secure Configuration Guide

## API Key Security

This document explains how to secure your API keys by removing them from configuration files and passing them via startup scripts.

## Configuration Files - Remove All API Keys

### application.yml
Remove or replace all hardcoded API keys with placeholder values:

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY:}  # Empty default, must be provided at runtime
    deepseek:
      api-key: ${DEEPSEEK_API_KEY:}   # Empty default, must be provided at runtime

dify:
  apps:
    default:
      api-key: ${DIFY_DEFAULT_API_KEY:}
      app-id: ${DIFY_DEFAULT_APP_ID:}
    customer-service:
      api-key: ${DIFY_CS_API_KEY:}
      app-id: ${DIFY_CS_APP_ID:}
    tech-support:
      api-key: ${DIFY_TS_API_KEY:}
      app-id: ${DIFY_TS_APP_ID:}

wecom:
  bot:
    token: ${WECOM_BOT_TOKEN:}
    encoding-aes-key: ${WECOM_BOT_AES_KEY:}
    corp-id: ${WECOM_CORP_ID:}
```

### application-dev.yml
Remove all hardcoded keys in development profile:

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY:}
    deepseek:
      api-key: ${DEEPSEEK_API_KEY:}

dify:
  apps:
    default:
      api-key: ${DIFY_DEFAULT_API_KEY:}
      app-id: ${DIFY_DEFAULT_APP_ID:}
```

### application-prod.yml
Production should never have hardcoded keys:

```yaml
# Keys must be provided via startup script
# No default values in production
```

## Using the Startup Scripts

### Linux/Mac (start.sh)

```bash
# Make script executable
chmod +x start.sh

# Start with API keys
./start.sh \
  --dashscope-api-key "sk-your-dashscope-key" \
  --deepseek-api-key "sk-your-deepseek-key" \
  --dify-default-api-key "app-your-dify-key" \
  --dify-default-app-id "your-app-id" \
  --profile prod \
  --port 8080

# Stop the application
./start.sh --stop

# Check status
./start.sh --status

# Restart
./start.sh --restart
```

### Windows (start.ps1)

```powershell
# Start with API keys
.\start.ps1 `
  -DashscopeApiKey "sk-your-dashscope-key" `
  -DeepseekApiKey "sk-your-deepseek-key" `
  -DifyDefaultApiKey "app-your-dify-key" `
  -DifyDefaultAppId "your-app-id" `
  -Profile "prod" `
  -Port 8080

# Stop the application
.\start.ps1 -Stop

# Check status
.\start.ps1 -Status

# Restart with new keys
.\start.ps1 -Restart `
  -DifyDefaultApiKey "app-new-key" `
  -DifyDefaultAppId "new-app-id"
```

## Environment Variables Alternative

You can also use environment variables instead of command-line arguments:

### Linux/Mac
```bash
export DASHSCOPE_API_KEY="sk-your-dashscope-key"
export DEEPSEEK_API_KEY="sk-your-deepseek-key"
export DIFY_DEFAULT_API_KEY="app-your-dify-key"
export DIFY_DEFAULT_APP_ID="your-app-id"

java -jar target/ai-alibaba-dify-1.0.0.jar --spring.profiles.active=prod
```

### Windows PowerShell
```powershell
$env:DASHSCOPE_API_KEY="sk-your-dashscope-key"
$env:DEEPSEEK_API_KEY="sk-your-deepseek-key"
$env:DIFY_DEFAULT_API_KEY="app-your-dify-key"
$env:DIFY_DEFAULT_APP_ID="your-app-id"

java -jar target/ai-alibaba-dify-1.0.0.jar --spring.profiles.active=prod
```

## Security Best Practices

1. **Never commit API keys to version control**
   - Add `application-*.yml` files with real keys to `.gitignore`
   - Use template files like `application-dev.yml.template` instead

2. **Use different keys for different environments**
   - Development keys for testing
   - Production keys with restricted permissions

3. **Rotate keys regularly**
   - Change API keys periodically
   - Revoke compromised keys immediately

4. **Restrict key permissions**
   - Use API keys with minimum required permissions
   - Set rate limits and usage quotas

5. **Store keys securely**
   - Use password managers or secret management tools
   - Consider using Vault, AWS Secrets Manager, or similar

6. **Monitor key usage**
   - Enable logging and monitoring
   - Set up alerts for unusual activity

## Verification

After starting the application with the scripts:

1. Check application logs:
   ```bash
   tail -f logs/application.log
   ```

2. Verify no keys in config files:
   ```bash
   grep -r "sk-" src/main/resources/
   grep -r "app-" src/main/resources/
   ```

3. Test API endpoints:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

## Troubleshooting

### Application won't start
- Check if JAR file exists: `target/ai-alibaba-dify-1.0.0.jar`
- Verify at least one API key is provided
- Check logs: `cat logs/startup.log`

### API key errors
- Ensure keys are valid and not expired
- Check key format (DashScope: sk-*, Dify: app-*)
- Verify network connectivity to API endpoints

### Port already in use
- Change port: `--port 8081` or `-Port 8081`
- Or stop existing process: `./start.sh --stop`
