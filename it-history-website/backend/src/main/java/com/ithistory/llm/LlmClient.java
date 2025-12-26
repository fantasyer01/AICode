package com.ithistory.llm;

public interface LlmClient {
    /**
     * Generate IT history story for a specific date using the configured provider.
     *
     * @param request LLM request containing date information
     * @return LLM response with story content and image descriptions
     * @throws LlmException if generation fails
     */
    LlmResponse generateStory(LlmRequest request) throws LlmException;
}
