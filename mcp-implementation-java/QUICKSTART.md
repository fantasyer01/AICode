# MCP Java Implementation - Quick Start Guide

## Prerequisites

Before you begin, ensure you have:
- **Java 17 or higher**: Download from [https://adoptium.net/](https://adoptium.net/)
- **Maven 3.6+**: Download from [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)

## Installation Steps

### 1. Verify Java Installation

```bash
java -version
```

Expected output should show Java 17 or higher:
```
openjdk version "17.0.x"
```

### 2. Verify Maven Installation

```bash
mvn -version
```

Expected output:
```
Apache Maven 3.x.x
```

### 3. Build the MCP Server

```bash
cd mcp-server
mvn clean package
```

This creates: `target/mcp-server-1.0.0.jar`

### 4. Build the MCP Client

```bash
cd ../mcp-client
mvn clean package
```

This creates: `target/mcp-client-1.0.0.jar`

## Running the System

### Option 1: Using the Client (Recommended)

The client automatically starts the server:

```bash
cd mcp-client
java -jar target/mcp-client-1.0.0.jar ../mcp-server/target/mcp-server-1.0.0.jar
```

You'll see:
```
╔═══════════════════════════════════════════╗
║   MCP Client Interactive Shell v1.0      ║
║   Model Context Protocol Client          ║
╚═══════════════════════════════════════════╝

Available commands:
  help                              - Show this help message
  ...

> 
```

## Example Session

Once the shell starts, try these commands:

### 1. Check Server Information
```bash
> server-info
```

### 2. List Available Tools
```bash
> list-tools
```

You'll see 5 tools:
- addToCart
- removeFromCart
- calculateTotal
- clearCart
- getCart

### 3. View Product Catalog
```bash
> read-resource --uri=products://catalog
```

### 4. Add Items to Cart
```bash
> call-tool --name=addToCart --args='{"productId":"PROD-001","quantity":1,"price":999.99}'
> call-tool --name=addToCart --args='{"productId":"PROD-002","quantity":2,"price":29.99}'
```

### 5. View Cart Contents
```bash
> read-resource --uri=cart://current
```

### 6. Calculate Total
```bash
> call-tool --name=calculateTotal
```

### 7. Clear Cart
```bash
> call-tool --name=clearCart
```

### 8. List Available Prompts
```bash
> list-prompts
```

### 9. Get a Prompt Template
```bash
> get-prompt --name=assistPurchase --args='{"customerName":"Alice"}'
```

### 10. Exit
```bash
> exit
```

## Complete Shopping Scenario

Here's a complete shopping flow:

```bash
# Start client
java -jar target/mcp-client-1.0.0.jar ../mcp-server/target/mcp-server-1.0.0.jar

# View what's available
> read-resource --uri=products://catalog

# Add laptop to cart
> call-tool --name=addToCart --args='{"productId":"PROD-001","quantity":1,"price":999.99}'

# Add mouse to cart
> call-tool --name=addToCart --args='{"productId":"PROD-002","quantity":1,"price":29.99}'

# Add keyboard to cart
> call-tool --name=addToCart --args='{"productId":"PROD-003","quantity":1,"price":79.99}'

# Check what's in the cart
> read-resource --uri=cart://current

# Calculate total
> call-tool --name=calculateTotal

# Remove one item
> call-tool --name=removeFromCart --args='{"productId":"PROD-002"}'

# Recalculate
> call-tool --name=calculateTotal

# Clear everything
> call-tool --name=clearCart

# Exit
> exit
```

## Command Reference

### Tools Commands

**List all tools:**
```bash
> list-tools
```

**Call a tool:**
```bash
> call-tool --name=<toolName> --args='<jsonArgs>'
```

### Resources Commands

**List all resources:**
```bash
> list-resources
```

**Read a resource:**
```bash
> read-resource --uri=<resourceUri>
```

### Prompts Commands

**List all prompts:**
```bash
> list-prompts
```

**Get a prompt:**
```bash
> get-prompt --name=<promptName> --args='<jsonArgs>'
```

### Other Commands

**Show server info:**
```bash
> server-info
```

**Show help:**
```bash
> help
```

**Exit shell:**
```bash
> exit
```

## Troubleshooting

### Maven Not Found

**Windows (PowerShell):**
```powershell
# Download Maven from https://maven.apache.org/download.cgi
# Extract to C:\Program Files\Apache\maven
$env:PATH += ";C:\Program Files\Apache\maven\bin"
mvn -version
```

**macOS/Linux:**
```bash
# Using Homebrew (macOS)
brew install maven

# Using apt (Ubuntu/Debian)
sudo apt install maven

# Using yum (CentOS/RHEL)
sudo yum install maven
```

### Java Version Issues

If you see "unsupported class file version" errors:
```bash
# Check Java version
java -version

# If less than 17, download Java 17 from:
# https://adoptium.net/
```

### Build Errors

If Maven build fails:
```bash
# Clean and rebuild
mvn clean install -U
```

### Client Won't Connect

Ensure the server JAR path is correct:
```bash
# Use absolute path
java -jar target/mcp-client-1.0.0.jar C:\path\to\mcp-server-1.0.0.jar

# Or relative path
java -jar target/mcp-client-1.0.0.jar ../mcp-server/target/mcp-server-1.0.0.jar
```

### JSON Syntax Errors

When using JSON arguments:
- Use single quotes around the entire JSON: `'{"key":"value"}'`
- Use double quotes inside JSON: `{"key":"value"}`
- Validate JSON at: https://jsonlint.com/

## Windows-Specific Notes

### Using PowerShell
```powershell
cd mcp-client
java -jar target/mcp-client-1.0.0.jar ..\mcp-server\target\mcp-server-1.0.0.jar
```

### Using Command Prompt
```cmd
cd mcp-client
java -jar target\mcp-client-1.0.0.jar ..\mcp-server\target\mcp-server-1.0.0.jar
```

### Path Separators
- PowerShell/CMD use backslash: `\`
- The application accepts both: `/` or `\`

## macOS/Linux-Specific Notes

```bash
# Build both
cd mcp-server && mvn clean package && cd ../mcp-client && mvn clean package

# Run
cd mcp-client
java -jar target/mcp-client-1.0.0.jar ../mcp-server/target/mcp-server-1.0.0.jar
```

## Next Steps

1. **Explore all tools**: Try each of the 5 shopping cart tools
2. **Read resources**: View both cart://current and products://catalog
3. **Try prompts**: Experiment with prompt templates
4. **Review code**: Check the implementation in both projects
5. **Extend**: Add your own tools, resources, or prompts

## Support

For issues:
1. Check logs in console output
2. Verify Java and Maven versions
3. Ensure correct file paths
4. Review error messages carefully

## Additional Resources

- **MCP Specification**: https://modelcontextprotocol.io/
- **Java Documentation**: https://docs.oracle.com/en/java/javase/17/
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Maven Guide**: https://maven.apache.org/guides/
