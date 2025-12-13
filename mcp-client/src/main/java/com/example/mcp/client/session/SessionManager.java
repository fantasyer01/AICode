package com.example.mcp.client.session;

import com.example.mcp.client.core.TransportHandler;
import com.example.mcp.client.protocol.JsonRpcRequest;
import com.example.mcp.client.protocol.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages MCP session state and request/response correlation.
 */
public class SessionManager {
    
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
    
    private final TransportHandler transportHandler;
    private final AtomicLong requestIdCounter = new AtomicLong(1);
    private final Map<Object, CompletableFuture<JsonRpcResponse>> pendingRequests = new ConcurrentHashMap<>();
    
    private boolean initialized = false;
    private Map<String, Object> serverCapabilities = null;
    private Map<String, Object> serverInfo = null;
    
    public SessionManager(TransportHandler transportHandler) {
        this.transportHandler = transportHandler;
        startResponseListener();
    }
    
    /**
     * Initialize the session with the server
     */
    public boolean initialize() throws Exception {
        log.info("Initializing MCP session...");
        
        Map<String, Object> params = new HashMap<>();
        params.put("protocolVersion", "2024-11-05");
        
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("roots", Map.of("listChanged", false));
        params.put("capabilities", capabilities);
        
        Map<String, String> clientInfo = new HashMap<>();
        clientInfo.put("name", "MCP Java Client");
        clientInfo.put("version", "1.0.0");
        params.put("clientInfo", clientInfo);
        
        JsonRpcResponse response = sendRequest("initialize", params);
        
        if (response.getError() != null) {
            log.error("Initialization failed: {}", response.getError().getMessage());
            return false;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        serverCapabilities = (Map<String, Object>) result.get("capabilities");
        serverInfo = (Map<String, Object>) result.get("serverInfo");
        
        log.info("Server: {} v{}", serverInfo.get("name"), serverInfo.get("version"));
        log.info("Protocol version: {}", result.get("protocolVersion"));
        
        // Send initialized notification
        sendNotification("initialized", null);
        
        initialized = true;
        log.info("Session initialized successfully");
        return true;
    }
    
    /**
     * Send a request and wait for response
     */
    public JsonRpcResponse sendRequest(String method, Object params) throws Exception {
        long requestIdLong = requestIdCounter.getAndIncrement();
        // Convert to Integer for JSON compatibility (Jackson deserializes numbers as Integer)
        Integer requestId = (int) requestIdLong;
        
        JsonRpcRequest request = JsonRpcRequest.builder()
                .jsonrpc("2.0")
                .id(requestId)
                .method(method)
                .params(params)
                .build();
        
        CompletableFuture<JsonRpcResponse> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        log.debug("Registered request ID: {} (type: {}) for method: {}", requestId, requestId.getClass().getSimpleName(), method);
        
        transportHandler.sendRequest(request);
        log.debug("Sent request ID: {}", requestId);
        
        // Wait for response with timeout
        return future.get(30, java.util.concurrent.TimeUnit.SECONDS);
    }
    
    /**
     * Send a notification (no response expected)
     */
    public void sendNotification(String method, Object params) throws Exception {
        JsonRpcRequest request = JsonRpcRequest.builder()
                .jsonrpc("2.0")
                .method(method)
                .params(params)
                .build();
        
        transportHandler.sendRequest(request);
    }
    
    /**
     * Start listening for responses in background thread
     */
    private void startResponseListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (transportHandler.isActive()) {
                    JsonRpcResponse response = transportHandler.readResponse();
                    
                    if (response == null) {
                        log.debug("Connection closed");
                        break;
                    }
                    
                    Object id = response.getId();
                    log.debug("Looking up request ID: {} (type: {}), pending requests: {}", id, id.getClass().getSimpleName(), pendingRequests.keySet());
                    CompletableFuture<JsonRpcResponse> future = pendingRequests.remove(id);
                    
                    if (future != null) {
                        log.debug("Completing future for request ID: {}", id);
                        future.complete(response);
                    } else {
                        log.warn("Received response for unknown request ID: {} (type: {}), pending: {}", id, id.getClass().getSimpleName(), pendingRequests.keySet());
                    }
                }
            } catch (Exception e) {
                log.error("Response listener error: {}", e.getMessage(), e);
            }
        });
        
        listenerThread.setDaemon(true);
        listenerThread.setName("MCP-ResponseListener");
        listenerThread.start();
    }
    
    /**
     * Check if session is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get server capabilities
     */
    public Map<String, Object> getServerCapabilities() {
        return serverCapabilities;
    }
    
    /**
     * Get server info
     */
    public Map<String, Object> getServerInfo() {
        return serverInfo;
    }
    
    /**
     * Close the session
     */
    public void close() {
        transportHandler.close();
        pendingRequests.values().forEach(future -> 
            future.completeExceptionally(new Exception("Session closed")));
        pendingRequests.clear();
    }
}
