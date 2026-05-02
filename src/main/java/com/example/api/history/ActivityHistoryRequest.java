package com.example.api.history;

public class ActivityHistoryRequest {
    private Object userId;  // Accept both String (UUID) and numeric IDs
    private String activityType;
    private String moodValue;
    private String note;
    private String description;

    // Constructors
    public ActivityHistoryRequest() {}

    public ActivityHistoryRequest(Object userId, String activityType, String moodValue, String note, String description) {
        this.userId = userId;
        this.activityType = activityType;
        this.moodValue = moodValue;
        this.note = note;
        this.description = description;
    }

    // Getters and Setters
    public Object getUserId() { return userId; }
    public void setUserId(Object userId) { this.userId = userId; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getMoodValue() { return moodValue; }
    public void setMoodValue(String moodValue) { this.moodValue = moodValue; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "ActivityHistoryRequest{userId=" + userId + ", activityType='" + activityType + 
               "', moodValue='" + moodValue + "', note='" + note + "', description='" + description + "'}";
    }
}
