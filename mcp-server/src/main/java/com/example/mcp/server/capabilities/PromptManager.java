package com.example.mcp.server.capabilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Manager for MCP prompts.
 * 
 * Manages prompt template registration, discovery, and retrieval.
 */
@Slf4j
@Component
public class PromptManager {
    
    private final Map<String, PromptDefinition> prompts = new ConcurrentHashMap<>();
    private final Map<String, Function<Map<String, Object>, Object>> generators = new ConcurrentHashMap<>();
    
    /**
     * Register a prompt template with its generator
     */
    public void registerPrompt(String name, String description, List<String> arguments,
                              Function<Map<String, Object>, Object> generator) {
        PromptDefinition prompt = new PromptDefinition(name, description, arguments);
        prompts.put(name, prompt);
        generators.put(name, generator);
        log.info("Registered prompt: {}", name);
    }
    
    /**
     * List all available prompts
     */
    public List<Map<String, Object>> listPrompts() {
        List<Map<String, Object>> promptList = new ArrayList<>();
        
        for (PromptDefinition prompt : prompts.values()) {
            Map<String, Object> promptInfo = new HashMap<>();
            promptInfo.put("name", prompt.name);
            promptInfo.put("description", prompt.description);
            
            if (!prompt.arguments.isEmpty()) {
                List<Map<String, Object>> args = new ArrayList<>();
                for (String arg : prompt.arguments) {
                    Map<String, Object> argInfo = new HashMap<>();
                    argInfo.put("name", arg);
                    argInfo.put("required", true);
                    args.add(argInfo);
                }
                promptInfo.put("arguments", args);
            }
            
            promptList.add(promptInfo);
        }
        
        return promptList;
    }
    
    /**
     * Get a prompt with given arguments
     */
    public Object getPrompt(String name, Map<String, Object> arguments) {
        if (!prompts.containsKey(name)) {
            throw new IllegalArgumentException("Prompt not found: " + name);
        }
        
        Function<Map<String, Object>, Object> generator = generators.get(name);
        if (generator == null) {
            throw new IllegalStateException("Prompt generator not found: " + name);
        }
        
        try {
            return generator.apply(arguments != null ? arguments : new HashMap<>());
        } catch (Exception e) {
            log.error("Prompt generation failed for {}: {}", name, e.getMessage(), e);
            throw new RuntimeException("Prompt generation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Prompt definition class
     */
    private static class PromptDefinition {
        final String name;
        final String description;
        final List<String> arguments;
        
        PromptDefinition(String name, String description, List<String> arguments) {
            this.name = name;
            this.description = description;
            this.arguments = arguments != null ? arguments : new ArrayList<>();
        }
    }
}
