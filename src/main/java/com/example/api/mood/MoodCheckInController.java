package com.example.api.mood;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.util.AuthUtil;

@RestController
@RequestMapping("/mood")
public class MoodCheckInController {
    private static final Logger logger = LoggerFactory.getLogger(MoodCheckInController.class);
    private final MoodService moodService;
    private final AuthUtil authUtil;

    public MoodCheckInController(MoodService moodService, AuthUtil authUtil) {
        this.moodService = moodService;
        this.authUtil = authUtil;
    }

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> request) {
        
        logger.debug("POST /api/mood/check-in called");
        
        try {
            // Extract userId from JWT token (optional for demo mode)
            String userId = authUtil.verifyAndGetUserId(authHeader);
            
            // If no valid JWT, use demo mode with userId from request or generate one
            if (userId == null || userId.isEmpty()) {
                if (request.containsKey("userId") && request.get("userId") != null) {
                    userId = request.get("userId").toString();
                    logger.debug("Using userId from request (demo mode): {}", userId);
                } else {
                    // Generate a demo user ID
                    userId = "demo-user-" + System.currentTimeMillis();
                    logger.debug("Generated demo userId: {}", userId);
                }
            }

            // Parse frontend format: { userId, mood, thought }
            String moodValue = (String) request.get("mood");
            String thought = (String) request.get("thought");
            
            if (moodValue == null || moodValue.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Mood value is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Create MoodRequest with verified userId
            MoodRequest moodRequest = new MoodRequest();
            moodRequest.setUserId(userId);
            moodRequest.setMoodValue(moodValue);
            moodRequest.setNote(thought != null ? thought : "");
            moodRequest.setCreatedAt(Instant.now());
            
            logger.debug("Check-in request with verified userId: {}", userId);
            
            Mood savedMood = moodService.saveMood(moodRequest);
            logger.debug("Check-in saved successfully with id: {}", savedMood.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Check-in saved successfully");
            response.put("data", savedMood);
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            logger.error("Exception in check-in: {}", ex.getMessage(), ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error saving check-in");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
