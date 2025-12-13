package com.example.mcp.client;

import com.example.mcp.client.core.McpClient;
import com.example.mcp.client.shell.InteractiveShell;

/**
 * MCP Client Application.
 * 
 * Main entry point for the Model Context Protocol client.
 */
public class McpClientApplication {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java -jar mcp-client.jar <server-jar-path>");
            System.err.println("Example: java -jar mcp-client.jar ../mcp-server/target/mcp-server-1.0.0.jar");
            System.exit(1);
        }
        
        String serverJarPath = args[0];
        
        try {
            // Create and connect client
            McpClient client = new McpClient(serverJarPath);
            
            // Start interactive shell
            InteractiveShell shell = new InteractiveShell(client);
            shell.start();
            
            // Cleanup
            client.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
