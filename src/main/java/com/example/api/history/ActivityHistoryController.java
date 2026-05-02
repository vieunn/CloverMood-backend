package com.example.api.history;

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
@RequestMapping("/api/activity-history")
public class ActivityHistoryController {

    private final ActivityHistoryService activityHistoryService;
    private final AuthUtil authUtil;

    public ActivityHistoryController(ActivityHistoryService activityHistoryService, AuthUtil authUtil) {
        this.activityHistoryService = activityHistoryService;
        this.authUtil = authUtil;
    }

    @PostMapping
    public ResponseEntity<?> recordActivity(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ActivityHistoryRequest request) {
        
        System.out.println("DEBUG: POST /api/activity-history called");
        System.out.println("DEBUG: Full request: " + request);
        System.out.println("DEBUG: Authorization header present: " + (authHeader != null));
        
        try {
            // TEMPORARY: For local testing - accept request if userId is provided in body
            String userId = null;
            if (request.getUserId() != null) {
                userId = request.getUserId().toString().trim();
                System.out.println("DEBUG: Using userId from request body: " + userId);
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
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Keep userId as-is (String UUID or numeric)
            request.setUserId(userId);
            System.out.println("DEBUG: Saved request with userId: " + userId);
            
            ActivityHistory savedActivity = activityHistoryService.saveActivityHistory(request);
            System.out.println("DEBUG: Activity history saved successfully with id: " + savedActivity.getId());
            return ResponseEntity.ok(savedActivity);
            
        } catch (Exception ex) {
            System.err.println("ERROR: Exception in recordActivity: " + ex.getMessage());
            ex.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error saving activity history: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserActivityHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId) {
        
        System.out.println("DEBUG: GET /api/activity-history/user/" + userId + " called");
        
        String authenticatedUserId = authUtil.verifyAndGetUserId(authHeader);
        if (authenticatedUserId != null) {
            // Auth token provided - verify user can only access their own history
            if (!authenticatedUserId.equals(userId.toString())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Forbidden - cannot access other user's activity history");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        }
        // If no auth token, still allow access (temporary for local testing)

        List<ActivityHistory> history = activityHistoryService.getActivityHistoryByUser(userId);
        // Return raw list (frontend expects an array of activity items)
        return ResponseEntity.ok(history);
    }

    @GetMapping("/user/{userId}/type/{activityType}")
    public ResponseEntity<?> getUserActivityHistoryByType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId,
            @PathVariable String activityType) {
        
        System.out.println("DEBUG: GET /api/activity-history/user/" + userId + "/type/" + activityType + " called");
        
        String authenticatedUserId = authUtil.verifyAndGetUserId(authHeader);
        if (authenticatedUserId != null) {
            // Auth token provided - verify user can only access their own history
            if (!authenticatedUserId.equals(userId.toString())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Forbidden - cannot access other user's activity history");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
        }
        // If no auth token, still allow access (temporary for local testing)

        List<ActivityHistory> history = activityHistoryService.getActivityHistoryByUserAndType(userId, activityType);
        // Return raw list (frontend expects an array of activity items)
        return ResponseEntity.ok(history);
    }
}
