package com.example.aialibaba.service.impl;

import com.example.aialibaba.model.dto.wecom.response.WeComStreamResponse;
import com.example.aialibaba.model.dto.wecom.response.WeComTextResponse;
import com.example.aialibaba.service.WeComResponseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of WeComResponseService using OkHttp for async responses.
 */
@Service
public class WeComResponseServiceImpl implements WeComResponseService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeComResponseServiceImpl.class);
    
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public WeComResponseServiceImpl() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public boolean sendTextResponse(String responseUrl, WeComTextResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            return sendRawResponse(responseUrl, json);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize text response", e);
            return false;
        }
    }
    
    @Override
    public boolean sendStreamResponse(String responseUrl, WeComStreamResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            return sendRawResponse(responseUrl, json);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize stream response", e);
            return false;
        }
    }
    
    @Override
    public boolean sendRawResponse(String responseUrl, String jsonResponse) {
        if (responseUrl == null || responseUrl.isEmpty()) {
            logger.warn("Response URL is empty, cannot send async response");
            return false;
        }
        
        logger.debug("Sending async response to URL: {}", responseUrl);
        
        RequestBody body = RequestBody.create(jsonResponse, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(responseUrl)
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                logger.debug("Successfully sent async response");
                return true;
            } else {
                String responseBody = response.body() != null ? response.body().string() : "No body";
                logger.error("Failed to send async response, status: {}, body: {}", 
                        response.code(), responseBody);
                return false;
            }
        } catch (IOException e) {
            logger.error("Error sending async response to {}", responseUrl, e);
            return false;
        }
    }
}
