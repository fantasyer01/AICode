package com.example.mcp.server.tools;

import com.example.mcp.server.capabilities.ToolRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Shopping cart tool implementations.
 * 
 * Provides tools for managing a shopping cart.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShoppingCartTools {
    
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;
    
    // In-memory cart storage
    private final List<CartItem> cart = Collections.synchronizedList(new ArrayList<>());
    
    @PostConstruct
    public void registerTools() {
        registerAddToCartTool();
        registerRemoveFromCartTool();
        registerCalculateTotalTool();
        registerClearCartTool();
        registerGetCartTool();
    }
    
    /**
     * Register the addToCart tool
     */
    private void registerAddToCartTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> productIdProp = new HashMap<>();
        productIdProp.put("type", "string");
        productIdProp.put("description", "Product identifier");
        properties.put("productId", productIdProp);
        
        Map<String, Object> quantityProp = new HashMap<>();
        quantityProp.put("type", "integer");
        quantityProp.put("description", "Quantity to add");
        quantityProp.put("minimum", 1);
        properties.put("quantity", quantityProp);
        
        Map<String, Object> priceProp = new HashMap<>();
        priceProp.put("type", "number");
        priceProp.put("description", "Price per unit");
        priceProp.put("minimum", 0);
        properties.put("price", priceProp);
        
        schema.put("properties", properties);
        schema.put("required", List.of("productId", "quantity", "price"));
        
        toolRegistry.registerTool(
            "addToCart",
            "Add an item to the shopping cart",
            schema,
            this::addToCart
        );
    }
    
    /**
     * Register the removeFromCart tool
     */
    private void registerRemoveFromCartTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> productIdProp = new HashMap<>();
        productIdProp.put("type", "string");
        productIdProp.put("description", "Product identifier to remove");
        properties.put("productId", productIdProp);
        
        schema.put("properties", properties);
        schema.put("required", List.of("productId"));
        
        toolRegistry.registerTool(
            "removeFromCart",
            "Remove an item from the shopping cart",
            schema,
            this::removeFromCart
        );
    }
    
    /**
     * Register the calculateTotal tool
     */
    private void registerCalculateTotalTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", new HashMap<>());
        
        toolRegistry.registerTool(
            "calculateTotal",
            "Calculate the total price of items in the cart",
            schema,
            this::calculateTotal
        );
    }
    
    /**
     * Register the clearCart tool
     */
    private void registerClearCartTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", new HashMap<>());
        
        toolRegistry.registerTool(
            "clearCart",
            "Remove all items from the cart",
            schema,
            this::clearCart
        );
    }
    
    /**
     * Register the getCart tool
     */
    private void registerGetCartTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", new HashMap<>());
        
        toolRegistry.registerTool(
            "getCart",
            "Get all items currently in the cart",
            schema,
            this::getCart
        );
    }
    
    /**
     * Add item to cart
     */
    private Object addToCart(Map<String, Object> args) {
        String productId = (String) args.get("productId");
        Integer quantity = getInteger(args.get("quantity"));
        Double price = getDouble(args.get("price"));
        
        if (productId == null || quantity == null || price == null) {
            throw new IllegalArgumentException("Missing required parameters");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        if (price < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        
        CartItem item = new CartItem(productId, quantity, price);
        cart.add(item);
        
        log.info("Added to cart: {} x{} @ ${}", productId, quantity, price);
        
        return createTextContent(String.format(
            "Successfully added %s (quantity: %d, price: $%.2f) to cart. Cart now has %d items.",
            productId, quantity, price, cart.size()
        ));
    }
    
    /**
     * Remove item from cart
     */
    private Object removeFromCart(Map<String, Object> args) {
        String productId = (String) args.get("productId");
        
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        
        boolean removed = cart.removeIf(item -> item.productId.equals(productId));
        
        if (removed) {
            log.info("Removed from cart: {}", productId);
            return createTextContent(String.format(
                "Successfully removed %s from cart. Cart now has %d items.",
                productId, cart.size()
            ));
        } else {
            return createTextContent(String.format(
                "Product %s not found in cart.",
                productId
            ));
        }
    }
    
    /**
     * Calculate cart total
     */
    private Object calculateTotal(Map<String, Object> args) {
        double total = 0.0;
        Map<String, Integer> itemCounts = new HashMap<>();
        
        for (CartItem item : cart) {
            total += item.quantity * item.price;
            itemCounts.merge(item.productId, item.quantity, Integer::sum);
        }
        
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Cart Summary:\n");
        breakdown.append("================\n");
        
        if (cart.isEmpty()) {
            breakdown.append("Cart is empty\n");
        } else {
            for (CartItem item : cart) {
                breakdown.append(String.format("- %s: %d x $%.2f = $%.2f\n",
                    item.productId, item.quantity, item.price, item.quantity * item.price));
            }
            breakdown.append("================\n");
            breakdown.append(String.format("Total: $%.2f\n", total));
            breakdown.append(String.format("Total items: %d", cart.size()));
        }
        
        log.info("Calculated cart total: ${}", total);
        return createTextContent(breakdown.toString());
    }
    
    /**
     * Clear the cart
     */
    private Object clearCart(Map<String, Object> args) {
        int previousSize = cart.size();
        cart.clear();
        
        log.info("Cart cleared, removed {} items", previousSize);
        return createTextContent(String.format(
            "Cart cleared. Removed %d items.",
            previousSize
        ));
    }
    
    /**
     * Get cart contents
     */
    private Object getCart(Map<String, Object> args) {
        if (cart.isEmpty()) {
            return createTextContent("Cart is empty");
        }
        
        try {
            String cartJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(cart);
            return createTextContent("Current cart:\n" + cartJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cart", e);
            return createTextContent("Error retrieving cart contents");
        }
    }
    
    /**
     * Create text content response
     */
    private List<Map<String, Object>> createTextContent(String text) {
        Map<String, Object> content = new HashMap<>();
        content.put("type", "text");
        content.put("text", text);
        return List.of(content);
    }
    
    /**
     * Get integer from object (handles both Integer and Number types)
     */
    private Integer getInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }
    
    /**
     * Get double from object (handles both Double and Number types)
     */
    private Double getDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return null;
    }
    
    /**
     * Get current cart (for resource access)
     */
    public List<CartItem> getCurrentCart() {
        return new ArrayList<>(cart);
    }
    
    /**
     * Cart item class
     */
    public static class CartItem {
        public String productId;
        public int quantity;
        public double price;
        
        public CartItem(String productId, int quantity, double price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
