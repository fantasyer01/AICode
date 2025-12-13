package com.example.mcp.server.capabilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Manager for MCP resources.
 * 
 * Manages resource registration, discovery, and access.
 */
@Slf4j
@Component
public class ResourceManager {
    
    private final Map<String, ResourceDefinition> resources = new ConcurrentHashMap<>();
    private final Map<String, Supplier<Object>> providers = new ConcurrentHashMap<>();
    
    /**
     * Register a resource with its provider
     */
    public void registerResource(String uri, String name, String description, 
                                String mimeType, Supplier<Object> provider) {
        ResourceDefinition resource = new ResourceDefinition(uri, name, description, mimeType);
        resources.put(uri, resource);
        providers.put(uri, provider);
        log.info("Registered resource: {}", uri);
    }
    
    /**
     * List all available resources
     */
    public List<Map<String, Object>> listResources() {
        List<Map<String, Object>> resourceList = new ArrayList<>();
        
        for (ResourceDefinition resource : resources.values()) {
            Map<String, Object> resourceInfo = new HashMap<>();
            resourceInfo.put("uri", resource.uri);
            resourceInfo.put("name", resource.name);
            resourceInfo.put("description", resource.description);
            resourceInfo.put("mimeType", resource.mimeType);
            resourceList.add(resourceInfo);
        }
        
        return resourceList;
    }
    
    /**
     * Read a resource by URI
     */
    public Object readResource(String uri) {
        if (!resources.containsKey(uri)) {
            throw new IllegalArgumentException("Resource not found: " + uri);
        }
        
        Supplier<Object> provider = providers.get(uri);
        if (provider == null) {
            throw new IllegalStateException("Resource provider not found: " + uri);
        }
        
        try {
            Object content = provider.get();
            
            ResourceDefinition resource = resources.get(uri);
            Map<String, Object> result = new HashMap<>();
            result.put("uri", uri);
            result.put("mimeType", resource.mimeType);
            result.put("text", content);
            
            return List.of(result);
        } catch (Exception e) {
            log.error("Resource read failed for {}: {}", uri, e.getMessage(), e);
            throw new RuntimeException("Resource read failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Resource definition class
     */
    private static class ResourceDefinition {
        final String uri;
        final String name;
        final String description;
        final String mimeType;
        
        ResourceDefinition(String uri, String name, String description, String mimeType) {
            this.uri = uri;
            this.name = name;
            this.description = description;
            this.mimeType = mimeType;
        }
    }
}
