package com.example.api.util;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
     * Verify and extract user ID from Supabase JWT token with signature verification
     * The token structure is: Bearer {token}
     */
    public String extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("Missing or invalid Authorization header");
            return null;
        }

        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            // JWT secret MUST be configured
            if (jwtSecret == null || jwtSecret.isEmpty()) {
                logger.warn("JWT secret is not configured - cannot verify tokens. Set SUPABASE_JWT_SECRET environment variable.");
                return null;
            }
            
            // Decode base64-encoded JWT secret from Supabase
            byte[] decodedSecret = Base64.getDecoder().decode(jwtSecret);
            logger.debug("JWT secret decoded, length: {} bytes", decodedSecret.length);
            
            // Verify JWT with HS256
            Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(decodedSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            // Extract 'sub' claim (user ID from Supabase)
            String userId = claims.getSubject();
            logger.debug("JWT verified successfully. User ID: {}", userId);
            
            if (userId != null && !userId.isEmpty()) {
                return userId;
            }
            
            return null;

        } catch (JwtException ex) {
            logger.warn("JWT verification failed: {}", ex.getMessage());
            
            // Log token details for debugging (first 50 chars of token)
            try {
                String token = authHeader.substring(7);
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String decodedHeader = new String(Base64.getDecoder().decode(parts[0]));
                    logger.warn("Token header: {}", decodedHeader);
                }
            } catch (Exception e) {
                logger.debug("Could not decode token header: {}", e.getMessage());
            }
            
            return null;
        } catch (Exception ex) {
            logger.error("Failed to extract user from token: {}", ex.getMessage(), ex);
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
