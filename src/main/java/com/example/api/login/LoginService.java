package com.example.api.login;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LoginService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    public LoginService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> login(LoginRequest request) {
        Map<String, Object> res = new HashMap<>();

        if (request.getEmail() == null || request.getPassword() == null) {
            res.put("success", false);
            res.put("message", "Email and password required");
            return res;
        }

        String url = supabaseUrl + "/auth/v1/token?grant_type=password";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);

        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getPassword());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            // Parse Supabase response to extract token and user
            @SuppressWarnings("unchecked")
            Map<String, Object> supabaseResponse = objectMapper.readValue(response.getBody(), Map.class);
            
            String accessToken = (String) supabaseResponse.get("access_token");
            Object user = supabaseResponse.get("user");
            
            if (accessToken == null) {
                res.put("success", false);
                res.put("message", "No token received from Supabase");
                res.put("data", supabaseResponse);
                return res;
            }
            
            // Extract user ID from user object
            String userId = null;
            if (user instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userMap = (Map<String, Object>) user;
                userId = (String) userMap.get("id");
            }

            res.put("success", true);
            res.put("message", "Login successful");
            res.put("token", accessToken);        // ← Frontend expects this
            res.put("access_token", accessToken); // ← Alternative name
            res.put("userId", userId);            // ← User ID from Supabase
            res.put("user", user);                // ← Full user object
            return res;

        } catch (HttpStatusCodeException ex) {
            res.put("success", false);
            res.put("message", "Invalid login credentials");
            res.put("status", ex.getStatusCode().value());
            res.put("error", ex.getResponseBodyAsString());
            return res;

        } catch (Exception ex) {
            res.put("success", false);
            res.put("message", "Server error");
            res.put("error", ex.getMessage());
            return res;
        }
    }
}