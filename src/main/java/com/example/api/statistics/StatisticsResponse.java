package com.example.api.statistics;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatisticsResponse {

    @JsonProperty("most_frequent_mood")
    private MostFrequentMood mostFrequentMood;
    
    @JsonProperty("weekly_overview")
    private List<DayOverview> weeklyOverview;
    
    @JsonProperty("monthly_overview")
    private List<WeekOverview> monthlyOverview;
    
    @JsonProperty("total_entries")
    private int totalEntries;
    
    @JsonProperty("day_streak")
    private int dayStreak;

    // Constructors
    public StatisticsResponse() {}

    public StatisticsResponse(MostFrequentMood mostFrequentMood, List<DayOverview> weeklyOverview,
            List<WeekOverview> monthlyOverview, int totalEntries, int dayStreak) {
        this.mostFrequentMood = mostFrequentMood;
        this.weeklyOverview = weeklyOverview;
        this.monthlyOverview = monthlyOverview;
        this.totalEntries = totalEntries;
        this.dayStreak = dayStreak;
    }

    // Getters and Setters
    public MostFrequentMood getMostFrequentMood() { return mostFrequentMood; }
    public void setMostFrequentMood(MostFrequentMood mostFrequentMood) { this.mostFrequentMood = mostFrequentMood; }

    public List<DayOverview> getWeeklyOverview() { return weeklyOverview; }
    public void setWeeklyOverview(List<DayOverview> weeklyOverview) { this.weeklyOverview = weeklyOverview; }

    public List<WeekOverview> getMonthlyOverview() { return monthlyOverview; }
    public void setMonthlyOverview(List<WeekOverview> monthlyOverview) { this.monthlyOverview = monthlyOverview; }

    public int getTotalEntries() { return totalEntries; }
    public void setTotalEntries(int totalEntries) { this.totalEntries = totalEntries; }

    public int getDayStreak() { return dayStreak; }
    public void setDayStreak(int dayStreak) { this.dayStreak = dayStreak; }

    // Inner class for most frequent mood
    public static class MostFrequentMood {
        private String mood;
        private int count;
        private double percentage;

        public MostFrequentMood() {}
        public MostFrequentMood(String mood, int count, double percentage) {
            this.mood = mood;
            this.count = count;
            this.percentage = percentage;
        }

        public String getMood() { return mood; }
        public void setMood(String mood) { this.mood = mood; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    // Inner class for daily overview
    public static class DayOverview {
        private String day; // Mon, Tue, Wed, etc.
        private int count;

        public DayOverview() {}
        public DayOverview(String day, int count) {
            this.day = day;
            this.count = count;
        }

        public String getDay() { return day; }
        public void setDay(String day) { this.day = day; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    // Inner class for weekly overview
    public static class WeekOverview {
        private String week; // "Week 1", "Week 2", etc.
        private int count;

        public WeekOverview() {}
        public WeekOverview(String week, int count) {
            this.week = week;
            this.count = count;
        }

        public String getWeek() { return week; }
        public void setWeek(String week) { this.week = week; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }
}
