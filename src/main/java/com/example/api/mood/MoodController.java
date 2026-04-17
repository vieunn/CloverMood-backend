package com.example.api.mood;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@RequestMapping("/api/moods")
public class MoodController {

    private final MoodService moodService;
    private final AuthUtil authUtil;

    public MoodController(MoodService moodService, AuthUtil authUtil) {
        this.moodService = moodService;
        this.authUtil = authUtil;
    }

    @PostMapping
    public ResponseEntity<?> recordMood(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody MoodRequest request) {
        
        System.out.println("DEBUG: POST /api/moods called");
        System.out.println("DEBUG: Full request: " + request);
        System.out.println("DEBUG: Authorization header present: " + (authHeader != null));
        
        try {
            // TEMPORARY: For local testing - accept request if userId is provided in body
            String userId = null;
            if (request.getUserId() != null) {
                userId = request.getUserId().toString().trim();
                System.out.println("DEBUG: Using userId from request body: " + userId + " (type: " + request.getUserId().getClass().getName() + ")");
            } else {
                // Try to get from token if available
                userId = authUtil.verifyAndGetUserId(authHeader);
                System.out.println("DEBUG: Extracted userId from token: " + userId);
            }
            
            if (userId == null || userId.isEmpty()) {
                System.out.println("DEBUG: userId is null or empty - returning 400");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "userId is required in request body or Authorization header");
                error.put("receivedUserId", request.getUserId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Keep userId as-is (String UUID or numeric)
            request.setUserId(userId);
            System.out.println("DEBUG: Saved request with userId: " + userId);
            
            Mood savedMood = moodService.saveMood(request);
            System.out.println("DEBUG: Mood saved successfully with id: " + savedMood.getId());
            return ResponseEntity.ok(savedMood);
            
        } catch (Exception ex) {
            System.err.println("ERROR: Exception in recordMood: " + ex.getMessage());
            ex.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error saving mood: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserMoods(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId) {
        
        // TEMPORARY: For local testing - allow requests without auth if userId is in path
        // Once frontend properly sends JWT token, uncomment auth check below
        System.out.println("DEBUG: GET /user/" + userId + " called");
        
        String authenticatedUserId = authUtil.verifyAndGetUserId(authHeader);
        if (authenticatedUserId != null) {
            // Auth token provided - verify user can only access their own moods
            if (!authenticatedUserId.equals(userId.toString())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Forbidden - cannot access other user's moods");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        }
        // If no auth token, still allow access (temporary for local testing)

        List<Mood> moods = moodService.getMoodsByUser(userId);
        return ResponseEntity.ok(moods);
    }
}