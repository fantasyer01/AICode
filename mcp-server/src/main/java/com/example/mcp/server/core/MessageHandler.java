package com.example.mcp.server.core;

import com.example.mcp.server.capabilities.ToolRegistry;
import com.example.mcp.server.capabilities.ResourceManager;
import com.example.mcp.server.capabilities.PromptManager;
import com.example.mcp.server.protocol.JsonRpcError;
import com.example.mcp.server.protocol.JsonRpcRequest;
import com.example.mcp.server.protocol.JsonRpcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles MCP protocol message processing and routing.
 * 
 * Processes JSON-RPC requests and dispatches to appropriate handlers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler {
    
    private final ObjectMapper objectMapper;
    private final ToolRegistry toolRegistry;
    private final ResourceManager resourceManager;
    private final PromptManager promptManager;
    
    @Value("${mcp.server.name}")
    private String serverName;
    
    @Value("${mcp.server.version}")
    private String serverVersion;
    
    @Value("${mcp.protocol.version}")
    private String protocolVersion;
    
    private boolean initialized = false;
    
    /**
     * Process a JSON-RPC request and generate a response
     */
    public JsonRpcResponse handleRequest(JsonRpcRequest request) {
        try {
            // Validate request structure
            if (request.getMethod() == null || request.getMethod().isEmpty()) {
                return JsonRpcResponse.error(request.getId(), 
                    JsonRpcError.invalidRequest("Method name is required"));
            }
            
            // If this is a notification, process but don't return response
            if (request.isNotification()) {
                handleNotification(request);
                return null;
            }
            
            // Route to appropriate handler based on method
            return routeRequest(request);
            
        } catch (Exception e) {
            log.error("Error processing request: {}", e.getMessage(), e);
            return JsonRpcResponse.error(request.getId(), 
                JsonRpcError.internalError(e.getMessage()));
        }
    }
    
    /**
     * Route request to the appropriate handler
     */
    private JsonRpcResponse routeRequest(JsonRpcRequest request) {
        String method = request.getMethod();
        
        switch (method) {
            case "initialize":
                return handleInitialize(request);
            case "tools/list":
                return handleToolsList(request);
            case "tools/call":
                return handleToolsCall(request);
            case "resources/list":
                return handleResourcesList(request);
            case "resources/read":
                return handleResourcesRead(request);
            case "prompts/list":
                return handlePromptsList(request);
            case "prompts/get":
                return handlePromptsGet(request);
            default:
                return JsonRpcResponse.error(request.getId(), 
                    JsonRpcError.methodNotFound(method));
        }
    }
    
    /**
     * Handle notifications (no response required)
     */
    private void handleNotification(JsonRpcRequest request) {
        String method = request.getMethod();
        log.debug("Received notification: {}", method);
        
        if ("initialized".equals(method)) {
            initialized = true;
            log.info("Client sent initialized notification - session ready");
        } else if ("cancelled".equals(method)) {
            log.info("Client cancelled operation");
        }
    }
    
    /**
     * Handle initialize request
     */
    private JsonRpcResponse handleInitialize(JsonRpcRequest request) {
        log.info("Processing initialize request");
        
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", protocolVersion);
        
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("tools", Map.of("listChanged", false));
        capabilities.put("resources", Map.of("subscribe", false, "listChanged", false));
        capabilities.put("prompts", Map.of("listChanged", false));
        result.put("capabilities", capabilities);
        
        Map<String, String> serverInfo = new HashMap<>();
        serverInfo.put("name", serverName);
        serverInfo.put("version", serverVersion);
        result.put("serverInfo", serverInfo);
        
        log.info("Server initialized successfully");
        return JsonRpcResponse.success(request.getId(), result);
    }
    
    /**
     * Handle tools/list request
     */
    private JsonRpcResponse handleToolsList(JsonRpcRequest request) {
        if (!initialized) {
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.invalidRequest("Server not initialized"));
        }
        
        log.debug("Listing available tools");
        Map<String, Object> result = new HashMap<>();
        result.put("tools", toolRegistry.listTools());
        
        return JsonRpcResponse.success(request.getId(), result);
    }
    
    /**
     * Handle tools/call request
     */
    @SuppressWarnings("unchecked")
    private JsonRpcResponse handleToolsCall(JsonRpcRequest request) {
        if (!initialized) {
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.invalidRequest("Server not initialized"));
        }
        
        try {
            Map<String, Object> params = objectMapper.convertValue(request.getParams(), Map.class);
            String toolName = (String) params.get("name");
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
            
            if (toolName == null) {
                return JsonRpcResponse.error(request.getId(),
                    JsonRpcError.invalidParams("Tool name is required"));
            }
            
            log.debug("Calling tool: {}", toolName);
            Object result = toolRegistry.callTool(toolName, arguments);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", result);
            
            return JsonRpcResponse.success(request.getId(), response);
            
        } catch (IllegalArgumentException e) {
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.invalidParams(e.getMessage()));
        } catch (Exception e) {
            log.error("Tool execution failed: {}", e.getMessage(), e);
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.internalError(e.getMessage()));
        }
    }
    
    /**
     * Handle resources/list request
     */
    private JsonRpcResponse handleResourcesList(JsonRpcRequest request) {
        if (!initialized) {
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.invalidRequest("Server not initialized"));
        }
        
        log.debug("Listing available resources");
        Map<String, Object> result = new HashMap<>();
        result.put("resources", resourceManager.listResources());
        
        return JsonRpcResponse.success(request.getId(), result);
    }
    
    /**
     * Handle resources/read request
     */
    @SuppressWarnings("unchecked")
    private JsonRpcResponse handleResourcesRead(JsonRpcRequest request) {
        if (!initialized) {
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.invalidRequest("Server not initialized"));
        }
        
        try {
            Map<String, Object> params = objectMapper.convertValue(request.getParams(), Map.class);
            String uri = (String) params.get("uri");
            
            if (uri == null) {
                return JsonRpcResponse.error(request.getId(),
                    JsonRpcError.invalidParams("Resource URI is required"));
            }
            
            log.debug("Reading resource: {}", uri);
            Object content = resourceManager.readResource(uri);
            
            Map<String, Object> response = new HashMap<>();
            response.put("contents", content);
            
            return JsonRpcResponse.success(request.getId(), response);
            
        } catch (IllegalArgumentException e) {
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.invalidParams(e.getMessage()));
        } catch (Exception e) {
            log.error("Resource read failed: {}", e.getMessage(), e);
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.internalError(e.getMessage()));
        }
    }
    
    /**
     * Handle prompts/list request
     */
    private JsonRpcResponse handlePromptsList(JsonRpcRequest request) {
        if (!initialized) {
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.invalidRequest("Server not initialized"));
        }
        
        log.debug("Listing available prompts");
        Map<String, Object> result = new HashMap<>();
        result.put("prompts", promptManager.listPrompts());
        
        return JsonRpcResponse.success(request.getId(), result);
    }
    
    /**
     * Handle prompts/get request
     */
    @SuppressWarnings("unchecked")
    private JsonRpcResponse handlePromptsGet(JsonRpcRequest request) {
        if (!initialized) {
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.invalidRequest("Server not initialized"));
        }
        
        try {
            Map<String, Object> params = objectMapper.convertValue(request.getParams(), Map.class);
            String name = (String) params.get("name");
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
            
            if (name == null) {
                return JsonRpcResponse.error(request.getId(),
                    JsonRpcError.invalidParams("Prompt name is required"));
            }
            
            log.debug("Getting prompt: {}", name);
            Object messages = promptManager.getPrompt(name, arguments);
            
            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages);
            
            return JsonRpcResponse.success(request.getId(), response);
            
        } catch (IllegalArgumentException e) {
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.invalidParams(e.getMessage()));
        } catch (Exception e) {
            log.error("Prompt retrieval failed: {}", e.getMessage(), e);
            return JsonRpcResponse.error(request.getId(),
                JsonRpcError.internalError(e.getMessage()));
        }
    }
}
