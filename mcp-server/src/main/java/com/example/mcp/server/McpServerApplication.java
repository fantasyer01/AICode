package com.example.mcp.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP Server Spring Boot Application.
 * 
 * Main entry point for the Model Context Protocol server.
 */
@SpringBootApplication
public class McpServerApplication {
    
    public static void main(String[] args) {
        // Disable Spring Boot banner for cleaner STDIO communication
        SpringApplication app = new SpringApplication(McpServerApplication.class);
        app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);
        app.run(args);
    }
}
