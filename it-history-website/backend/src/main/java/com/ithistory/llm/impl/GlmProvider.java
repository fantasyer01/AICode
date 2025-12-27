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
 * LLM provider implementation for Zhipu GLM models.
 *
 * Zhipu GLM API is compatible with OpenAI chat completions format.
 * See: https://open.bigmodel.cn/dev/api
 */
@Component
public class GlmProvider implements LlmProvider {

    private static final Logger logger = LoggerFactory.getLogger(GlmProvider.class);

    @Value("${llm.glm.api-key:}")
    private String apiKey;

    @Value("${llm.glm.model:glm-4}")
    private String model;

    @Value("${llm.glm.endpoint:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String endpoint;

    @Value("${llm.glm.max-tokens:8000}")
    private Integer maxTokens;

    @Value("${llm.glm.temperature:0.7}")
    private Double temperature;

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private StoryPromptConfig storyPromptConfig;

    public GlmProvider() {
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
            logger.warn("GLM API key not configured, using mock response");
            return generateMockResponse(request);
        }

        try {
            String systemPrompt = storyPromptConfig.getSystemPrompt();
            String userMessage = storyPromptConfig.buildUserMessage(request);
            String requestBody = buildRequestBody(systemPrompt, userMessage);

            // Log request details
            logger.info("[GLM] Sending request for date: {}/{}", request.getMonth(), request.getDay());
            logger.debug("[GLM] Request endpoint: {}", endpoint);
            logger.debug("[GLM] Request body: {}", requestBody);

            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

                // Log response details
                logger.info("[GLM] Response status code: {}", statusCode);
                logger.debug("[GLM] Response body: {}", responseBody);

                if (statusCode == 200) {
                    LlmResponse llmResponse = parseResponse(responseBody);
                    logger.info("[GLM] Successfully generated story with {} sections", 
                            llmResponse.getSections() != null ? llmResponse.getSections().size() : 0);
                    return llmResponse;
                } else {
                    logger.error("[GLM] API error - Status: {}, Body: {}", statusCode, responseBody);
                    throw new LlmException("GLM API returned error: " + statusCode + " - " + responseBody);
                }
            }
        } catch (IOException e) {
            logger.error("[GLM] Failed to call API", e);
            throw new LlmException("Failed to call GLM API", e);
        }
    }

    private String buildRequestBody(String systemPrompt, String userMessage) throws IOException {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", model);
        requestMap.put("max_tokens", maxTokens);
        requestMap.put("temperature", temperature);

        // GLM supports response_format for JSON mode
        Map<String, String> responseFormat = new HashMap<>();
        responseFormat.put("type", "json_object");
        requestMap.put("response_format", responseFormat);

        List<Map<String, String>> messages = new ArrayList<>();
        
        // System message with role and instructions
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        // User message with the specific date
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        requestMap.put("messages", messages);

        return objectMapper.writeValueAsString(requestMap);
    }

    private LlmResponse parseResponse(String responseBody) throws LlmException {
        String content = "";
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            content = root.path("choices").get(0).path("message").path("content").asText();
            
            logger.debug("GLM raw content: {}", content);
            
            // Try parsing as structured JSON
            try {
                return objectMapper.readValue(content, LlmResponse.class);
            } catch (Exception parseException) {
                logger.debug("Content that failed to parse: {}", content);
                logger.warn("GLM returned JSON but structure doesn't match LlmResponse: {}", parseException.getMessage());
                return buildPlainTextResponse(content);
            }
        } catch (Exception e) {
            logger.error("Failed to extract content from GLM response", e);
            throw new LlmException("Failed to parse GLM response: " + e.getMessage(), e);
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
        return "glm";
    }
}
