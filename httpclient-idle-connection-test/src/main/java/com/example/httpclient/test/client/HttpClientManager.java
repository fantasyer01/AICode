package com.example.httpclient.test.client;

import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Manages HttpClient instance with connection pool configuration
 */
public class HttpClientManager {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpClientManager.class);
    
    private final ConnectionPoolConfig config;
    private PoolingHttpClientConnectionManager connectionManager;
    private CloseableHttpClient httpClient;
    private Thread evictionThread;
    private volatile boolean running = false;

    public HttpClientManager(ConnectionPoolConfig config) {
        this.config = config;
    }

    /**
     * Initialize HttpClient with connection pool
     */
    public void initialize() {
        logger.info("Initializing HttpClient with configuration: {}", config);
        
        // Create connection manager with TTL
        connectionManager = new PoolingHttpClientConnectionManager(
            config.getConnectionTimeToLive(), 
            TimeUnit.SECONDS
        );
        
        // Set maximum total connections
        connectionManager.setMaxTotal(config.getMaxTotal());
        
        // Set maximum connections per route
        connectionManager.setDefaultMaxPerRoute(config.getMaxPerRoute());
        
        // Set validate after inactivity
        connectionManager.setValidateAfterInactivity(config.getValidateAfterInactivity());
        
        // Configure socket settings
        SocketConfig socketConfig = SocketConfig.custom()
            .setSoTimeout(config.getSocketTimeout())
            .build();
        connectionManager.setDefaultSocketConfig(socketConfig);
        
        // Create HttpClient
        httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            // .evictExpiredConnections()
            .evictIdleConnections(config.getEvictionIdleTime(), TimeUnit.SECONDS)
            .build();
        
        // Start background eviction thread
        startEvictionThread();
        
        logger.info("HttpClient initialized successfully");
    }

    /**
     * Start background thread for idle connection eviction
     */
    private void startEvictionThread() {
        running = true;
        evictionThread = new Thread(() -> {
            logger.info("Eviction thread started (interval: {}s, idle timeout: {}s)", 
                config.getEvictionCheckInterval(), config.getEvictionIdleTime());
            
            while (running) {
                try {
                    Thread.sleep(config.getEvictionCheckInterval() * 1000L);
                    
                    if (connectionManager != null) {
                        // Close expired connections
                        // connectionManager.closeExpiredConnections();
                        
                        // Close idle connections
                        connectionManager.closeIdleConnections(
                            config.getEvictionIdleTime(), 
                            TimeUnit.SECONDS
                        );
                        
                        logger.debug("Eviction check completed");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.info("Eviction thread interrupted");
                    break;
                }
            }
            
            logger.info("Eviction thread stopped");
        }, "ConnectionEvictionThread");
        
        evictionThread.setDaemon(true);
        evictionThread.start();
    }

    /**
     * Get the HttpClient instance
     */
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Get the connection manager
     */
    public PoolingHttpClientConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Manually trigger eviction of expired connections
     */
    public void closeExpiredConnections() {
        if (connectionManager != null) {
            connectionManager.closeExpiredConnections();
            logger.info("Manually closed expired connections");
        }
    }

    /**
     * Manually trigger eviction of idle connections
     */
    public void closeIdleConnections(long idleTime, TimeUnit timeUnit) {
        if (connectionManager != null) {
            connectionManager.closeIdleConnections(idleTime, timeUnit);
            logger.info("Manually closed idle connections (timeout: {} {})", idleTime, timeUnit);
        }
    }

    /**
     * Close HttpClient and release resources
     */
    public void close() {
        logger.info("Closing HttpClient and connection manager");
        
        // Stop eviction thread
        running = false;
        if (evictionThread != null) {
            evictionThread.interrupt();
            try {
                evictionThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Close HttpClient
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("Error closing HttpClient", e);
            }
        }
        
        // Shutdown connection manager
        if (connectionManager != null) {
            connectionManager.shutdown();
        }
        
        logger.info("HttpClient closed successfully");
    }
}
