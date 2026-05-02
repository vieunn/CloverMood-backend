package com.example.api.history;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActivityHistory {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("user_id")
    private String userId;  // Store as String to support both numeric and UUID formats
    @JsonProperty("activity_type")
    private String activityType;  // e.g., "mood_recorded", "mood_updated", etc.
    @JsonProperty("mood_value")
    private String moodValue;  // The mood recorded
    @JsonProperty("note")
    private String note;  // Optional note from the mood
    @JsonProperty("description")
    private String description;  // More detailed description of the activity
    @JsonProperty("timestamp")
    private Instant timestamp;

    // Constructors
    public ActivityHistory() {
        this.timestamp = Instant.now();
    }

    public ActivityHistory(Object userId, String activityType, String moodValue, String note, String description) {
        this.userId = userId != null ? userId.toString() : null;
        this.activityType = activityType;
        this.moodValue = moodValue;
        this.note = note;
        this.description = description;
        this.timestamp = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(Object userId) { this.userId = userId != null ? userId.toString() : null; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getMoodValue() { return moodValue; }
    public void setMoodValue(String moodValue) { this.moodValue = moodValue; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
