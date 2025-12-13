package com.example.mcp.server.resources;

import com.example.mcp.server.capabilities.ResourceManager;
import com.example.mcp.server.tools.ShoppingCartTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource provider for shopping cart data.
 * 
 * Exposes cart state as MCP resources.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartResourceProvider {
    
    private final ResourceManager resourceManager;
    private final ShoppingCartTools shoppingCartTools;
    private final ObjectMapper objectMapper;
    
    @PostConstruct
    public void registerResources() {
        registerCurrentCartResource();
        registerProductCatalogResource();
    }
    
    /**
     * Register the current cart resource
     */
    private void registerCurrentCartResource() {
        resourceManager.registerResource(
            "cart://current",
            "Current Cart",
            "Current shopping cart contents with all items",
            "application/json",
            this::getCurrentCart
        );
    }
    
    /**
     * Register the product catalog resource
     */
    private void registerProductCatalogResource() {
        resourceManager.registerResource(
            "products://catalog",
            "Product Catalog",
            "Available products catalog",
            "application/json",
            this::getProductCatalog
        );
    }
    
    /**
     * Get current cart as JSON
     */
    private Object getCurrentCart() {
        try {
            List<ShoppingCartTools.CartItem> cart = shoppingCartTools.getCurrentCart();
            
            Map<String, Object> cartData = new HashMap<>();
            cartData.put("items", cart);
            cartData.put("itemCount", cart.size());
            
            double total = cart.stream()
                .mapToDouble(item -> item.quantity * item.price)
                .sum();
            cartData.put("total", total);
            
            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(cartData);
                
        } catch (Exception e) {
            log.error("Failed to get cart data", e);
            return "{\"error\": \"Failed to retrieve cart data\"}";
        }
    }
    
    /**
     * Get product catalog
     */
    private Object getProductCatalog() {
        try {
            List<Map<String, Object>> products = List.of(
                createProduct("PROD-001", "Laptop", 999.99, "High-performance laptop"),
                createProduct("PROD-002", "Mouse", 29.99, "Wireless ergonomic mouse"),
                createProduct("PROD-003", "Keyboard", 79.99, "Mechanical keyboard"),
                createProduct("PROD-004", "Monitor", 299.99, "27-inch 4K monitor"),
                createProduct("PROD-005", "Headphones", 149.99, "Noise-cancelling headphones")
            );
            
            Map<String, Object> catalog = new HashMap<>();
            catalog.put("products", products);
            catalog.put("count", products.size());
            
            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(catalog);
                
        } catch (Exception e) {
            log.error("Failed to get product catalog", e);
            return "{\"error\": \"Failed to retrieve product catalog\"}";
        }
    }
    
    /**
     * Create a product entry
     */
    private Map<String, Object> createProduct(String id, String name, double price, String description) {
        Map<String, Object> product = new HashMap<>();
        product.put("id", id);
        product.put("name", name);
        product.put("price", price);
        product.put("description", description);
        return product;
    }
}
