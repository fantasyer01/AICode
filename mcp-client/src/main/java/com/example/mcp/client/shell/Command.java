package com.example.mcp.client.shell;

import java.util.Map;

/**
 * Represents a parsed command.
 */
public class Command {
    
    private final String command;
    private final Map<String, String> args;
    
    public Command(String command, Map<String, String> args) {
        this.command = command;
        this.args = args;
    }
    
    public String getCommand() {
        return command;
    }
    
    public String getArg(String name) {
        return args.get(name);
    }
    
    public Map<String, String> getArgs() {
        return args;
    }
}
