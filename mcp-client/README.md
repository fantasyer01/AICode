# MCP Client

An interactive command-line client for the Model Context Protocol (MCP) implemented in Java.

## Overview

This MCP client provides an interactive shell interface for communicating with MCP servers. It demonstrates the complete MCP protocol lifecycle including initialization, capability negotiation, and executing operations.

## Features

- **Interactive Shell**: User-friendly command-line interface
- **Full MCP Support**: Tools, Resources, and Prompts
- **Session Management**: Automatic connection and lifecycle handling
- **Pretty Output**: JSON formatting for easy reading
- **Error Handling**: Comprehensive error reporting

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- A running MCP server JAR file

## Building

Build the client using Maven:

```bash
cd mcp-client
mvn clean package
```

This will create `target/mcp-client-1.0.0.jar`

## Running

Launch the client with the path to the MCP server JAR:

```bash
java -jar target/mcp-client-1.0.0.jar <path-to-server.jar>
```

**Example:**
```bash
java -jar target/mcp-client-1.0.0.jar ../mcp-server/target/mcp-server-1.0.0.jar
```

## Usage

Once started, you'll see an interactive shell:

```
╔═══════════════════════════════════════════╗
║   MCP Client Interactive Shell v1.0      ║
║   Model Context Protocol Client          ║
╚═══════════════════════════════════════════╝

Available commands:
  help                              - Show this help message
  server-info                       - Show server information
  list-tools                        - List available tools
  call-tool --name=<name> [--args=<json>]  - Call a tool
  list-resources                    - List available resources
  read-resource --uri=<uri>         - Read a resource
  list-prompts                      - List available prompts
  get-prompt --name=<name> [--args=<json>] - Get a prompt
  exit                              - Exit the shell

> 
```

## Command Examples

### Server Information
```bash
> server-info
```

Shows connected server details and capabilities.

### List Available Tools
```bash
> list-tools
```

Displays all tools exposed by the server with descriptions and schemas.

### Call a Tool
```bash
> call-tool --name=addToCart --args='{"productId":"PROD-001","quantity":2,"price":999.99}'
```

Executes the `addToCart` tool with the specified JSON arguments.

### List Resources
```bash
> list-resources
```

Shows all available resources with URIs and descriptions.

### Read a Resource
```bash
> read-resource --uri=cart://current
```

Retrieves and displays the content of the specified resource.

### List Prompts
```bash
> list-prompts
```

Displays all available prompt templates.

### Get a Prompt
```bash
> get-prompt --name=assistPurchase --args='{"customerName":"John"}'
```

Retrieves a prompt template with arguments filled in.

## Complete Example Session

```bash
# Start the client
> java -jar mcp-client-1.0.0.jar ../mcp-server/target/mcp-server-1.0.0.jar

# Check server info
> server-info

# View available products
> read-resource --uri=products://catalog

# Add items to cart
> call-tool --name=addToCart --args='{"productId":"PROD-001","quantity":1,"price":999.99}'
> call-tool --name=addToCart --args='{"productId":"PROD-002","quantity":2,"price":29.99}'

# Check cart contents
> read-resource --uri=cart://current

# Calculate total
> call-tool --name=calculateTotal

# Clear cart
> call-tool --name=clearCart

# Exit
> exit
```

## Architecture

```
mcp-client/
├── core/              # Client core and transport handling
├── protocol/          # JSON-RPC message models
├── session/           # Session and request management
└── shell/             # Interactive shell and command parsing
```

## Command Format

Commands follow this general format:
```
<command-name> [--arg1=value1] [--arg2=value2]
```

### Argument Rules
- Arguments are specified with `--name=value`
- JSON arguments should be enclosed in single quotes
- Multiple arguments are space-separated

### JSON Arguments
For complex data structures, use JSON format:
```bash
call-tool --name=toolName --args='{"key1":"value1","key2":123,"key3":true}'
```

## Configuration

The client uses default configuration optimized for MCP communication. Logging can be adjusted in `src/main/resources/logback.xml`.

## Troubleshooting

### Connection Issues
- Ensure the server JAR path is correct
- Verify Java 17+ is installed
- Check that the server JAR is executable

### Command Errors
- Verify command syntax matches examples
- Use single quotes for JSON arguments
- Check tool/resource names with list commands

### JSON Parsing Errors
- Ensure JSON is valid (use online validator)
- Escape special characters in strings
- Use double quotes for JSON keys/values

## Development

### Adding New Commands

1. Add command to `InteractiveShell.java`
2. Implement command handler method
3. Update help text

### Customizing Output

Modify the `InteractiveShell` class to customize:
- Output formatting
- Color schemes
- Display layouts

## Technical Details

### Protocol Implementation
- **Transport**: STDIO (Standard Input/Output)
- **Message Format**: JSON-RPC 2.0
- **Protocol Version**: 2024-11-05

### Session Management
- Automatic initialization handshake
- Request/response correlation
- Timeout handling (30 seconds default)
- Graceful connection cleanup

### Threading Model
- Background response listener thread
- Non-blocking request sending
- CompletableFuture for async responses

## Limitations

- Single server connection at a time
- STDIO transport only (no HTTP/WebSocket)
- Interactive mode only (no batch commands)

## License

This is a demonstration implementation of the MCP protocol.

## Support

For MCP protocol specification, see: https://modelcontextprotocol.io/
