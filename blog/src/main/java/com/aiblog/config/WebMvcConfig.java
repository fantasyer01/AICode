package com.aiblog.config;

import com.aiblog.interceptor.AdminAuthInterceptor;
import com.aiblog.interceptor.ApiKeyInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.image.storage-dir:./data/images}")
    private String storageDir;

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final ApiKeyInterceptor apiKeyInterceptor;

    public WebMvcConfig(AdminAuthInterceptor adminAuthInterceptor, ApiKeyInterceptor apiKeyInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(storageDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/images/**")
                .addResourceLocations(absolutePath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");

        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/articles/**", "/api/snippets/**");
    }
}
