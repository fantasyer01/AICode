package com.example.mcp.client.shell;

import com.example.mcp.client.core.McpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interactive command-line shell for MCP client.
 * 
 * Provides a user-friendly interface for interacting with the MCP server.
 */
public class InteractiveShell {
    
    private static final Logger log = LoggerFactory.getLogger(InteractiveShell.class);
    
    private final McpClient client;
    private final BufferedReader reader;
    private final ObjectMapper objectMapper;
    private final CommandParser commandParser;
    
    public InteractiveShell(McpClient client) {
        this.client = client;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.objectMapper = new ObjectMapper();
        this.commandParser = new CommandParser();
    }
    
    /**
     * Start the interactive shell
     */
    public void start() {
        printWelcome();
        printHelp();
        
        try {
            String line;
            while ((line = readLine("> ")) != null) {
                line = line.trim();
                
                if (line.isEmpty()) {
                    continue;
                }
                
                if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                    println("Goodbye!");
                    break;
                }
                
                if ("help".equalsIgnoreCase(line)) {
                    printHelp();
                    continue;
                }
                
                try {
                    executeCommand(line);
                } catch (Exception e) {
                    printError("Error: " + e.getMessage());
                    log.error("Command execution error", e);
                }
            }
        } catch (Exception e) {
            printError("Shell error: " + e.getMessage());
            log.error("Shell error", e);
        }
    }
    
    /**
     * Execute a command
     */
    private void executeCommand(String commandLine) throws Exception {
        Command cmd = commandParser.parse(commandLine);
        
        switch (cmd.getCommand()) {
            case "list-tools":
                listTools();
                break;
                
            case "call-tool":
                callTool(cmd);
                break;
                
            case "list-resources":
                listResources();
                break;
                
            case "read-resource":
                readResource(cmd);
                break;
                
            case "list-prompts":
                listPrompts();
                break;
                
            case "get-prompt":
                getPrompt(cmd);
                break;
                
            case "server-info":
                serverInfo();
                break;
                
            default:
                printError("Unknown command: " + cmd.getCommand());
                println("Type 'help' for available commands.");
        }
    }
    
    /**
     * List available tools
     */
    private void listTools() throws Exception {
        List<Map<String, Object>> tools = client.listTools();
        
        println("\n=== Available Tools ===");
        for (Map<String, Object> tool : tools) {
            println("\nTool: " + tool.get("name"));
            println("  Description: " + tool.get("description"));
            println("  Schema: " + objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(tool.get("inputSchema")));
        }
        println("\nTotal tools: " + tools.size());
    }
    
    /**
     * Call a tool
     */
    private void callTool(Command cmd) throws Exception {
        String toolName = cmd.getArg("name");
        if (toolName == null) {
            printError("Tool name required. Usage: call-tool --name=<toolName> [--args=<json>]");
            return;
        }
        
        Map<String, Object> arguments = new HashMap<>();
        String argsJson = cmd.getArg("args");
        if (argsJson != null) {
            arguments = objectMapper.readValue(argsJson, Map.class);
        }
        
        println("\nCalling tool: " + toolName);
        Object result = client.callTool(toolName, arguments);
        println("\n=== Tool Result ===");
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }
    
    /**
     * List available resources
     */
    private void listResources() throws Exception {
        List<Map<String, Object>> resources = client.listResources();
        
        println("\n=== Available Resources ===");
        for (Map<String, Object> resource : resources) {
            println("\nResource: " + resource.get("uri"));
            println("  Name: " + resource.get("name"));
            println("  Description: " + resource.get("description"));
            println("  MIME Type: " + resource.get("mimeType"));
        }
        println("\nTotal resources: " + resources.size());
    }
    
    /**
     * Read a resource
     */
    private void readResource(Command cmd) throws Exception {
        String uri = cmd.getArg("uri");
        if (uri == null) {
            printError("Resource URI required. Usage: read-resource --uri=<resourceUri>");
            return;
        }
        
        println("\nReading resource: " + uri);
        Object content = client.readResource(uri);
        println("\n=== Resource Content ===");
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(content));
    }
    
    /**
     * List available prompts
     */
    private void listPrompts() throws Exception {
        List<Map<String, Object>> prompts = client.listPrompts();
        
        println("\n=== Available Prompts ===");
        for (Map<String, Object> prompt : prompts) {
            println("\nPrompt: " + prompt.get("name"));
            println("  Description: " + prompt.get("description"));
            if (prompt.containsKey("arguments")) {
                println("  Arguments: " + objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(prompt.get("arguments")));
            }
        }
        println("\nTotal prompts: " + prompts.size());
    }
    
    /**
     * Get a prompt
     */
    private void getPrompt(Command cmd) throws Exception {
        String promptName = cmd.getArg("name");
        if (promptName == null) {
            printError("Prompt name required. Usage: get-prompt --name=<promptName> [--args=<json>]");
            return;
        }
        
        Map<String, Object> arguments = new HashMap<>();
        String argsJson = cmd.getArg("args");
        if (argsJson != null) {
            arguments = objectMapper.readValue(argsJson, Map.class);
        }
        
        println("\nGetting prompt: " + promptName);
        Object messages = client.getPrompt(promptName, arguments);
        println("\n=== Prompt Messages ===");
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messages));
    }
    
    /**
     * Show server information
     */
    private void serverInfo() {
        println("\n=== Server Information ===");
        Map<String, Object> info = client.getServerInfo();
        println("Name: " + info.get("name"));
        println("Version: " + info.get("version"));
        
        println("\n=== Server Capabilities ===");
        try {
            println(objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(client.getServerCapabilities()));
        } catch (Exception e) {
            printError("Error displaying capabilities: " + e.getMessage());
        }
    }
    
    /**
     * Print welcome message
     */
    private void printWelcome() {
        println("\n╔═══════════════════════════════════════════╗");
        println("║   MCP Client Interactive Shell v1.0      ║");
        println("║   Model Context Protocol Client          ║");
        println("╚═══════════════════════════════════════════╝");
        println();
    }
    
    /**
     * Print help message
     */
    private void printHelp() {
        println("Available commands:");
        println("  help                              - Show this help message");
        println("  server-info                       - Show server information");
        println("  list-tools                        - List available tools");
        println("  call-tool --name=<name> [--args=<json>]  - Call a tool");
        println("  list-resources                    - List available resources");
        println("  read-resource --uri=<uri>         - Read a resource");
        println("  list-prompts                      - List available prompts");
        println("  get-prompt --name=<name> [--args=<json>] - Get a prompt");
        println("  exit                              - Exit the shell");
        println();
        println("Example:");
        println("  call-tool --name=addToCart --args='{\"productId\":\"PROD-001\",\"quantity\":2,\"price\":999.99}'");
        println();
    }
    
    /**
     * Read a line from input
     */
    private String readLine(String prompt) {
        try {
            System.out.print(prompt);
            return reader.readLine();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Print a message
     */
    private void println(String message) {
        System.out.println(message);
    }
    
    /**
     * Print without newline
     */
    private void println() {
        System.out.println();
    }
    
    /**
     * Print an error message
     */
    private void printError(String message) {
        System.err.println(message);
    }
}
