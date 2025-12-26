package com.ithistory.llm;

public interface LlmProvider {
    
    /**
     * Generate IT history story for a specific date
     * 
     * @param request LLM request containing date information
     * @return LLM response with story content and image descriptions
     * @throws LlmException if generation fails
     */
    LlmResponse generateStory(LlmRequest request) throws LlmException;
    
    /**
     * Get provider name
     * 
     * @return provider identifier
     */
    String getProviderName();
}
