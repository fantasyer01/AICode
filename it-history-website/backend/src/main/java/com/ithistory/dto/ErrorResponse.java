package com.ithistory.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private String errorCode;
    private String errorMessage;
    private String timestamp;
    private String path;
    
    public ErrorResponse(String errorCode, String errorMessage, String path) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now().toString();
        this.path = path;
    }
}
