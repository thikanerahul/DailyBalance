package com.example.dailybalance.utils;

import com.example.dailybalance.data.local.entity.Task;
import com.example.dailybalance.data.local.entity.Habit;
import java.text.SimpleDateFormat;
import java.util.*;

public class AnalyticsHelper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Calculate current streak from completedDates string
     * Returns number of consecutive days including today
     */
    public static int calculateCurrentStreak(String completedDates) {
        if (completedDates == null || completedDates.isEmpty()) {
            return 0;
        }

        Set<String> dates = TaskCompletionHelper.getCompletedDatesSet(completedDates);
        if (dates.isEmpty()) {
            return 0;
        }

        String today = TaskCompletionHelper.getTodayDateString();

        // If today is not completed, streak is broken
        if (!dates.contains(today)) {
            return 0;
        }

        int streak = 1; // Today counts
        Calendar cal = Calendar.getInstance();

        // Go backwards from yesterday
        cal.add(Calendar.DAY_OF_MONTH, -1);

        while (true) {
            String dateStr = DATE_FORMAT.format(cal.getTime());
            if (dates.contains(dateStr)) {
                streak++;
                cal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * Calculate longest streak from completedDates string
     */
    public static int calculateLongestStreak(String completedDates) {
        if (completedDates == null || completedDates.isEmpty()) {
            return 0;
        }

        Set<String> dateSet = TaskCompletionHelper.getCompletedDatesSet(completedDates);
        if (dateSet.isEmpty()) {
            return 0;
        }

        // Convert to sorted list
        List<Date> dates = new ArrayList<>();
        for (String dateStr : dateSet) {
            try {
                dates.add(DATE_FORMAT.parse(dateStr));
            } catch (Exception e) {
                // Skip invalid dates
            }
        }

        if (dates.isEmpty()) {
            return 0;
        }

        Collections.sort(dates);

        int longestStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < dates.size(); i++) {
            long diff = dates.get(i).getTime() - dates.get(i - 1).getTime();
            long daysDiff = diff / (1000 * 60 * 60 * 24);

            if (daysDiff == 1) {
                // Consecutive day
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                // Streak broken
                currentStreak = 1;
            }
        }

        return longestStreak;
    }

    /**
     * Get completion rate for a specific date
     * Returns percentage (0-100)
     */
    public static float getCompletionRate(List<Task> allTasks, String date) {
        if (allTasks == null || allTasks.isEmpty()) {
            return 0f;
        }

        int totalTasks = 0;
        int completedTasks = 0;

        for (Task task : allTasks) {
            // Check if task is for this date
            String taskDate = DATE_FORMAT.format(new Date(task.dateTime));

            if (task.isRecurring) {
                // Recurring task - check if completed on this date
                totalTasks++;
                if (task.completedDates != null && task.completedDates.contains(date)) {
                    completedTasks++;
                }
            } else if (taskDate.equals(date)) {
                // One-time task for this date
                totalTasks++;
                if (task.isCompleted) {
                    completedTasks++;
                }
            }
        }

        if (totalTasks == 0) {
            return 0f;
        }

        return (completedTasks * 100f) / totalTasks;
    }

    /**
     * Get weekly completion stats (last 7 days)
     * Returns array of 7 completion percentages
     */
    public static float[] getWeeklyStats(List<Task> allTasks) {
        float[] stats = new float[7];
        Calendar cal = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            String date = DATE_FORMAT.format(cal.getTime());
            stats[i] = getCompletionRate(allTasks, date);
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        return stats;
    }

    /**
     * Count completed tasks for a specific date
     */
    public static int getCompletedTaskCount(List<Task> allTasks, String date) {
        if (allTasks == null) {
            return 0;
        }

        int count = 0;
        for (Task task : allTasks) {
            if (task.isRecurring) {
                // Check completedDates
                if (task.completedDates != null && task.completedDates.contains(date)) {
                    count++;
                }
            } else {
                // Check if task date matches and is completed
                String taskDate = DATE_FORMAT.format(new Date(task.dateTime));
                if (taskDate.equals(date) && task.isCompleted) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Get task completion counts for last 7 days
     * Returns array of 7 counts (oldest to newest)
     */
    public static int[] getWeeklyCompletionCounts(List<Task> allTasks) {
        int[] counts = new int[7];
        Calendar cal = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            String date = DATE_FORMAT.format(cal.getTime());
            counts[i] = getCompletedTaskCount(allTasks, date);
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        return counts;
    }
}
