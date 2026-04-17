package com.example.api.util;

import java.util.Base64;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AuthUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extract user ID from Supabase JWT token
     * The token structure is: Bearer {token}
     */
    public String extractUserIdFromToken(String authHeader) {
        System.out.println("DEBUG: authHeader = " + authHeader);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("DEBUG: Missing or invalid Authorization header");
            return null;
        }

        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            System.out.println("DEBUG: Token (first 20 chars): " + token.substring(0, Math.min(20, token.length())));
            
            // JWT format: header.payload.signature
            String[] parts = token.split("\\.");
            System.out.println("DEBUG: JWT parts count: " + parts.length);
            if (parts.length != 3) {
                System.out.println("DEBUG: Invalid JWT structure");
                return null;
            }

            // Decode the payload (second part)
            String payload = parts[1];
            // Add padding if needed
            int padding = 4 - (payload.length() % 4);
            if (padding != 4) {
                payload += "=".repeat(padding);
            }

            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String decodedPayload = new String(decodedBytes);
            System.out.println("DEBUG: Decoded payload: " + decodedPayload);
            
            // Parse JSON to get 'sub' claim (user ID)
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(decodedPayload, Map.class);
            Object sub = claims.get("sub");
            System.out.println("DEBUG: Extracted sub (user_id): " + sub);
            
            if (sub != null) {
                return sub.toString();
            }
            
            return null;

        } catch (Exception ex) {
            System.err.println("ERROR: Failed to extract user from token: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Verify token is present and extract user ID
     */
    public String verifyAndGetUserId(String authHeader) {
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            System.err.println("ERROR: Invalid or missing authorization token");
        }
        return userId;
    }
}
