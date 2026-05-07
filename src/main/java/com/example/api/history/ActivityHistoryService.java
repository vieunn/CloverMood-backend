package com.example.api.history;

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
public class ActivityHistoryService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    public ActivityHistoryService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // POST /activity_history - Save activity history
    public ActivityHistory saveActivityHistory(ActivityHistoryRequest request) {
        try {
            String url = supabaseUrl + "/rest/v1/activity_history";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Content-Type", "application/json");
            headers.set("Prefer", "return=representation");

            // Create activity history object for request body
            Map<String, Object> body = new HashMap<>();
            body.put("user_id", request.getUserId());
            body.put("activity_type", request.getActivityType());
            body.put("mood_value", request.getMoodValue());
            body.put("note", request.getNote());
            body.put("description", request.getDescription());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Parse response to get the created activity history
            String responseBody = response.getBody();
            if (responseBody != null && !responseBody.equals("[]")) {
                // Parse the array response - Supabase returns an array with the created record
                List<Map<String, Object>> result = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {});
                if (!result.isEmpty()) {
                    return parseJsonToActivityHistory(objectMapper.writeValueAsString(result.get(0)));
                }
            }

            // Fallback: return the activity history we sent
            return new ActivityHistory(request.getUserId(), request.getActivityType(), 
                                      request.getMoodValue(), request.getNote(), request.getDescription());

        } catch (HttpStatusCodeException ex) {
            System.err.println("ERROR: HTTP error saving activity history");
            System.err.println("Status: " + ex.getStatusCode().value());
            System.err.println("Response: " + ex.getResponseBodyAsString());
            throw new RuntimeException("Failed to save activity history: " + ex.getMessage());

        } catch (Exception ex) {
            System.err.println("ERROR: Exception saving activity history");
            System.err.println("Exception: " + ex.getMessage());
            throw new RuntimeException("Server error saving activity history: " + ex.getMessage());
        }
    }

    // GET /activity_history?user_id=<userId> - Get activity history by user
    public List<ActivityHistory> getActivityHistoryByUser(String userId) {
        try {
            String url = supabaseUrl + "/rest/v1/activity_history?user_id=eq." + userId + "&order=timestamp.desc";
            
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
            List<ActivityHistory> histories = new ArrayList<>();
            for (Map<String, Object> record : result) {
                histories.add(parseJsonToActivityHistory(objectMapper.writeValueAsString(record)));
            }
            return histories;

        } catch (Exception ex) {
            System.err.println("ERROR: Exception fetching activity history");
            System.err.println("Exception: " + ex.getMessage());
            throw new RuntimeException("Server error fetching activity history: " + ex.getMessage());
        }
    }

    // GET /activity_history?user_id=<userId>&activity_type=<type> - Get activity history by type
    public List<ActivityHistory> getActivityHistoryByUserAndType(String userId, String activityType) {
        try {
            String url = supabaseUrl + "/rest/v1/activity_history?user_id=eq." + userId 
                         + "&activity_type=eq." + activityType + "&order=timestamp.desc";
            
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
            List<ActivityHistory> histories = new ArrayList<>();
            for (Map<String, Object> record : result) {
                histories.add(parseJsonToActivityHistory(objectMapper.writeValueAsString(record)));
            }
            return histories;

        } catch (Exception ex) {
            System.err.println("ERROR: Exception fetching activity history by type");
            System.err.println("Exception: " + ex.getMessage());
            throw new RuntimeException("Server error fetching activity history: " + ex.getMessage());
        }
    }

    // Helper method to parse JSON string to ActivityHistory object
    private ActivityHistory parseJsonToActivityHistory(String json) throws Exception {
        ActivityHistory history = objectMapper.readValue(json, ActivityHistory.class);
        return history;
    }
}
