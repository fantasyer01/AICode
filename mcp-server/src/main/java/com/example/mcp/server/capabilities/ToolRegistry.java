package com.example.mcp.server.capabilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Registry for MCP tools.
 * 
 * Manages tool registration, discovery, and execution.
 */
@Slf4j
@Component
public class ToolRegistry {
    
    private final Map<String, ToolDefinition> tools = new ConcurrentHashMap<>();
    private final Map<String, Function<Map<String, Object>, Object>> handlers = new ConcurrentHashMap<>();
    
    /**
     * Register a tool with its handler
     */
    public void registerTool(String name, String description, Map<String, Object> inputSchema,
                            Function<Map<String, Object>, Object> handler) {
        ToolDefinition tool = new ToolDefinition(name, description, inputSchema);
        tools.put(name, tool);
        handlers.put(name, handler);
        log.info("Registered tool: {}", name);
    }
    
    /**
     * List all available tools
     */
    public List<Map<String, Object>> listTools() {
        List<Map<String, Object>> toolList = new ArrayList<>();
        
        for (ToolDefinition tool : tools.values()) {
            Map<String, Object> toolInfo = new HashMap<>();
            toolInfo.put("name", tool.name);
            toolInfo.put("description", tool.description);
            toolInfo.put("inputSchema", tool.inputSchema);
            toolList.add(toolInfo);
        }
        
        return toolList;
    }
    
    /**
     * Call a tool with given arguments
     */
    public Object callTool(String name, Map<String, Object> arguments) {
        if (!tools.containsKey(name)) {
            throw new IllegalArgumentException("Tool not found: " + name);
        }
        
        Function<Map<String, Object>, Object> handler = handlers.get(name);
        if (handler == null) {
            throw new IllegalStateException("Tool handler not found: " + name);
        }
        
        try {
            return handler.apply(arguments != null ? arguments : new HashMap<>());
        } catch (Exception e) {
            log.error("Tool execution failed for {}: {}", name, e.getMessage(), e);
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tool definition class
     */
    private static class ToolDefinition {
        final String name;
        final String description;
        final Map<String, Object> inputSchema;
        
        ToolDefinition(String name, String description, Map<String, Object> inputSchema) {
            this.name = name;
            this.description = description;
            this.inputSchema = inputSchema;
        }
    }
}
