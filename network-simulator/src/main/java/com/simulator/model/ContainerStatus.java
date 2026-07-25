package com.simulator.model;

public class ContainerStatus {
    private String containerId;
    private String containerName;
    private String state;
    private String status;
    private boolean running;
    private boolean paused;
    private String networkId;
    private boolean networkConnected;

    // Getters and Setters
    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public boolean isNetworkConnected() {
        return networkConnected;
    }

    public void setNetworkConnected(boolean networkConnected) {
        this.networkConnected = networkConnected;
    }
}
