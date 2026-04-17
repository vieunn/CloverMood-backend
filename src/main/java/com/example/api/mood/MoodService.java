package com.example.api.mood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MoodService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    public MoodService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // POST /moods - Save mood
    public Mood saveMood(MoodRequest request) {
        try {
            String url = supabaseUrl + "/rest/v1/moods";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Content-Type", "application/json");
            headers.set("Prefer", "return=representation");

            // Create mood object for request body
            Map<String, Object> body = new HashMap<>();
            body.put("user_id", request.getUserId());
            body.put("mood_value", request.getMoodValue());
            body.put("note", request.getNote());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Parse response to get the created mood
            String responseBody = response.getBody();
            if (responseBody != null && !responseBody.equals("[]")) {
                // Parse the array response - Supabase returns an array with the created record
                List<Map<String, Object>> result = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {});
                if (!result.isEmpty()) {
                    return parseJsonToMood(objectMapper.writeValueAsString(result.get(0)));
                }
            }

            // Fallback: return the mood we sent
            return new Mood(request.getUserId(), request.getMoodValue(), request.getNote());

        } catch (HttpStatusCodeException ex) {
            System.err.println("ERROR: HTTP error saving mood");
            System.err.println("Status: " + ex.getStatusCode().value());
            System.err.println("Response: " + ex.getResponseBodyAsString());
            throw new RuntimeException("Failed to save mood: " + ex.getMessage());

        } catch (Exception ex) {
            System.err.println("ERROR: Exception saving mood");
            System.err.println("Exception: " + ex.getMessage());
            throw new RuntimeException("Server error saving mood: " + ex.getMessage());
        }
    }

    // GET /moods?user_id=<userId> - Get moods by user
    public List<Mood> getMoodsByUser(Long userId) {
        try {
            String url = supabaseUrl + "/rest/v1/moods?user_id=eq." + userId + "&order=created_at.desc";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String body = response.getBody();
            if (body == null || body.equals("[]")) {
                return new ArrayList<>();
            }

            // Parse JSON array response
            List<Map<String, Object>> result = objectMapper.readValue(body, new TypeReference<List<Map<String, Object>>>() {});
            List<Mood> moods = new ArrayList<>();
            for (Map<String, Object> item : result) {
                moods.add(parseJsonToMood(objectMapper.writeValueAsString(item)));
            }
            return moods;

        } catch (HttpStatusCodeException ex) {
            System.err.println("ERROR: HTTP error fetching moods");
            System.err.println("Status: " + ex.getStatusCode().value());
            System.err.println("Response: " + ex.getResponseBodyAsString());
            return new ArrayList<>();

        } catch (Exception ex) {
            System.err.println("ERROR: Exception fetching moods");
            System.err.println("Exception: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    private Mood parseJsonToMood(String json) {
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            Mood mood = new Mood();
            mood.setId(Long.parseLong(map.get("id").toString()));
            
            // Handle userId - keep as string to support both numeric IDs and UUIDs
            Object userIdObj = map.get("user_id");
            if (userIdObj != null) {
                mood.setUserId(userIdObj.toString());
            }
            
            mood.setMoodValue((String) map.get("mood_value"));
            mood.setNote((String) map.get("note"));
            return mood;
        } catch (Exception ex) {
            System.err.println("ERROR: Failed to parse mood JSON: " + ex.getMessage());
            ex.printStackTrace();
            return new Mood();
        }
    }
}