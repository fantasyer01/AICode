package com.commandhub.controller;

import com.commandhub.config.AppProperties;
import com.commandhub.interceptor.AuthInterceptor;
import com.commandhub.model.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppProperties appProperties;
    private final AuthInterceptor authInterceptor;

    public AuthController(AppProperties appProperties, AuthInterceptor authInterceptor) {
        this.appProperties = appProperties;
        this.authInterceptor = authInterceptor;
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password != null && password.equals(appProperties.getAdminPassword())) {
            String token = authInterceptor.generateToken();
            return ApiResponse.ok(Map.of("token", token));
        }
        return ApiResponse.error(401, "密码错误");
    }

    @GetMapping("/status")
    public ApiResponse<?> status(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (authInterceptor.isValidToken(token)) {
                return ApiResponse.ok(Map.of("authenticated", true));
            }
        }
        return ApiResponse.ok(Map.of("authenticated", false));
    }
}
