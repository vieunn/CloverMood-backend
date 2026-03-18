package com.example.api.profile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProfileService {

    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    public ProfileService(RestTemplate restTemplate, BCryptPasswordEncoder passwordEncoder) {
        this.restTemplate = restTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    // GET /profile?email=user@email.com
    public Map<String, Object> getProfile(String email) {
        Map<String, Object> res = new HashMap<>();

        if (email == null || email.isEmpty()) {
            res.put("success", false);
            res.put("message", "Email is required");
            return res;
        }

        try {
            String url = supabaseUrl + "/rest/v1/profiles?email=eq." + email + "&select=email,full_name,gender,profile_image";

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String body = response.getBody();
            
            // Parse JSON array response - if empty, user not found
            if (body == null || body.equals("[]")) {
                res.put("success", false);
                res.put("message", "Profile not found");
                return res;
            }

            // Extract profile data from JSON (simple parsing)
            ProfileResponse profileResponse = parseProfileFromJson(body);
            
            res.put("success", true);
            res.put("message", "Profile retrieved");
            res.put("data", profileResponse);
            return res;

        } catch (HttpStatusCodeException ex) {
            res.put("success", false);
            res.put("message", "Database error");
            res.put("status", ex.getStatusCode().value());
            return res;

        } catch (Exception ex) {
            res.put("success", false);
            res.put("message", "Server error");
            res.put("error", ex.getMessage());
            return res;
        }
    }

    // PUT /profile
    public Map<String, Object> updateProfile(UpdateProfileRequest request) {
        Map<String, Object> res = new HashMap<>();

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            res.put("success", false);
            res.put("message", "Email is required");
            return res;
        }

        try {
            String url = supabaseUrl + "/rest/v1/profiles?email=eq." + request.getEmail();
            System.out.println("DEBUG: updateProfile URL = " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Prefer", "return=minimal");

            Map<String, Object> body = new HashMap<>();
            if (request.getFullName() != null) {
                body.put("full_name", request.getFullName());
            }
            if (request.getGender() != null) {
                body.put("gender", request.getGender());
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);

            // Check if update was successful
            if (isProfileExists(request.getEmail())) {
                res.put("success", true);
                res.put("message", "Profile updated successfully");
                return res;
            } else {
                res.put("success", false);
                res.put("message", "Profile not found");
                return res;
            }

        } catch (HttpStatusCodeException ex) {
            res.put("success", false);
            res.put("message", "Database error");
            res.put("status", ex.getStatusCode().value());
            res.put("error", ex.getResponseBodyAsString());
            System.out.println("ProfileService updateProfile Error: " + ex.getResponseBodyAsString());
            return res;

        } catch (Exception ex) {
            res.put("success", false);
            res.put("message", "Server error");
            res.put("error", ex.getMessage());
            System.out.println("ProfileService updateProfile Exception: " + ex.getMessage());
            return res;
        }
    }

    // PUT /profile/password
    public Map<String, Object> changePassword(ChangePasswordRequest request) {
        Map<String, Object> res = new HashMap<>();

        if (request.getEmail() == null || request.getCurrentPassword() == null || request.getNewPassword() == null) {
            res.put("success", false);
            res.put("message", "Email, current password, and new password are required");
            return res;
        }

        try {
            // Step 1: Verify current password using Supabase Auth (single source of truth)
            String accessToken = verifyPasswordWithSupabaseAuth(request.getEmail(), request.getCurrentPassword());
            
            if (accessToken == null) {
                res.put("success", false);
                res.put("message", "Current password is incorrect");
                return res;
            }

            // Step 2: Update password in Supabase Auth using the access token
            boolean passwordUpdated = updatePasswordInSupabaseAuth(accessToken, request.getNewPassword());
            
            if (!passwordUpdated) {
                res.put("success", false);
                res.put("message", "Failed to update password in authentication system");
                return res;
            }

            // Step 3: Also update profiles.password for data consistency (secondary/optional)
            try {
                String hashedPassword = passwordEncoder.encode(request.getNewPassword());
                String profileUrl = supabaseUrl + "/rest/v1/profiles?email=eq." + request.getEmail();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("apikey", supabaseKey);
                headers.set("Authorization", "Bearer " + supabaseKey);
                headers.set("Prefer", "return=minimal");

                Map<String, Object> body = new HashMap<>();
                body.put("password", hashedPassword);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                restTemplate.exchange(profileUrl, HttpMethod.PATCH, entity, String.class);
            } catch (Exception ex) {
                // Log but don't fail - Supabase Auth update succeeded which is the important part
                System.out.println("ProfileService: Could not update local password copy: " + ex.getMessage());
            }

            res.put("success", true);
            res.put("message", "Password changed successfully");
            return res;

        } catch (HttpStatusCodeException ex) {
            res.put("success", false);
            res.put("message", "Authentication error");
            res.put("status", ex.getStatusCode().value());
            return res;

        } catch (Exception ex) {
            res.put("success", false);
            res.put("message", "Server error");
            res.put("error", ex.getMessage());
            return res;
        }
    }

    // POST /profile/photo
    public Map<String, Object> uploadProfilePhoto(String email, byte[] fileBytes, String fileName) {
        Map<String, Object> res = new HashMap<>();

        if (email == null || email.isEmpty() || fileBytes == null) {
            res.put("success", false);
            res.put("message", "Email and file are required");
            return res;
        }

        // Validate file type
        if (!isValidImageFile(fileName)) {
            res.put("success", false);
            res.put("message", "Only jpg, jpeg, and png files are allowed");
            return res;
        }

        try {
            // Convert bytes to base64 for storage
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);

            String url = supabaseUrl + "/rest/v1/profiles?email=eq." + email;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Prefer", "return=minimal");

            Map<String, Object> body = new HashMap<>();
            body.put("profile_image", base64Image);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);

            res.put("success", true);
            res.put("message", "Photo uploaded successfully");
            res.put("fileName", fileName);
            res.put("size", fileBytes.length);
            return res;

        } catch (HttpStatusCodeException ex) {
            res.put("success", false);
            res.put("message", "Database error");
            res.put("status", ex.getStatusCode().value());
            return res;

        } catch (Exception ex) {
            res.put("success", false);
            res.put("message", "Server error");
            res.put("error", ex.getMessage());
            return res;
        }
    }

    // Helper: Check if profile exists
    private boolean isProfileExists(String email) {
        try {
            String url = supabaseUrl + "/rest/v1/profiles?email=eq." + email + "&select=email";
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String body = response.getBody();
            return body != null && !body.equals("[]");

        } catch (Exception ex) {
            return false;
        }
    }

    // Helper: Verify password by authenticating against Supabase Auth (single source of truth)
    // Returns access token if password is correct, null if incorrect
    private String verifyPasswordWithSupabaseAuth(String email, String password) {
        try {
            String url = supabaseUrl + "/auth/v1/token?grant_type=password";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseKey);

            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // Login successful - extract access_token from response
            String responseBody = response.getBody();
            if (responseBody != null) {
                String accessToken = extractJsonField(responseBody, "access_token");
                return accessToken;
            }
            return null;

        } catch (HttpStatusCodeException ex) {
            // 400 or 401 means invalid credentials - return null
            if (ex.getStatusCode().value() == 400 || ex.getStatusCode().value() == 401) {
                return null;
            }
            throw ex;
        } catch (Exception ex) {
            return null;
        }
    }

    // Helper: Update password in Supabase Auth using access token
    private boolean updatePasswordInSupabaseAuth(String accessToken, String newPassword) {
        try {
            String url = supabaseUrl + "/auth/v1/user";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + accessToken);

            Map<String, Object> body = new HashMap<>();
            body.put("password", newPassword);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception ex) {
            System.out.println("ProfileService: Error updating password in Supabase Auth: " + ex.getMessage());
            return false;
        }
    }

    // Helper: Simple JSON parsing (you may replace with proper JSON library if needed)
    private ProfileResponse parseProfileFromJson(String jsonBody) {
        // Simple extraction from JSON array response like: [{"email":"..","full_name":"..","gender":"..","profile_image":".."}]
        String email = extractJsonField(jsonBody, "email");
        String fullName = extractJsonField(jsonBody, "full_name");
        String gender = extractJsonField(jsonBody, "gender");
        String profileImage = extractJsonField(jsonBody, "profile_image");
        
        boolean hasPhoto = profileImage != null && !profileImage.isEmpty() && !profileImage.equals("null");
        
        return new ProfileResponse(email, fullName, gender, hasPhoto);
    }

    private String extractJsonField(String json, String field) {
        String key = "\"" + field + "\":";
        int startIndex = json.indexOf(key);
        
        if (startIndex == -1) return null;
        
        startIndex = json.indexOf("\"", startIndex + key.length());
        if (startIndex == -1) return null;
        
        int endIndex = json.indexOf("\"", startIndex + 1);
        if (endIndex == -1) return null;
        
        return json.substring(startIndex + 1, endIndex);
    }

    private boolean isValidImageFile(String fileName) {
        if (fileName == null) return false;
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png");
    }
}
