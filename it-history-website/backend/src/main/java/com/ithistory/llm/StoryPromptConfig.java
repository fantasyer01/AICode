package com.ithistory.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.time.Month;

/**
 * Central configuration for story-generation prompts.
 *
 * All LLM providers should use this component to build the final prompt text
 * so that prompt wording can be managed from configuration instead of code.
 */
@Component
@PropertySource("classpath:story-prompt.properties")
public class StoryPromptConfig {

    /**
     * Prompt template with placeholders for month name and day.
     * For example: "... occurred on %s %d ..." where %s is month name and %d is day.
     */
    @Value("${llm.story.prompt.template}")
    private String promptTemplate;

    public String buildPrompt(LlmRequest request) {
        String monthName = Month.of(request.getMonth()).name();
        return String.format(promptTemplate, monthName, request.getDay());
    }
}
