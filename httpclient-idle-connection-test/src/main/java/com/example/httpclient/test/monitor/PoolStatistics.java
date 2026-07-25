package com.example.httpclient.test.monitor;

/**
 * Data model for connection pool statistics
 */
public class PoolStatistics {
    
    private final int available;
    private final int leased;
    private final int pending;
    private final int max;
    private final long timestamp;

    public PoolStatistics(int available, int leased, int pending, int max) {
        this.available = available;
        this.leased = leased;
        this.pending = pending;
        this.max = max;
        this.timestamp = System.currentTimeMillis();
    }

    public int getAvailable() {
        return available;
    }

    public int getLeased() {
        return leased;
    }

    public int getPending() {
        return pending;
    }

    public int getMax() {
        return max;
    }

    public int getTotal() {
        return available + leased;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("Available: %d, Leased: %d, Pending: %d, Total: %d, Max: %d",
                available, leased, pending, getTotal(), max);
    }
}
