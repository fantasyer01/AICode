package com.example.mcp.client.shell;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses command-line input into structured commands.
 */
public class CommandParser {
    
    private static final Pattern ARG_PATTERN = Pattern.compile("--([\\w-]+)=([\"'])(.*?)\\2|--([\\w-]+)=(\\S+)");
    
    /**
     * Parse a command line into a Command object
     */
    public Command parse(String commandLine) {
        commandLine = commandLine.trim();
        
        // Extract command name (first word)
        int spaceIdx = commandLine.indexOf(' ');
        String commandName;
        String argsString;
        
        if (spaceIdx == -1) {
            commandName = commandLine;
            argsString = "";
        } else {
            commandName = commandLine.substring(0, spaceIdx);
            argsString = commandLine.substring(spaceIdx + 1).trim();
        }
        
        // Parse arguments
        Map<String, String> args = parseArguments(argsString);
        
        return new Command(commandName, args);
    }
    
    /**
     * Parse argument string
     */
    private Map<String, String> parseArguments(String argsString) {
        Map<String, String> args = new HashMap<>();
        
        if (argsString.isEmpty()) {
            return args;
        }
        
        Matcher matcher = ARG_PATTERN.matcher(argsString);
        while (matcher.find()) {
            String name;
            String value;
            
            if (matcher.group(1) != null) {
                // Quoted value
                name = matcher.group(1);
                value = matcher.group(3);
            } else {
                // Unquoted value
                name = matcher.group(4);
                value = matcher.group(5);
            }
            
            args.put(name, value);
        }
        
        return args;
    }
}
