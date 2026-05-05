package com.example.api.statistics;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.api.mood.Mood;
import com.example.api.mood.MoodService;

@Service
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);
    private final MoodService moodService;

    public StatisticsService(MoodService moodService) {
        this.moodService = moodService;
    }

    // GET /statistics/{userId} - Get statistics for user's moods
    public StatisticsResponse getStatistics(String userId) {
        try {
            List<Mood> moods = moodService.getMoodsByUser(userId);
            ZoneId zoneId = ZoneId.systemDefault();
            
            if (moods.isEmpty()) {
                return new StatisticsResponse(
                    null,
                    generateEmptyWeeklyOverview(),
                    generateEmptyMonthlyOverview(),
                    0,
                    0
                );
            }

            StatisticsResponse.MostFrequentMood mostFrequent = calculateMostFrequentMood(moods);
            List<StatisticsResponse.DayOverview> weeklyData = calculateWeeklyOverview(moods, zoneId);
            List<StatisticsResponse.WeekOverview> monthlyData = calculateMonthlyOverview(moods, zoneId);
            int totalEntries = moods.size();
            int streak = calculateDayStreak(moods, zoneId);

            return new StatisticsResponse(mostFrequent, weeklyData, monthlyData, totalEntries, streak);

        } catch (Exception ex) {
            logger.error("Exception calculating statistics: {}", ex.getMessage(), ex);
            return new StatisticsResponse();
        }
    }

    private StatisticsResponse.MostFrequentMood calculateMostFrequentMood(List<Mood> moods) {
        Map<String, Integer> moodCounts = new HashMap<>();
        for (Mood mood : moods) {
            moodCounts.put(mood.getMoodValue(), moodCounts.getOrDefault(mood.getMoodValue(), 0) + 1);
        }

        String mostFrequentMoodValue = moodCounts.entrySet().stream()
            .max((a, b) -> a.getValue().compareTo(b.getValue()))
            .map(Map.Entry::getKey)
            .orElse("unknown");

        int count = moodCounts.getOrDefault(mostFrequentMoodValue, 0);
        double percentage = (count * 100.0) / moods.size();

        return new StatisticsResponse.MostFrequentMood(mostFrequentMoodValue, count, Math.round(percentage * 10.0) / 10.0);
    }

    private List<StatisticsResponse.DayOverview> calculateWeeklyOverview(List<Mood> moods, ZoneId zoneId) {
        Map<String, Integer> dayCount = new HashMap<>();
        String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        LocalDate today = LocalDate.now(zoneId);
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        
        // Initialize all days with 0
        for (String day : daysOfWeek) {
            dayCount.put(day, 0);
        }

        // Count moods only for the current week so the chart reflects recent activity
        for (Mood mood : moods) {
            if (mood.getCreatedAt() != null) {
                LocalDate createdDate = mood.getCreatedAt().atZone(zoneId).toLocalDate();
                if (!createdDate.isBefore(startOfWeek) && !createdDate.isAfter(today)) {
                    String dayName = createdDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    dayCount.put(dayName, dayCount.getOrDefault(dayName, 0) + 1);
                }
            }
        }

        List<StatisticsResponse.DayOverview> result = new ArrayList<>();
        for (String day : daysOfWeek) {
            result.add(new StatisticsResponse.DayOverview(day, dayCount.getOrDefault(day, 0)));
        }
        return result;
    }

    private List<StatisticsResponse.WeekOverview> calculateMonthlyOverview(List<Mood> moods, ZoneId zoneId) {
        Map<Integer, Integer> weekCount = new HashMap<>();

        // Count moods by week of month
        for (Mood mood : moods) {
            if (mood.getCreatedAt() != null) {
                LocalDateTime createdAt = mood.getCreatedAt().atZone(zoneId).toLocalDateTime();
                int dayOfMonth = createdAt.getDayOfMonth();
                int weekOfMonth = (dayOfMonth - 1) / 7 + 1; // Week 1, 2, 3, 4, 5
                weekCount.put(weekOfMonth, weekCount.getOrDefault(weekOfMonth, 0) + 1);
            }
        }

        List<StatisticsResponse.WeekOverview> result = new ArrayList<>();
        for (int week = 1; week <= 5; week++) {
            result.add(new StatisticsResponse.WeekOverview("W" + week, weekCount.getOrDefault(week, 0)));
        }
        return result;
    }

    private int calculateDayStreak(List<Mood> moods, ZoneId zoneId) {
        if (moods.isEmpty()) {
            return 0;
        }

        // Collapse multiple entries from the same day before calculating the streak
        List<LocalDate> uniqueDates = moods.stream()
            .filter(m -> m.getCreatedAt() != null)
            .map(m -> m.getCreatedAt().atZone(zoneId).toLocalDate())
            .distinct()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        if (uniqueDates.isEmpty()) {
            return 0;
        }

        int streak = 1;
        LocalDate previousDate = uniqueDates.get(0);

        for (int i = 1; i < uniqueDates.size(); i++) {
            LocalDate currentDate = uniqueDates.get(i);
            // Check if dates are consecutive (exactly 1 day apart)
            if (currentDate.equals(previousDate.minusDays(1))) {
                streak++;
                previousDate = currentDate;
            } else {
                break; // Streak broken
            }
        }

        return streak;
    }

    private List<StatisticsResponse.DayOverview> generateEmptyWeeklyOverview() {
        List<StatisticsResponse.DayOverview> result = new ArrayList<>();
        String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : daysOfWeek) {
            result.add(new StatisticsResponse.DayOverview(day, 0));
        }
        return result;
    }

    private List<StatisticsResponse.WeekOverview> generateEmptyMonthlyOverview() {
        List<StatisticsResponse.WeekOverview> result = new ArrayList<>();
        for (int week = 1; week <= 5; week++) {
            result.add(new StatisticsResponse.WeekOverview("W" + week, 0));
        }
        return result;
    }
}
