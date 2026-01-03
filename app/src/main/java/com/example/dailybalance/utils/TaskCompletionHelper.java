package com.example.dailybalance.utils;

import com.example.dailybalance.data.local.entity.Task;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TaskCompletionHelper {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Get today's date as a string in yyyy-MM-dd format
     */
    public static String getTodayDateString() {
        return DATE_FORMAT.format(new Date());
    }

    /**
     * Check if a task is completed for today
     */
    public static boolean isCompletedToday(Task task) {
        if (task.completedDates == null || task.completedDates.isEmpty()) {
            return false;
        }
        String today = getTodayDateString();
        return task.completedDates.contains(today);
    }

    /**
     * Mark a task as completed for today
     * Returns the updated completedDates string
     */
    public static String markTaskCompleteForToday(Task task) {
        String today = getTodayDateString();

        if (task.completedDates == null || task.completedDates.isEmpty()) {
            return today;
        }

        // Check if already completed today
        if (task.completedDates.contains(today)) {
            return task.completedDates; // Already completed
        }

        // Add today's date
        return task.completedDates + "," + today;
    }

    /**
     * Remove today's completion (unmark task)
     * Returns the updated completedDates string
     */
    public static String unmarkTaskForToday(Task task) {
        if (task.completedDates == null || task.completedDates.isEmpty()) {
            return "";
        }

        String today = getTodayDateString();
        Set<String> dates = getCompletedDatesSet(task.completedDates);
        dates.remove(today);

        return String.join(",", dates);
    }

    /**
     * Parse completedDates string into a Set
     */
    public static Set<String> getCompletedDatesSet(String completedDates) {
        Set<String> dates = new HashSet<>();
        if (completedDates != null && !completedDates.isEmpty()) {
            String[] dateArray = completedDates.split(",");
            for (String date : dateArray) {
                if (!date.trim().isEmpty()) {
                    dates.add(date.trim());
                }
            }
        }
        return dates;
    }

    /**
     * Get formatted date string from timestamp
     */
    public static String getDateStringFromTimestamp(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }
}
