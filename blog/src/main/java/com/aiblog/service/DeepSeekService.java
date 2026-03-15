package com.aiblog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekService.class);

    private final RestClient restClient;
    private final String model;
    private final ObjectMapper objectMapper;

    public record ProcessingResult(String title, String content, List<String> tags) {}

    private static final String SYSTEM_PROMPT = """
            You are a reading notes organizer. The user will provide raw text they captured during reading.
            Your job is to:
            1. Generate a concise title (max 50 characters) in Chinese that summarizes the core idea.
            2. Restructure and organize the raw text into clean, well-formatted markdown content in Chinese. Fix grammar, improve clarity, and add structure (headings, lists, etc.) where appropriate. Preserve the original meaning but condense the content - aim for about 30-50% of the original length by extracting key points and removing redundancy.
            3. Generate 1-2 relevant topic tags in English (lowercase, hyphenated, e.g. "machine-learning", "system-design").

            IMPORTANT: Your response MUST be in Chinese (both title and content), regardless of the input language.

            You MUST respond with a valid JSON object and nothing else. The JSON must have exactly these fields:
            {
              "title": "concise Chinese title here",
              "content": "structured Chinese markdown content here, condensed and concise",
              "tags": ["tag1", "tag2"]
            }
            """;

    public DeepSeekService(@Value("${app.deepseek.api-key}") String apiKey,
                           @Value("${app.deepseek.base-url}") String baseUrl,
                           @Value("${app.deepseek.model}") String model,
                           ObjectMapper objectMapper) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public ProcessingResult processSnippet(String rawContent) {
        log.info("Calling DeepSeek API to process snippet ({} chars)", rawContent.length());

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", rawContent)
                ),
                "response_format", Map.of("type", "json_object")
        );

        String responseBody = restClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode root = parseJson(responseBody);
        String messageContent = root.path("choices").path(0).path("message").path("content").asText();
        JsonNode result = parseJson(messageContent);

        String title = result.path("title").asText("");
        String content = result.path("content").asText("");
        List<String> tags = new ArrayList<>();
        JsonNode tagsNode = result.path("tags");
        if (tagsNode.isArray()) {
            for (JsonNode tag : tagsNode) {
                tags.add(tag.asText());
            }
        }

        if (title.length() > 50) {
            title = title.substring(0, 50);
        }
        if (tags.size() > 2) {
            tags = tags.subList(0, 2);
        }

        log.info("DeepSeek processing complete: title='{}', tags={}", title, tags);
        return new ProcessingResult(title, content, tags);
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DeepSeek API response as JSON", e);
        }
    }
}
