package com.example.httpclient.test.client;

/**
 * Connection pool configuration settings for HttpClient
 */
public class ConnectionPoolConfig {
    
    // Maximum total connections in the pool
    private int maxTotal = 20;
    
    // Maximum connections per route
    private int maxPerRoute = 10;
    
    // Time to validate connection after inactivity (ms)
    private int validateAfterInactivity = 2000;
    
    // Connection time to live (seconds)
    private int connectionTimeToLive = 3;
    
    // Idle time before connection eligible for eviction (seconds)
    private int evictionIdleTime = 10;
    
    // Eviction check interval (seconds)
    private int evictionCheckInterval = 5;
    
    // Connection timeout (ms)
    private int connectionTimeout = 5000;
    
    // Socket timeout (ms)
    private int socketTimeout = 10000;
    
    // Connection request timeout (ms)
    private int connectionRequestTimeout = 3000;

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxPerRoute() {
        return maxPerRoute;
    }

    public void setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    public int getValidateAfterInactivity() {
        return validateAfterInactivity;
    }

    public void setValidateAfterInactivity(int validateAfterInactivity) {
        this.validateAfterInactivity = validateAfterInactivity;
    }

    public int getConnectionTimeToLive() {
        return connectionTimeToLive;
    }

    public void setConnectionTimeToLive(int connectionTimeToLive) {
        this.connectionTimeToLive = connectionTimeToLive;
    }

    public int getEvictionIdleTime() {
        return evictionIdleTime;
    }

    public void setEvictionIdleTime(int evictionIdleTime) {
        this.evictionIdleTime = evictionIdleTime;
    }

    public int getEvictionCheckInterval() {
        return evictionCheckInterval;
    }

    public void setEvictionCheckInterval(int evictionCheckInterval) {
        this.evictionCheckInterval = evictionCheckInterval;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    @Override
    public String toString() {
        return "ConnectionPoolConfig{" +
                "maxTotal=" + maxTotal +
                ", maxPerRoute=" + maxPerRoute +
                ", validateAfterInactivity=" + validateAfterInactivity + "ms" +
                ", connectionTimeToLive=" + connectionTimeToLive + "s" +
                ", evictionIdleTime=" + evictionIdleTime + "s" +
                ", evictionCheckInterval=" + evictionCheckInterval + "s" +
                ", connectionTimeout=" + connectionTimeout + "ms" +
                ", socketTimeout=" + socketTimeout + "ms" +
                ", connectionRequestTimeout=" + connectionRequestTimeout + "ms" +
                '}';
    }
}
