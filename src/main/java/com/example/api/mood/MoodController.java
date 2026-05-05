package com.example.api.mood;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.util.AuthUtil;

@RestController
@RequestMapping("/moods")
public class MoodController {
    private static final Logger logger = LoggerFactory.getLogger(MoodController.class);    private final MoodService moodService;
    private final AuthUtil authUtil;

    public MoodController(MoodService moodService, AuthUtil authUtil) {
        this.moodService = moodService;
        this.authUtil = authUtil;
    }

    @PostMapping
    public ResponseEntity<?> recordMood(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody MoodRequest request) {
        
        logger.debug("POST /api/moods called");
        
        try {
            // Extract userId from JWT token (optional for demo mode)
            String userId = authUtil.verifyAndGetUserId(authHeader);
            
            if (userId == null || userId.isEmpty()) {
                Object reqUserId = request.getUserId();
                if (reqUserId != null) {
                    userId = reqUserId.toString();
                    logger.debug("Using userId from request (demo mode): {}", userId);
                } else {
                    userId = "demo-user-" + System.currentTimeMillis();
                    logger.debug("Generated demo userId: {}", userId);
                }
            }

            // Use userId (from JWT or demo mode)
            request.setUserId(userId);
            logger.debug("Saved mood request with verified userId: {}", userId);
            
            Mood savedMood = moodService.saveMood(request);
            logger.debug("Mood saved successfully with id: {}", savedMood.getId());
            return ResponseEntity.ok(savedMood);
            
        } catch (Exception ex) {
            logger.error("Exception in recordMood: {}", ex.getMessage(), ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error saving mood");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserMoods(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId) {
        
        logger.debug("GET /user/{} called", userId);
        
        try {
            // In demo mode, allow access to any user's moods
            logger.debug("Retrieving moods for user: {} (demo mode)", userId);

            List<Mood> moods = moodService.getMoodsByUser(userId);
            return ResponseEntity.ok(moods);
            
        } catch (Exception ex) {
            logger.error("Exception in getUserMoods: {}", ex.getMessage(), ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error retrieving moods");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}