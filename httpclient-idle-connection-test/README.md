# Apache HttpClient Idle Connection Eviction Test

A Java demonstration application that tests and validates Apache HttpClient 4.5+ idle connection eviction mechanisms in a local environment.

## Overview

This application creates a complete testing environment with:
- Local HTTP server for self-contained testing
- Configured HttpClient connection pool
- Real-time connection monitoring
- Four test scenarios demonstrating eviction behavior

## Requirements

- Java 11 or higher
- Maven 3.6+

## Quick Start

### Build the Application

```bash
cd httpclient-idle-connection-test
mvn clean package
```

This creates an executable JAR: `target/httpclient-idle-connection-test-1.0.0-executable.jar`

### Run All Test Scenarios

```bash
java -jar target/httpclient-idle-connection-test-1.0.0-executable.jar
```

### Run a Specific Scenario

```bash
java -jar target/httpclient-idle-connection-test-1.0.0-executable.jar 2
```

Available scenarios: 1, 2, 3, 4

## Test Scenarios

### Scenario 1: Basic Connection Pooling
- Executes 5 sequential HTTP requests
- Demonstrates connection reuse from the pool
- Shows connections being returned to the pool after use

### Scenario 2: Idle Connection Eviction
- Executes 10 concurrent requests to populate the pool
- Waits 15 seconds (longer than idle timeout of 10 seconds)
- Demonstrates automatic eviction of idle connections
- **Key observation**: Connection count decreases after idle period

### Scenario 3: Connection Time-To-Live (TTL)
- Executes requests over 30+ seconds (matching TTL configuration)
- Demonstrates TTL-based connection replacement
- Shows that connections are closed after reaching maximum lifetime

### Scenario 4: Manual Eviction Trigger
- Populates pool with concurrent requests
- Waits for connections to become idle
- Manually triggers `closeExpiredConnections()` and `closeIdleConnections()`
- Demonstrates immediate cleanup effect

## Connection Pool Configuration

Default configuration values (see `ConnectionPoolConfig.java`):

| Parameter | Value | Description |
|-----------|-------|-------------|
| Max Total Connections | 20 | Maximum connections in pool |
| Max Per Route | 10 | Maximum connections per destination |
| Connection TTL | 30s | Maximum connection lifetime |
| Idle Eviction Time | 10s | Time before idle connection is evicted |
| Eviction Check Interval | 5s | How often eviction runs |
| Validate After Inactivity | 2000ms | Time before validating stale connections |

## Understanding the Output

### Pool Statistics Format
```
Available: X, Leased: Y, Pending: Z, Total: X+Y, Max: N
```

- **Available**: Idle connections in the pool ready for reuse
- **Leased**: Connections currently in use by requests
- **Pending**: Requests waiting for a connection
- **Total**: Total active connections (Available + Leased)
- **Max**: Maximum allowed connections

### What to Look For

1. **Connection Reuse**: After requests complete, `Available` count increases
2. **Idle Eviction**: After idle timeout, `Available` count decreases
3. **TTL Effect**: Connections are replaced even if not idle
4. **Manual Eviction**: Immediate reduction in connection count

## Project Structure

```
src/main/java/com/example/httpclient/test/
├── Application.java              - Main entry point
├── client/
│   ├── ConnectionPoolConfig.java - Pool configuration settings
│   └── HttpClientManager.java    - HttpClient lifecycle management
├── server/
│   └── LocalHttpServer.java      - Test HTTP server
├── monitor/
│   ├── ConnectionMonitor.java    - Pool statistics monitoring
│   └── PoolStatistics.java       - Statistics data model
├── executor/
│   └── RequestExecutor.java      - HTTP request execution
└── orchestrator/
    └── TestOrchestrator.java     - Test scenario coordination
```

## How It Works

### Initialization Phase
1. Starts local HTTP server on port 8080
2. Initializes HttpClient with connection pool configuration
3. Starts background eviction thread (runs every 5 seconds)
4. Starts connection monitoring thread (logs stats every 3 seconds)

### Execution Phase
Each scenario exercises the connection pool differently:
- Creates connections through HTTP requests
- Allows connections to become idle
- Observes eviction behavior through monitoring

### Background Eviction
The application uses two eviction mechanisms:

1. **Built-in HttpClient eviction**: 
   ```java
   HttpClients.custom()
       .evictExpiredConnections()
       .evictIdleConnections(10, TimeUnit.SECONDS)
   ```

2. **Custom eviction thread**: Runs every 5 seconds calling:
   ```java
   connectionManager.closeExpiredConnections();
   connectionManager.closeIdleConnections(10, TimeUnit.SECONDS);
   ```

## Customizing Configuration

To modify connection pool settings, edit `ConnectionPoolConfig.java`:

```java
private int maxTotal = 20;              // Change max connections
private int evictionIdleTime = 10;      // Change idle timeout
private int connectionTimeToLive = 30;   // Change TTL
```

## Troubleshooting

### Port 8080 Already in Use
Change `SERVER_PORT` in `Application.java` to a different port.

### No Eviction Observed
- Ensure idle timeout is shorter than your test wait period
- Check that eviction thread is running (look for log messages)
- Verify connections are actually idle (no ongoing requests)

### Connection Count Not Decreasing
- Wait for at least one eviction interval (5 seconds)
- Ensure connections have been idle longer than `evictionIdleTime`
- Check logs for eviction thread activity

## Windows-Specific Commands

### Build
```powershell
mvn clean package
```

### Run
```powershell
java -jar target\httpclient-idle-connection-test-1.0.0-executable.jar
```

### Run Specific Scenario
```powershell
java -jar target\httpclient-idle-connection-test-1.0.0-executable.jar 2
```

## Key Takeaways

1. **HttpClient 4.5+ provides built-in idle connection eviction**
2. **Eviction requires both configuration and enabling**:
   - Set eviction time with `.evictIdleConnections(time, unit)`
   - Enable with `.evictExpiredConnections()`
3. **Background eviction thread is recommended** for proactive cleanup
4. **Connection TTL is independent of idle eviction**
5. **Manual eviction is possible** via `closeIdleConnections()` and `closeExpiredConnections()`

## Dependencies

- Apache HttpClient 4.5.14
- Apache HttpCore 4.4.16
- SLF4J 2.0.9
- Logback 1.4.14

## License

This is a demonstration application for educational purposes.
