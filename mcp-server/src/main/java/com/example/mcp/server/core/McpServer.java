package com.example.mcp.server.core;

import com.example.mcp.server.protocol.JsonRpcError;
import com.example.mcp.server.protocol.JsonRpcRequest;
import com.example.mcp.server.protocol.JsonRpcResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Core MCP Server implementation.
 * 
 * Manages the server lifecycle and processes incoming requests.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpServer implements CommandLineRunner {
    
    private final TransportHandler transportHandler;
    private final MessageHandler messageHandler;
    
    /**
     * Start the MCP server and begin processing requests
     */
    @Override
    public void run(String... args) {
        log.info("MCP Server starting...");
        log.info("Ready to accept connections via STDIO");
        
        try {
            processRequests();
        } catch (Exception e) {
            log.error("Server error: {}", e.getMessage(), e);
            System.exit(1);
        }
        
        log.info("MCP Server shutting down");
    }
    
    /**
     * Main request processing loop
     */
    private void processRequests() {
        while (transportHandler.isActive()) {
            try {
                // Read request from STDIN
                JsonRpcRequest request = transportHandler.readRequest();
                
                // Null means stream closed
                if (request == null) {
                    log.info("Client closed connection");
                    break;
                }
                
                // Process the request
                JsonRpcResponse response = messageHandler.handleRequest(request);
                
                // Send response if not a notification
                if (response != null) {
                    transportHandler.writeResponse(response);
                }
                
            } catch (IOException e) {
                log.error("Transport error: {}", e.getMessage());
                // Try to send error response if possible
                try {
                    JsonRpcResponse errorResponse = JsonRpcResponse.error(null,
                        JsonRpcError.parseError(e.getMessage()));
                    transportHandler.writeResponse(errorResponse);
                } catch (IOException ex) {
                    log.error("Failed to send error response: {}", ex.getMessage());
                    break;
                }
            } catch (Exception e) {
                log.error("Unexpected error: {}", e.getMessage(), e);
                break;
            }
        }
    }
}
