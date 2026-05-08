package com.example.api.history;

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
@RequestMapping("/activity-history")
public class ActivityHistoryController {
    private static final Logger logger = LoggerFactory.getLogger(ActivityHistoryController.class);    private final ActivityHistoryService activityHistoryService;
    private final AuthUtil authUtil;

    public ActivityHistoryController(ActivityHistoryService activityHistoryService, AuthUtil authUtil) {
        this.activityHistoryService = activityHistoryService;
        this.authUtil = authUtil;
    }

    @PostMapping
    public ResponseEntity<?> recordActivity(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ActivityHistoryRequest request) {
        
        logger.debug("POST /api/activity-history called");
        logger.debug("Authorization header present: {}", authHeader != null);
        
        try {
            // Extract userId from JWT token (optional for demo mode)
            String userId = authUtil.verifyAndGetUserId(authHeader);
            logger.debug("After JWT verification, userId: {}", userId);
            
            if (userId == null || userId.isEmpty()) {
                Object reqUserId = request.getUserId();
                if (reqUserId != null) {
                    userId = reqUserId.toString();
                    logger.debug("Using userId from request body (demo mode): {}", userId);
                } else {
                    userId = "demo-user-" + System.currentTimeMillis();
                    logger.debug("Generated demo userId: {}", userId);
                }
            } else {
                logger.debug("Using authenticated userId from JWT: {}", userId);
            }

            // Use userId (from JWT or demo mode)
            request.setUserId(userId);
            logger.debug("Saved activity history with userId: {}", userId);
            
            ActivityHistory savedActivity = activityHistoryService.saveActivityHistory(request);
            logger.debug("Activity history saved successfully with id: {}", savedActivity.getId());
            return ResponseEntity.ok(savedActivity);
            
        } catch (Exception ex) {
            logger.error("Exception in recordActivity: {}", ex.getMessage(), ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error saving activity history");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyActivityHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        logger.debug("GET /api/activity-history called");
        logger.debug("Authorization header present: {}", authHeader != null);
        
        try {
            String userId = authUtil.verifyAndGetUserId(authHeader);
            logger.debug("After JWT verification, userId: {}", userId);
            
            // If no valid JWT, allow demo mode (same as POST endpoint)
            if (userId == null || userId.isEmpty()) {
                logger.debug("No valid JWT provided, allowing demo mode access");
                userId = "demo-user-" + System.currentTimeMillis();
                logger.debug("Generated demo userId: {}", userId);
            } else {
                logger.debug("Using authenticated userId from JWT: {}", userId);
            }
            
            logger.debug("Retrieving activity history for user: {}", userId);

            List<ActivityHistory> history = activityHistoryService.getActivityHistoryByUser(userId);
            logger.debug("Found {} activities for user: {}", history.size(), userId);
            
            return ResponseEntity.ok(history);
            
        } catch (Exception ex) {
            logger.error("Exception in getMyActivityHistory: {}", ex.getMessage(), ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error retrieving activity history");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserActivityHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId) {
        
        logger.debug("GET /api/activity-history/user/{} called", userId);
        
        try {
            // Extract userId from JWT token if available, otherwise use path parameter
            String verifiedUserId = authUtil.verifyAndGetUserId(authHeader);
            if (verifiedUserId != null && !verifiedUserId.isEmpty()) {
                userId = verifiedUserId;
            }
            logger.debug("Retrieving activity history for user: {}", userId);

            List<ActivityHistory> history = activityHistoryService.getActivityHistoryByUser(userId);
            return ResponseEntity.ok(history);
            
        } catch (Exception ex) {
            logger.error("Exception in getUserActivityHistory: {}", ex.getMessage(), ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error retrieving activity history");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/user/{userId}/type/{activityType}")
    public ResponseEntity<?> getUserActivityHistoryByType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId,
            @PathVariable String activityType) {
        
        logger.debug("GET /api/activity-history/user/{}/type/{} called", userId, activityType);
        
        try {
            // In demo mode, allow access to any user's history
            logger.debug("Retrieving activity history for user: {} type: {} (demo mode)", userId, activityType);

            List<ActivityHistory> history = activityHistoryService.getActivityHistoryByUserAndType(userId, activityType);
            return ResponseEntity.ok(history);
            
        } catch (Exception ex) {
            logger.error("Exception in getUserActivityHistoryByType: {}", ex.getMessage(), ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error retrieving activity history");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
