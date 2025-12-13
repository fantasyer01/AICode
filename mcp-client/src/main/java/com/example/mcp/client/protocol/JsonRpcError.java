package com.example.mcp.client.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a JSON-RPC 2.0 error object.
 * 
 * Error codes follow the JSON-RPC 2.0 specification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcError {
    
    /**
     * Error code indicating the error type
     */
    @JsonProperty("code")
    private int code;
    
    /**
     * Human-readable error message
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Additional error details (optional)
     */
    @JsonProperty("data")
    private Object data;
    
    // Standard JSON-RPC error codes
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    
    /**
     * Create a parse error
     */
    public static JsonRpcError parseError(String details) {
        return JsonRpcError.builder()
                .code(PARSE_ERROR)
                .message("Parse error")
                .data(details)
                .build();
    }
    
    /**
     * Create an invalid request error
     */
    public static JsonRpcError invalidRequest(String details) {
        return JsonRpcError.builder()
                .code(INVALID_REQUEST)
                .message("Invalid Request")
                .data(details)
                .build();
    }
    
    /**
     * Create a method not found error
     */
    public static JsonRpcError methodNotFound(String method) {
        return JsonRpcError.builder()
                .code(METHOD_NOT_FOUND)
                .message("Method not found")
                .data("Method '" + method + "' does not exist")
                .build();
    }
    
    /**
     * Create an invalid params error
     */
    public static JsonRpcError invalidParams(String details) {
        return JsonRpcError.builder()
                .code(INVALID_PARAMS)
                .message("Invalid params")
                .data(details)
                .build();
    }
    
    /**
     * Create an internal error
     */
    public static JsonRpcError internalError(String details) {
        return JsonRpcError.builder()
                .code(INTERNAL_ERROR)
                .message("Internal error")
                .data(details)
                .build();
    }
}
