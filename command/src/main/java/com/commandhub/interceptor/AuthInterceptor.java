package com.commandhub.interceptor;

import com.commandhub.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // token -> expiry timestamp
    private final ConcurrentHashMap<String, Long> tokenStore = new ConcurrentHashMap<>();

    public AuthInterceptor(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow GET requests without auth (public read access)
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null && isValidToken(token)) {
            return true;
        }

        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("code", 403, "message", "未授权，请先登录")
        ));
        return false;
    }

    public String generateToken() {
        try {
            String raw = appProperties.getAdminPassword() + Instant.now().toEpochMilli();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            String token = sb.toString();
            long expiry = Instant.now().plusSeconds((long) appProperties.getTokenExpireHours() * 3600).toEpochMilli();
            tokenStore.put(token, expiry);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    public boolean isValidToken(String token) {
        Long expiry = tokenStore.get(token);
        if (expiry == null) return false;
        if (Instant.now().toEpochMilli() > expiry) {
            tokenStore.remove(token);
            return false;
        }
        return true;
    }
}
