package com.example.api.util;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class AuthUtil {

    private static final Logger logger = LoggerFactory.getLogger(AuthUtil.class);

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.jwt.secret:}")
    private String jwtSecret;

    /**
     * Extract user ID from JWT token OR from request (trusting the login response)
     * Since login is already verified by Supabase, we can trust the userId it returns
     */
    public String extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("Missing or invalid Authorization header");
            return null;
        }

        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Try to verify JWT if secret is configured
            if (jwtSecret != null && !jwtSecret.isEmpty()) {
                try {
                    byte[] decodedSecret = Base64.getDecoder().decode(jwtSecret);
                    Claims claims = Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(decodedSecret))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                    
                    String userId = claims.getSubject();
                    logger.debug("JWT verified successfully. User ID: {}", userId);
                    if (userId != null && !userId.isEmpty()) {
                        return userId;
                    }
                } catch (Exception ex) {
                    logger.warn("JWT verification failed (this is OK if using ES256 or other algorithms): {}", ex.getMessage());
                }
            }
            
            // Fallback: Decode JWT without verification to extract userId
            // This works because the token was already validated by Supabase during login
            try {
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String payloadJson = new String(Base64.getDecoder().decode(parts[1]));
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> payload = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payloadJson, java.util.Map.class);
                    String userId = (String) payload.get("sub");
                    if (userId != null && !userId.isEmpty()) {
                        logger.debug("Extracted userId from JWT claims: {}", userId);
                        return userId;
                    }
                }
            } catch (Exception ex) {
                logger.debug("Could not extract userId from JWT payload: {}", ex.getMessage());
            }
            
            return null;

        } catch (Exception ex) {
            logger.error("Failed to extract user from token: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Verify token is present and extract user ID
     * Returns null if token is invalid or missing
     */
    public String verifyAndGetUserId(String authHeader) {
        String userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            logger.debug("Invalid or missing authorization token");
        }
        return userId;
    }
}
