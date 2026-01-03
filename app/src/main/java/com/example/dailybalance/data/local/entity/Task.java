package com.example.dailybalance.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public String description;
    public long dateTime; // Store as timestamp
    public int priority; // 0=Low, 1=Medium, 2=High
    public String category; // Work, Personal, etc.
    public boolean isCompleted;
    public boolean isRecurring; // true if task repeats daily (no specific date)
    public String completedDates; // Comma-separated dates when task was completed (yyyy-MM-dd)
    
    // Notification preferences
    public boolean enableNotification; // Show notification
    public boolean enableAlarm; // Play alarm sound

    public Task(String title, String description, long dateTime, int priority, String category, boolean isRecurring, boolean enableNotification, boolean enableAlarm) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.priority = priority;
        this.category = category;
        this.isCompleted = false;
        this.isRecurring = isRecurring;
        this.completedDates = "";
        this.enableNotification = enableNotification;
        this.enableAlarm = enableAlarm;
    }

    // Backward compatibility constructor
    @Ignore
    public Task(String title, String description, long dateTime, int priority, String category, boolean isRecurring) {
        this(title, description, dateTime, priority, category, isRecurring, true, true);
    }

    // Backward compatibility constructor
    @Ignore
    public Task(String title, String description, long dateTime, int priority, String category) {
        this(title, description, dateTime, priority, category, false, true, true);
    }
}
