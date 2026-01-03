package com.example.dailybalance.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public int streak; // Kept for backward compatibility
    public long lastCompletedDate; // Timestamp of last completion
    public String completedDates; // Comma-separated dates "yyyy-MM-dd"
    public int currentStreak; // Current consecutive days
    public int longestStreak; // Best streak achieved

    public Habit(String name, int streak, long lastCompletedDate) {
        this.name = name;
        this.streak = streak;
        this.lastCompletedDate = lastCompletedDate;
        this.completedDates = "";
        this.currentStreak = 0;
        this.longestStreak = 0;
    }
}
