package com.example.mcp.client.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a JSON-RPC 2.0 response message.
 * 
 * Response messages are sent from server to client in reply to requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {
    
    /**
     * JSON-RPC version, always "2.0"
     */
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    /**
     * Request identifier matching the original request
     */
    @JsonProperty("id")
    private Object id;
    
    /**
     * Result object for successful responses
     * Mutually exclusive with error
     */
    @JsonProperty("result")
    private Object result;
    
    /**
     * Error object for failed responses
     * Mutually exclusive with result
     */
    @JsonProperty("error")
    private JsonRpcError error;
    
    /**
     * Create a success response with result
     */
    public static JsonRpcResponse success(Object id, Object result) {
        return JsonRpcResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(result)
                .build();
    }
    
    /**
     * Create an error response
     */
    public static JsonRpcResponse error(Object id, JsonRpcError error) {
        return JsonRpcResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .error(error)
                .build();
    }
}
