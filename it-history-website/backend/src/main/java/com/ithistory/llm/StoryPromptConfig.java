package com.ithistory.llm;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Month;

/**
 * Central configuration for story-generation prompts.
 *
 * All LLM providers should use this component to build the final prompt text
 * so that prompt wording can be managed from configuration instead of code.
 * 
 * The prompt template is used as system message, and the user message contains
 * the specific date selected by the user.
 */
@Component
public class StoryPromptConfig {

    /**
     * System prompt template that defines the AI role and requirements.
     * Loaded from external txt file for better maintainability.
     */
    private String systemPromptTemplate;

    /**
     * Load the system prompt from external txt file on initialization.
     */
    @PostConstruct
    public void init() throws IOException {
        ClassPathResource resource = new ClassPathResource("prompts/story-system-prompt.md");
        this.systemPromptTemplate = new String(
            resource.getInputStream().readAllBytes(), 
            StandardCharsets.UTF_8
        );
    }

    /**
     * Get the system prompt (AI role and instructions).
     */
    public String getSystemPrompt() {
        return systemPromptTemplate;
    }

    /**
     * Build user message with the selected date.
     * For example: "请讲述历史上12月25日发生的IT重大事件"
     */
    public String buildUserMessage(LlmRequest request) {
        String monthName = Month.of(request.getMonth()).name();
        return String.format("请讲述历史上%s月%d日（%s %d）发生的IT重大事件，并按照要求的格式输出。", 
                request.getMonth(), request.getDay(), monthName, request.getDay());
    }
}
