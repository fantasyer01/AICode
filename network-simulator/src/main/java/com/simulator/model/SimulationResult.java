package com.simulator.model;

import java.time.LocalDateTime;

public class SimulationResult {
    private boolean success;
    private String operation;
    private String message;
    private LocalDateTime timestamp;
    private Long responseTimeMs;
    private String details;

    public SimulationResult() {
        this.timestamp = LocalDateTime.now();
    }

    public SimulationResult(boolean success, String operation, String message) {
        this();
        this.success = success;
        this.operation = operation;
        this.message = message;
    }

    public static SimulationResult success(String operation, String message) {
        return new SimulationResult(true, operation, message);
    }

    public static SimulationResult failure(String operation, String message) {
        return new SimulationResult(false, operation, message);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
