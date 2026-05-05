package com.example.api.mood;

import java.time.Instant;

public class MoodRequest {
    private Object userId;  // Accept both String (UUID) and numeric IDs
    private String moodValue;
    private String note;
    private Instant createdAt;

    // Constructors
    public MoodRequest() {}

    // Getters and Setters
    public Object getUserId() { return userId; }
    public void setUserId(Object userId) { this.userId = userId; }
    public String getMoodValue() { return moodValue; }
    public void setMoodValue(String moodValue) { this.moodValue = moodValue; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "MoodRequest{userId=" + userId + ", moodValue='" + moodValue + "', note='" + note + "', createdAt='" + createdAt + "'}";
    }
}