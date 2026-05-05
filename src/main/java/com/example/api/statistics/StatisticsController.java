package com.example.api.statistics;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.util.AuthUtil;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);
    private final StatisticsService statisticsService;
    private final AuthUtil authUtil;

    public StatisticsController(StatisticsService statisticsService, AuthUtil authUtil) {
        this.statisticsService = statisticsService;
        this.authUtil = authUtil;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getStatistics(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId) {
        
        logger.debug("GET /api/statistics/{} called", userId);
        
        try {
            // In demo mode, allow access to any user's statistics
            logger.debug("Retrieving statistics for user: {} (demo mode)", userId);
            
            StatisticsResponse stats = statisticsService.getStatistics(userId);
            return ResponseEntity.ok(stats);
            
        } catch (Exception ex) {
            logger.error("Exception in getStatistics: {}", ex.getMessage(), ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Server error fetching statistics");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
