package com.ithistory.llm.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithistory.llm.LlmException;
import com.ithistory.llm.LlmProvider;
import com.ithistory.llm.LlmRequest;
import com.ithistory.llm.LlmResponse;
import com.ithistory.llm.StoryPromptConfig;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM provider implementation for DeepSeek models.
 *
 * This provider is wired into the generic LLM client via its provider name
 * ("deepseek"), and uses the same story prompt configuration as other
 * providers.
 */
@Component
public class DeepSeekProvider implements LlmProvider {

    private static final Logger logger = LoggerFactory.getLogger(DeepSeekProvider.class);

    @Value("${llm.deepseek.api-key:}")
    private String apiKey;

    @Value("${llm.deepseek.model:deepseek-chat}")
    private String model;

    @Value("${llm.deepseek.endpoint:https://api.deepseek.com/chat/completions}")
    private String endpoint;

    @Value("${llm.deepseek.max-tokens:2000}")
    private Integer maxTokens;

    @Value("${llm.deepseek.temperature:0.7}")
    private Double temperature;

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private StoryPromptConfig storyPromptConfig;

    public DeepSeekProvider() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10);
        connectionManager.setDefaultMaxPerRoute(5);

        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public LlmResponse generateStory(LlmRequest request) throws LlmException {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("DeepSeek API key not configured, using mock response");
            return generateMockResponse(request);
        }

        try {
            String prompt = storyPromptConfig.buildPrompt(request);
            String requestBody = buildRequestBody(prompt);

            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

                if (statusCode == 200) {
                    return parseResponse(responseBody);
                } else {
                    throw new LlmException("DeepSeek API returned error: " + statusCode + " - " + responseBody);
                }
            }
        } catch (IOException e) {
            throw new LlmException("Failed to call DeepSeek API", e);
        }
    }

    private String buildRequestBody(String prompt) throws IOException {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", model);
        requestMap.put("max_tokens", maxTokens);
        requestMap.put("temperature", temperature);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);

        requestMap.put("messages", messages);

        return objectMapper.writeValueAsString(requestMap);
    }

    private LlmResponse parseResponse(String responseBody) throws LlmException {
        String content = "";
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            content = root.path("choices").get(0).path("message").path("content").asText();
            return objectMapper.readValue(content, LlmResponse.class);
        } catch (Exception e) {
            logger.info("DeepSeek content is not valid JSON for LlmResponse, using plain-text fallback");
            return buildPlainTextResponse(content);
        }
    }

    /**
     * Fallback: model returned plain text instead of structured JSON.
     * Wrap the text into a single-section LlmResponse so the frontend can render it.
     */
    private LlmResponse buildPlainTextResponse(String content) {
        LlmResponse response = new LlmResponse();

        // Simple generic title - date/title is already handled separately in StoryService / Story entity
        response.setTitle("IT History Story");

        List<LlmResponse.Section> sections = new ArrayList<>();
        LlmResponse.Section section = new LlmResponse.Section();
        section.setHeading("Story");
        section.setContent(content);
        sections.add(section);

        response.setSections(sections);
        // imageDescriptions can stay null/empty; frontend already handles that
        return response;
    }

    private LlmResponse generateMockResponse(LlmRequest request) {
        LlmResponse response = new LlmResponse();

        String monthName = Month.of(request.getMonth()).name();
        response.setTitle(String.format("IT History: %s %d", monthName, request.getDay()));

        List<LlmResponse.Section> sections = new ArrayList<>();

        LlmResponse.Section intro = new LlmResponse.Section();
        intro.setHeading("Introduction");
        intro.setContent(String.format(
                "On %s %d, a significant event shaped the history of information technology. " +
                        "This story explores the innovation, the people behind it, and its lasting impact on our digital world.",
                monthName, request.getDay()
        ));
        sections.add(intro);

        LlmResponse.Section main = new LlmResponse.Section();
        main.setHeading("The Story");
        main.setContent(
                "<p>In the ever-evolving landscape of technology, certain moments stand out as pivotal turning points. " +
                        "This particular date marks one such moment when innovation met opportunity, creating ripples that would " +
                        "transform how we interact with computers and the digital realm.</p>" +
                        "<p>The individuals involved in this breakthrough combined technical expertise with visionary thinking, " +
                        "pushing the boundaries of what was thought possible. Their work laid the foundation for technologies " +
                        "we now take for granted in our daily lives.</p>" +
                        "<p>From hardware innovations to software breakthroughs, from networking protocols to user interface " +
                        "designs, this achievement represented a leap forward in computing capabilities and accessibility.</p>"
        );
        sections.add(main);

        LlmResponse.Section significance = new LlmResponse.Section();
        significance.setHeading("Historical Significance");
        significance.setContent(
                "<p>The impact of this milestone extends far beyond its immediate technical achievements. It influenced " +
                        "subsequent generations of developers, shaped industry standards, and opened new possibilities for " +
                        "technological advancement.</p>" +
                        "<p>Today, we continue to build upon the foundation established on this date, demonstrating the enduring " +
                        "relevance of this contribution to information technology.</p>"
        );
        sections.add(significance);

        response.setSections(sections);

        List<LlmResponse.ImageDescription> images = new ArrayList<>();

        LlmResponse.ImageDescription img1 = new LlmResponse.ImageDescription();
        img1.setDescription("Historical computing equipment from the era");
        img1.setCaption("Technology that defined an era");
        img1.setOrderIndex(0);
        images.add(img1);

        LlmResponse.ImageDescription img2 = new LlmResponse.ImageDescription();
        img2.setDescription("Portrait of key figures in IT history");
        img2.setCaption("Pioneers of digital innovation");
        img2.setOrderIndex(1);
        images.add(img2);

        response.setImageDescriptions(images);

        return response;
    }

    @Override
    public String getProviderName() {
        return "deepseek";
    }
}
