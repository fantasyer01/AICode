package com.example.httpclient.test.monitor;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors and reports connection pool statistics
 */
public class ConnectionMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(ConnectionMonitor.class);
    
    private final PoolingHttpClientConnectionManager connectionManager;
    private final int monitoringInterval;
    private Thread monitorThread;
    private volatile boolean running = false;

    public ConnectionMonitor(PoolingHttpClientConnectionManager connectionManager, int monitoringInterval) {
        this.connectionManager = connectionManager;
        this.monitoringInterval = monitoringInterval;
    }

    /**
     * Start monitoring connection pool statistics
     */
    public void start() {
        running = true;
        monitorThread = new Thread(() -> {
            logger.info("Connection monitoring started (interval: {}s)", monitoringInterval);
            
            while (running) {
                try {
                    Thread.sleep(monitoringInterval * 1000L);
                    logPoolStatistics();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.info("Connection monitor interrupted");
                    break;
                }
            }
            
            logger.info("Connection monitoring stopped");
        }, "ConnectionMonitorThread");
        
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    /**
     * Stop monitoring
     */
    public void stop() {
        running = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
            try {
                monitorThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Get current pool statistics
     */
    public PoolStatistics getPoolStatistics() {
        PoolStats stats = connectionManager.getTotalStats();
        return new PoolStatistics(
            stats.getAvailable(),
            stats.getLeased(),
            stats.getPending(),
            stats.getMax()
        );
    }

    /**
     * Log pool statistics
     */
    public void logPoolStatistics() {
        PoolStatistics stats = getPoolStatistics();
        logger.info("Pool Statistics: {}", stats);
    }

    /**
     * Log pool statistics with a custom label
     */
    public void logPoolStatistics(String label) {
        PoolStatistics stats = getPoolStatistics();
        logger.info("[{}] Pool Statistics: {}", label, stats);
    }

    /**
     * Get detailed pool statistics information
     */
    public String getDetailedStats() {
        PoolStats stats = connectionManager.getTotalStats();
        return String.format(
            "Connection Pool Details:\n" +
            "  Available: %d\n" +
            "  Leased: %d\n" +
            "  Pending: %d\n" +
            "  Total: %d\n" +
            "  Max: %d",
            stats.getAvailable(),
            stats.getLeased(),
            stats.getPending(),
            stats.getAvailable() + stats.getLeased(),
            stats.getMax()
        );
    }
}
