package com.example.mcp.server.prompts;

import com.example.mcp.server.capabilities.PromptManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Prompt templates for shopping assistance.
 * 
 * Provides pre-defined prompt templates for AI interactions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PurchasePrompts {
    
    private final PromptManager promptManager;
    
    @PostConstruct
    public void registerPrompts() {
        registerAssistPurchasePrompt();
        registerRecommendProductsPrompt();
    }
    
    /**
     * Register the assist purchase prompt
     */
    private void registerAssistPurchasePrompt() {
        promptManager.registerPrompt(
            "assistPurchase",
            "Guide a customer through the purchase process",
            List.of("customerName"),
            this::generateAssistPurchasePrompt
        );
    }
    
    /**
     * Register the recommend products prompt
     */
    private void registerRecommendProductsPrompt() {
        promptManager.registerPrompt(
            "recommendProducts",
            "Recommend products based on customer needs",
            List.of("customerNeeds", "budget"),
            this::generateRecommendProductsPrompt
        );
    }
    
    /**
     * Generate assist purchase prompt
     */
    private Object generateAssistPurchasePrompt(Map<String, Object> args) {
        String customerName = (String) args.getOrDefault("customerName", "Customer");
        
        List<Map<String, Object>> messages = new ArrayList<>();
        
        messages.add(createMessage(
            "user",
            String.format("Hello! I'm %s and I need help with shopping.", customerName)
        ));
        
        messages.add(createMessage(
            "assistant",
            String.format(
                "Hello %s! I'd be happy to help you with your shopping today. " +
                "I can assist you with:\n\n" +
                "1. Adding items to your cart\n" +
                "2. Viewing the product catalog\n" +
                "3. Calculating your cart total\n" +
                "4. Managing your cart items\n\n" +
                "What would you like to do?",
                customerName
            )
        ));
        
        return messages;
    }
    
    /**
     * Generate recommend products prompt
     */
    private Object generateRecommendProductsPrompt(Map<String, Object> args) {
        String needs = (String) args.getOrDefault("customerNeeds", "general computing");
        String budget = (String) args.getOrDefault("budget", "not specified");
        
        List<Map<String, Object>> messages = new ArrayList<>();
        
        messages.add(createMessage(
            "user",
            String.format(
                "I'm looking for products for %s. My budget is %s.",
                needs, budget
            )
        ));
        
        messages.add(createMessage(
            "assistant",
            String.format(
                "I understand you're looking for products related to %s " +
                "with a budget of %s. Let me check our catalog and recommend " +
                "the best options for you. I'll consider:\n\n" +
                "- Quality and value\n" +
                "- Customer ratings\n" +
                "- Your specific needs\n" +
                "- Your budget constraints\n\n" +
                "Let me retrieve our product catalog to make personalized recommendations.",
                needs, budget
            )
        ));
        
        return messages;
    }
    
    /**
     * Create a message object
     */
    private Map<String, Object> createMessage(String role, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", role);
        
        Map<String, Object> contentObj = new HashMap<>();
        contentObj.put("type", "text");
        contentObj.put("text", content);
        
        message.put("content", contentObj);
        return message;
    }
}
