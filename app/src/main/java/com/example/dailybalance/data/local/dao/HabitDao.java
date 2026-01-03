package com.example.dailybalance.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.dailybalance.data.local.entity.Habit;
import java.util.List;

@Dao
public interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY id ASC")
    LiveData<List<Habit>> getAllHabits();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Habit habit);

    @Update
    void update(Habit habit);

    @androidx.room.Delete
    void delete(Habit habit);

    @Query("SELECT COUNT(*) FROM habits")
    int getHabitCount();

    @Query("UPDATE habits SET completedDates = :completedDates WHERE id = :habitId")
    void updateCompletedDates(long habitId, String completedDates);

    @Query("UPDATE habits SET currentStreak = :currentStreak, longestStreak = :longestStreak WHERE id = :habitId")
    void updateStreaks(long habitId, int currentStreak, int longestStreak);
}
