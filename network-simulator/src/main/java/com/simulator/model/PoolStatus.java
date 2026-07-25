package com.simulator.model;

import java.time.LocalDateTime;

public class PoolStatus {
    private int activeCount;
    private int poolingCount;
    private int waitThreadCount;
    private long connectCount;
    private long closeCount;
    private long connectErrorCount;
    private long errorCount;
    private int maxActive;
    private int minIdle;
    private int initialSize;
    private LocalDateTime timestamp;
    private String status;

    public PoolStatus() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getPoolingCount() {
        return poolingCount;
    }

    public void setPoolingCount(int poolingCount) {
        this.poolingCount = poolingCount;
    }

    public int getWaitThreadCount() {
        return waitThreadCount;
    }

    public void setWaitThreadCount(int waitThreadCount) {
        this.waitThreadCount = waitThreadCount;
    }

    public long getConnectCount() {
        return connectCount;
    }

    public void setConnectCount(long connectCount) {
        this.connectCount = connectCount;
    }

    public long getCloseCount() {
        return closeCount;
    }

    public void setCloseCount(long closeCount) {
        this.closeCount = closeCount;
    }

    public long getConnectErrorCount() {
        return connectErrorCount;
    }

    public void setConnectErrorCount(long connectErrorCount) {
        this.connectErrorCount = connectErrorCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
