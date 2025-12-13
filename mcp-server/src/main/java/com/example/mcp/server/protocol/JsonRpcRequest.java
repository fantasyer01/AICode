package com.example.mcp.server.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a JSON-RPC 2.0 request message.
 * 
 * Request messages are sent from client to server to invoke methods.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonRpcRequest {
    
    /**
     * JSON-RPC version, always "2.0"
     */
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    /**
     * Unique request identifier (string or number)
     * Must be unique within the session and not null for requests
     */
    @JsonProperty("id")
    private Object id;
    
    /**
     * Method name to invoke
     */
    @JsonProperty("method")
    private String method;
    
    /**
     * Method parameters (optional)
     */
    @JsonProperty("params")
    private Object params;
    
    /**
     * Check if this is a notification (no id field)
     * Notifications don't expect a response
     */
    public boolean isNotification() {
        return id == null;
    }
}
