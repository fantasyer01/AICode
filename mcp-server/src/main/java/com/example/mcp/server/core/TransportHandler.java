package com.example.mcp.server.core;

import com.example.mcp.server.protocol.JsonRpcRequest;
import com.example.mcp.server.protocol.JsonRpcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Handles STDIO transport for MCP protocol.
 * 
 * Reads JSON-RPC messages from standard input and writes responses to standard output.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransportHandler {
    
    private final ObjectMapper objectMapper;
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final PrintWriter writer = new PrintWriter(System.out, true);
    
    /**
     * Read a JSON-RPC request from STDIN
     * 
     * @return JsonRpcRequest or null if stream is closed
     * @throws IOException if reading fails
     */
    public JsonRpcRequest readRequest() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            log.debug("STDIN stream closed");
            return null;
        }
        
        log.trace("Received: {}", line);
        
        try {
            return objectMapper.readValue(line, JsonRpcRequest.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON-RPC request: {}", e.getMessage());
            throw new IOException("Invalid JSON-RPC message", e);
        }
    }
    
    /**
     * Write a JSON-RPC response to STDOUT
     * 
     * @param response The response to send
     * @throws IOException if writing fails
     */
    public void writeResponse(JsonRpcResponse response) throws IOException {
        try {
            String json = objectMapper.writeValueAsString(response);
            log.trace("Sending: {}", json);
            writer.println(json);
            writer.flush();
        } catch (Exception e) {
            log.error("Failed to serialize JSON-RPC response: {}", e.getMessage());
            throw new IOException("Failed to send response", e);
        }
    }
    
    /**
     * Check if the transport is still active
     */
    public boolean isActive() {
        return !writer.checkError();
    }
}
