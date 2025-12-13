# MCP Server

A complete implementation of the Model Context Protocol (MCP) server using Java and Spring Boot.

## Overview

This MCP server provides a shopping cart management system that demonstrates the full capabilities of the MCP protocol including:
- **Tools**: Executable functions for cart operations
- **Resources**: Accessible data sources (cart state, product catalog)
- **Prompts**: Pre-defined templates for AI interactions

## Features

### Tools
- `addToCart` - Add items to shopping cart
- `removeFromCart` - Remove items from cart
- `calculateTotal` - Calculate cart total with breakdown
- `clearCart` - Clear all cart items
- `getCart` - Retrieve current cart contents

### Resources
- `cart://current` - Current shopping cart state (JSON)
- `products://catalog` - Available products catalog (JSON)

### Prompts
- `assistPurchase` - Guide customer through purchase process
- `recommendProducts` - Recommend products based on needs

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building

Build the server using Maven:

```bash
cd mcp-server
mvn clean package
```

This will create `target/mcp-server-1.0.0.jar`

## Running

The server runs via STDIO transport and is typically started by an MCP client:

```bash
java -jar target/mcp-server-1.0.0.jar
```

**Note**: The server expects JSON-RPC messages on STDIN and writes responses to STDOUT. It's designed to be launched by MCP clients rather than run standalone.

## Configuration

Configure the server by editing `src/main/resources/application.properties`:

```properties
mcp.server.name=Shopping Cart MCP Server
mcp.server.version=1.0.0
mcp.protocol.version=2024-11-05
mcp.transport.type=stdio
```

## Protocol Support

This server implements the MCP protocol version 2024-11-05 with support for:
- Session initialization and capability negotiation
- Tool discovery and execution
- Resource listing and retrieval
- Prompt template management

## Architecture

```
mcp-server/
├── core/              # Protocol and transport handling
├── capabilities/      # Tool, Resource, and Prompt managers
├── tools/            # Shopping cart tool implementations
├── resources/        # Resource providers
└── prompts/          # Prompt template generators
```

## Example Tool Usage

### Add Item to Cart

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "addToCart",
    "arguments": {
      "productId": "PROD-001",
      "quantity": 2,
      "price": 999.99
    }
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Successfully added PROD-001 (quantity: 2, price: $999.99) to cart..."
      }
    ]
  }
}
```

## Development

### Adding New Tools

1. Create a new tool class in `tools/` package
2. Implement tool logic with `@PostConstruct` registration
3. Tools automatically become discoverable by clients

### Adding New Resources

1. Create a resource provider in `resources/` package
2. Register resources with ResourceManager
3. Implement content retrieval logic

### Extending the Server

The server is designed for easy extension:
- Add new tools by registering with ToolRegistry
- Add new resources by registering with ResourceManager
- Add new prompts by registering with PromptManager

## Logging

Logging is configured in `application.properties`. Adjust log levels as needed:
- `ERROR` - Critical failures only
- `INFO` - Key events (default)
- `DEBUG` - Detailed protocol flow
- `TRACE` - Full JSON-RPC messages

## License

This is a demonstration implementation of the MCP protocol.

## Support

For MCP protocol specification, see: https://modelcontextprotocol.io/
