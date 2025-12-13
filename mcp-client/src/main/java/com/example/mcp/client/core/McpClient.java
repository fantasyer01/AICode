package com.example.mcp.client.core;

import com.example.mcp.client.protocol.JsonRpcResponse;
import com.example.mcp.client.session.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core MCP Client implementation.
 * 
 * Manages connection to server and provides high-level API for MCP operations.
 */
public class McpClient {
    
    private static final Logger log = LoggerFactory.getLogger(McpClient.class);
    
    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper;
    
    public McpClient(String serverJarPath) throws Exception {
        this.objectMapper = new ObjectMapper();
        
        // Start server process
        log.info("Starting MCP server from: {}", serverJarPath);
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", serverJarPath);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process serverProcess = pb.start();
        
        // Create transport and session
        TransportHandler transport = new TransportHandler(serverProcess, objectMapper);
        this.sessionManager = new SessionManager(transport);
        
        // Initialize session
        if (!sessionManager.initialize()) {
            throw new Exception("Failed to initialize MCP session");
        }
    }
    
    /**
     * List all available tools
     */
    public List<Map<String, Object>> listTools() throws Exception {
        log.debug("Listing tools");
        JsonRpcResponse response = sessionManager.sendRequest("tools/list", null);
        
        if (response.getError() != null) {
            throw new Exception("Failed to list tools: " + response.getError().getMessage());
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        return (List<Map<String, Object>>) result.get("tools");
    }
    
    /**
     * Call a tool with arguments
     */
    public Object callTool(String toolName, Map<String, Object> arguments) throws Exception {
        log.debug("Calling tool: {}", toolName);
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", toolName);
        params.put("arguments", arguments);
        
        JsonRpcResponse response = sessionManager.sendRequest("tools/call", params);
        
        if (response.getError() != null) {
            throw new Exception("Tool call failed: " + response.getError().getMessage());
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        return result.get("content");
    }
    
    /**
     * List all available resources
     */
    public List<Map<String, Object>> listResources() throws Exception {
        log.debug("Listing resources");
        JsonRpcResponse response = sessionManager.sendRequest("resources/list", null);
        
        if (response.getError() != null) {
            throw new Exception("Failed to list resources: " + response.getError().getMessage());
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        return (List<Map<String, Object>>) result.get("resources");
    }
    
    /**
     * Read a resource by URI
     */
    public Object readResource(String uri) throws Exception {
        log.debug("Reading resource: {}", uri);
        
        Map<String, Object> params = new HashMap<>();
        params.put("uri", uri);
        
        JsonRpcResponse response = sessionManager.sendRequest("resources/read", params);
        
        if (response.getError() != null) {
            throw new Exception("Resource read failed: " + response.getError().getMessage());
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        return result.get("contents");
    }
    
    /**
     * List all available prompts
     */
    public List<Map<String, Object>> listPrompts() throws Exception {
        log.debug("Listing prompts");
        JsonRpcResponse response = sessionManager.sendRequest("prompts/list", null);
        
        if (response.getError() != null) {
            throw new Exception("Failed to list prompts: " + response.getError().getMessage());
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        return (List<Map<String, Object>>) result.get("prompts");
    }
    
    /**
     * Get a prompt with arguments
     */
    public Object getPrompt(String promptName, Map<String, Object> arguments) throws Exception {
        log.debug("Getting prompt: {}", promptName);
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", promptName);
        params.put("arguments", arguments);
        
        JsonRpcResponse response = sessionManager.sendRequest("prompts/get", params);
        
        if (response.getError() != null) {
            throw new Exception("Prompt retrieval failed: " + response.getError().getMessage());
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();
        return result.get("messages");
    }
    
    /**
     * Get server information
     */
    public Map<String, Object> getServerInfo() {
        return sessionManager.getServerInfo();
    }
    
    /**
     * Get server capabilities
     */
    public Map<String, Object> getServerCapabilities() {
        return sessionManager.getServerCapabilities();
    }
    
    /**
     * Close the client and cleanup resources
     */
    public void close() {
        log.info("Closing MCP client");
        sessionManager.close();
    }
}
