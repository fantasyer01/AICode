# Quick Start Guide - HttpClient Idle Connection Test

## Windows Quick Commands

### 1. Build the Application
```powershell
cd D:\code\AICode\httpclient-idle-connection-test
mvn clean package
```

### 2. Run All Test Scenarios
```powershell
java -jar target\httpclient-idle-connection-test-1.0.0-executable.jar
```

### 3. Run a Specific Scenario

#### Scenario 1 - Basic Connection Pooling
```powershell
java -jar target\httpclient-idle-connection-test-1.0.0-executable.jar 1
```

#### Scenario 2 - Idle Connection Eviction (Recommended for Testing)
```powershell
java -jar target\httpclient-idle-connection-test-1.0.0-executable.jar 2
```

#### Scenario 3 - Connection Time-To-Live
```powershell
java -jar target\httpclient-idle-connection-test-1.0.0-executable.jar 3
```

#### Scenario 4 - Manual Eviction Trigger
```powershell
java -jar target\httpclient-idle-connection-test-1.0.0-executable.jar 4
```

## What to Observe

### Key Metrics in Output
Look for lines like:
```
Pool Statistics: Available: X, Leased: Y, Pending: Z, Total: X+Y, Max: 20
```

### Expected Behavior per Scenario

#### Scenario 2 (Idle Eviction) - Best for Testing
1. **After concurrent requests**: You'll see `Available: 10` (connections returned to pool)
2. **After idle period (15s)**: You'll see `Available: 0` or reduced (connections evicted)
3. **This proves idle eviction is working!**

#### Scenario 4 (Manual Eviction)
1. Shows immediate effect of calling eviction methods
2. Connection count drops right after `closeIdleConnections()` is called

## Test Duration

- **All scenarios**: ~2-3 minutes
- **Single scenario**: ~30-60 seconds (except Scenario 3 which takes ~40 seconds)

## Verifying Success

### Idle Connection Eviction Works If:
✓ Available connections increase after requests complete  
✓ Available connections decrease to 0 after idle timeout  
✓ Eviction thread logs appear every 5 seconds  
✓ Manual eviction immediately reduces connection count  

### Sample Successful Output Pattern
```
15:30:00.123 [main] INFO  ...TestOrchestrator - [After concurrent requests] Pool Statistics: Available: 10, Leased: 0, ...
15:30:15.456 [main] INFO  ...TestOrchestrator - [After idle period] Pool Statistics: Available: 0, Leased: 0, ...
```

The drop from 10 to 0 available connections proves eviction worked!

## Troubleshooting

### If port 8080 is busy:
Edit `Application.java` line 19 to change port:
```java
private static final int SERVER_PORT = 8081;  // Change from 8080
```

### To see more detailed logs:
Edit `src/main/resources/logback.xml` and change:
```xml
<logger name="com.example.httpclient.test" level="TRACE" />
```

## Understanding Connection Pool Configuration

Current settings (in `ConnectionPoolConfig.java`):
- **Max connections**: 20
- **Idle timeout**: 10 seconds (connections idle >10s will be evicted)
- **Eviction check interval**: 5 seconds (checks every 5s)
- **Connection TTL**: 30 seconds (max lifetime regardless of activity)

## Next Steps After Testing

To use this in your own application:
1. Copy `HttpClientManager.java` configuration approach
2. Use `.evictIdleConnections()` when building HttpClient
3. Start a background eviction thread or use the built-in one
4. Monitor pool statistics with `connectionManager.getTotalStats()`
