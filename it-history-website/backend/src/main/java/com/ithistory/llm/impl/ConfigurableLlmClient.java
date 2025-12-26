package com.ithistory.llm.impl;

import com.ithistory.llm.LlmClient;
import com.ithistory.llm.LlmException;
import com.ithistory.llm.LlmProvider;
import com.ithistory.llm.LlmRequest;
import com.ithistory.llm.LlmResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Facade for selecting an underlying LLM provider based on configuration.
 *
 * This uses a strategy pattern: multiple {@link LlmProvider} implementations
 * can be registered, and the active one is chosen via the llm.provider property.
 */
@Component
public class ConfigurableLlmClient implements LlmClient {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurableLlmClient.class);

    private final Map<String, LlmProvider> providersByName = new HashMap<>();
    private final String activeProviderKey;

    public ConfigurableLlmClient(
            List<LlmProvider> providers,
            @Value("${llm.provider:openai}") String providerName
    ) {
        this.activeProviderKey = normalizeName(providerName);

        for (LlmProvider provider : providers) {
            String key = normalizeName(provider.getProviderName());
            if (providersByName.containsKey(key)) {
                logger.warn("Duplicate LLM provider name detected: {}. Existing provider will be kept.", key);
                continue;
            }
            providersByName.put(key, provider);
        }

        if (!providersByName.containsKey(this.activeProviderKey)) {
            logger.warn("Configured LLM provider '{}' not found. Available providers: {}",
                    this.activeProviderKey, providersByName.keySet());
        } else {
            logger.info("Active LLM provider set to '{}'.", this.activeProviderKey);
        }
    }

    @Override
    public LlmResponse generateStory(LlmRequest request) throws LlmException {
        LlmProvider provider = providersByName.get(activeProviderKey);
        if (provider == null) {
            throw new LlmException("No LLM provider available for configured name: " + activeProviderKey);
        }

        logger.info("Using LLM provider: {}", provider.getProviderName());
        return provider.generateStory(request);
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}
