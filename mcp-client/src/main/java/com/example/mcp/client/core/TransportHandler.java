package com.example.mcp.client.core;

import com.example.mcp.client.protocol.JsonRpcRequest;
import com.example.mcp.client.protocol.JsonRpcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Handles STDIO transport for MCP client.
 * 
 * Communicates with server via standard input/output of server process.
 */
public class TransportHandler {
    
    private static final Logger log = LoggerFactory.getLogger(TransportHandler.class);
    
    private final ObjectMapper objectMapper;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Process serverProcess;
    
    public TransportHandler(Process serverProcess, ObjectMapper objectMapper) {
        this.serverProcess = serverProcess;
        this.objectMapper = objectMapper;
        this.reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
        this.writer = new PrintWriter(new OutputStreamWriter(serverProcess.getOutputStream()), true);
    }
    
    /**
     * Send a JSON-RPC request to the server
     */
    public void sendRequest(JsonRpcRequest request) throws IOException {
        try {
            String json = objectMapper.writeValueAsString(request);
            log.info("Sending: {}", json);
            writer.println(json);
            writer.flush();
        } catch (Exception e) {
            log.error("Failed to send request: {}", e.getMessage());
            throw new IOException("Failed to send request", e);
        }
    }
    
    /**
     * Read a JSON-RPC response from the server
     */
    public JsonRpcResponse readResponse() throws IOException {
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    log.debug("Server closed connection");
                    return null;
                }
                
                // Skip empty lines
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                // Check if line looks like JSON (starts with '{')
                if (!line.startsWith("{")) {
                    log.warn("Skipping non-JSON line from server: {}", line);
                    continue;
                }
                
                log.info("Received: {}", line);
                return objectMapper.readValue(line, JsonRpcResponse.class);
            }
            
        } catch (Exception e) {
            log.error("Failed to read response: {}", e.getMessage());
            throw new IOException("Failed to read response", e);
        }
    }
    
    /**
     * Check if the connection is still alive
     */
    public boolean isActive() {
        return serverProcess.isAlive();
    }
    
    /**
     * Close the connection
     */
    public void close() {
        try {
            writer.close();
            reader.close();
            serverProcess.destroy();
            serverProcess.waitFor();
            log.info("Connection closed");
        } catch (Exception e) {
            log.error("Error closing connection: {}", e.getMessage());
        }
    }
}
