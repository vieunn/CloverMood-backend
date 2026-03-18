package com.example.api.register;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class RegisterService {

    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    public RegisterService(RestTemplate restTemplate, BCryptPasswordEncoder passwordEncoder) {
        this.restTemplate = restTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<Map<String, Object>> register(RegisterRequest request) {
        Map<String, Object> res = new HashMap<>();

        if (request.getEmail() == null || request.getPassword() == null) {
            res.put("success", false);
            res.put("message", "Email and password required");
            return ResponseEntity.badRequest().body(res);
        }

        String url = supabaseUrl + "/auth/v1/signup";

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

            // Create profile entry for the new user
            createEmptyProfile(request.getEmail(), request.getPassword());

            res.put("success", true);
            res.put("message", "User registered via Supabase");
            res.put("data", response.getBody());
            return ResponseEntity.ok(res);

        } catch (HttpStatusCodeException ex) {
            // This is the IMPORTANT part: show Supabase’s real error
            res.put("success", false);
            res.put("message", "Supabase error");
            res.put("status", ex.getStatusCode().value());
            res.put("error", ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode()).body(res);

        } catch (Exception ex) {
            res.put("success", false);
            res.put("message", "Server error");
            res.put("error", ex.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    private void createEmptyProfile(String email, String password) {
        try {
            String profileUrl = supabaseUrl + "/rest/v1/profiles";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);

            Map<String, Object> profileBody = new HashMap<>();
            profileBody.put("email", email);
            profileBody.put("full_name", "");
            profileBody.put("gender", "");
            profileBody.put("password", passwordEncoder.encode(password));
            profileBody.put("profile_image", null);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(profileBody, headers);
            restTemplate.postForEntity(profileUrl, entity, String.class);

        } catch (Exception ex) {
            // Silently fail - profile creation is not critical
            System.out.println("Warning: Could not create profile entry: " + ex.getMessage());
        }
    }
}