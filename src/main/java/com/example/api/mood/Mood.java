package com.example.api.mood;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Mood {

    private Long id;
    @JsonProperty("user_id")
    private String userId;  // Store as String to support both numeric and UUID formats
    @JsonProperty("mood_value")
    private String moodValue; 
    private String note;
    @JsonProperty("created_at")
    private Instant createdAt;

    // Constructors
    public Mood() {
        this.createdAt = Instant.now();
    }

    public Mood(Object userId, String moodValue, String note) {
        this.userId = userId != null ? userId.toString() : null;
        this.moodValue = moodValue;
        this.note = note;
        this.createdAt = Instant.now();
    }

    public Mood(Object userId, String moodValue, String note, Instant createdAt) {
        this.userId = userId != null ? userId.toString() : null;
        this.moodValue = moodValue;
        this.note = note;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(Object userId) { this.userId = userId != null ? userId.toString() : null; }
    public String getMoodValue() { return moodValue; }
    public void setMoodValue(String moodValue) { this.moodValue = moodValue; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}