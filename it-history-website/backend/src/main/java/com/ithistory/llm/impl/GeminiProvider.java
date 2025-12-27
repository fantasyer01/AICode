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
 * LLM provider implementation for Google Gemini Pro models.
 *
 * Uses Google's Gemini API which has a different request/response format
 * compared to OpenAI-compatible APIs.
 */
@Component
public class GeminiProvider implements LlmProvider {

    private static final Logger logger = LoggerFactory.getLogger(GeminiProvider.class);

    @Value("${llm.gemini.api-key:}")
    private String apiKey;

    @Value("${llm.gemini.model:gemini-pro}")
    private String model;

    @Value("${llm.gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models}")
    private String endpoint;

    @Value("${llm.gemini.max-tokens:2000}")
    private Integer maxTokens;

    @Value("${llm.gemini.temperature:0.7}")
    private Double temperature;

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private StoryPromptConfig storyPromptConfig;

    public GeminiProvider() {
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
            logger.warn("Google Gemini API key not configured, using mock response");
            return generateMockResponse(request);
        }

        try {
            String systemPrompt = storyPromptConfig.getSystemPrompt();
            String userMessage = storyPromptConfig.buildUserMessage(request);
            String requestBody = buildRequestBody(systemPrompt, userMessage);

            // Gemini uses API key in URL, not Authorization header
            String fullEndpoint = String.format("%s/%s:generateContent?key=%s", endpoint, model, apiKey);

            // Log request details (mask API key in URL)
            logger.info("[Gemini] Sending request for date: {}/{}", request.getMonth(), request.getDay());
            logger.debug("[Gemini] Request endpoint: {}", endpoint + "/" + model + ":generateContent");
            logger.debug("[Gemini] Request body: {}", requestBody);
            
            HttpPost httpPost = new HttpPost(fullEndpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

                // Log response details
                logger.info("[Gemini] Response status code: {}", statusCode);
                logger.debug("[Gemini] Response body: {}", responseBody);

                if (statusCode == 200) {
                    LlmResponse llmResponse = parseResponse(responseBody);
                    logger.info("[Gemini] Successfully generated story with {} sections", 
                            llmResponse.getSections() != null ? llmResponse.getSections().size() : 0);
                    return llmResponse;
                } else {
                    logger.error("[Gemini] API error - Status: {}, Body: {}", statusCode, responseBody);
                    throw new LlmException("Google Gemini API returned error: " + statusCode + " - " + responseBody);
                }
            }
        } catch (IOException e) {
            logger.error("[Gemini] Failed to call API", e);
            throw new LlmException("Failed to call Google Gemini API", e);
        }
    }

    private String buildRequestBody(String systemPrompt, String userMessage) throws IOException {
        Map<String, Object> requestMap = new HashMap<>();

        // Gemini uses "systemInstruction" for system-level instructions
        Map<String, Object> systemInstruction = new HashMap<>();
        List<Map<String, String>> systemParts = new ArrayList<>();
        Map<String, String> systemPart = new HashMap<>();
        systemPart.put("text", systemPrompt);
        systemParts.add(systemPart);
        systemInstruction.put("parts", systemParts);
        requestMap.put("systemInstruction", systemInstruction);

        // Gemini uses "contents" array with "parts" for user messages
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", userMessage);
        parts.add(part);
        
        content.put("parts", parts);
        contents.add(content);
        
        requestMap.put("contents", contents);

        // Generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", temperature);
        generationConfig.put("maxOutputTokens", maxTokens);
        generationConfig.put("responseMimeType", "application/json");
        requestMap.put("generationConfig", generationConfig);

        return objectMapper.writeValueAsString(requestMap);
    }

    private LlmResponse parseResponse(String responseBody) throws LlmException {
        String content = "";
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // Gemini response structure: candidates[0].content.parts[0].text
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    content = parts.get(0).path("text").asText();
                }
            }

            if (content.isEmpty()) {
                throw new LlmException("Empty content received from Gemini");
            }

            logger.debug("Gemini raw content: {}", content);

            // Try parsing as structured JSON
            try {
                return objectMapper.readValue(content, LlmResponse.class);
            } catch (Exception parseException) {
                logger.warn("Gemini returned JSON but structure doesn't match LlmResponse: {}", parseException.getMessage());
                logger.debug("Content that failed to parse: {}", content);
                return buildPlainTextResponse(content);
            }
        } catch (Exception e) {
            logger.error("Failed to extract content from Gemini response", e);
            throw new LlmException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }

    /**
     * Fallback: model returned plain text instead of structured JSON.
     * Wrap the text into a single-section LlmResponse so the frontend can render it.
     */
    private LlmResponse buildPlainTextResponse(String content) {
        LlmResponse response = new LlmResponse();
        response.setTitle("IT History Story");

        List<LlmResponse.Section> sections = new ArrayList<>();
        LlmResponse.Section section = new LlmResponse.Section();
        section.setHeading("Story");
        section.setContent(content);
        sections.add(section);

        response.setSections(sections);
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
        return "gemini";
    }
}
