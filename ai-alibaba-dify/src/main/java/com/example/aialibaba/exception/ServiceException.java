package com.example.aialibaba.exception;

/**
 * Custom business exception for service layer
 */
public class ServiceException extends RuntimeException {
    
    private final String errorCode;
    private final Object[] params;
    
    public ServiceException(String message) {
        super(message);
        this.errorCode = "SERVICE_ERROR";
        this.params = null;
    }
    
    public ServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.params = null;
    }
    
    public ServiceException(String errorCode, String message, Object... params) {
        super(message);
        this.errorCode = errorCode;
        this.params = params;
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SERVICE_ERROR";
        this.params = null;
    }
    
    public ServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.params = null;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object[] getParams() {
        return params;
    }
}