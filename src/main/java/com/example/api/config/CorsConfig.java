package com.example.api.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:https://clovermood-x34a.onrender.com,http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Trim whitespace from each origin to avoid URL matching issues
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toArray(String[]::new);
        
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type", "X-Total-Count")
                .allowCredentials(true)
                .maxAge(3600);
    }
}