package com.example.dailybalance.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.dailybalance.data.local.entity.Task;
import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY dateTime ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dateTime ASC")
    LiveData<List<Task>> getPendingTasks();

    @Query("SELECT * FROM tasks WHERE dateTime BETWEEN :start AND :end")
    LiveData<List<Task>> getTasksForDateRange(long start, long end);

    @Query("SELECT * FROM tasks WHERE isCompleted = 1")
    LiveData<List<Task>> getAllCompletedTasks();

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND dateTime BETWEEN :start AND :end")
    LiveData<List<Task>> getCompletedTasksBetween(long start, long end);

    @Query("SELECT * FROM tasks WHERE isRecurring = 1")
    LiveData<List<Task>> getRecurringTasks();

    @Query("SELECT * FROM tasks WHERE isRecurring = 0 AND dateTime BETWEEN :start AND :end")
    LiveData<List<Task>> getOneTimeTasksForDateRange(long start, long end);

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    Task getTaskById(long taskId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    @Update
    void update(Task task);

    @Query("UPDATE tasks SET completedDates = :completedDates WHERE id = :taskId")
    void updateCompletedDates(long taskId, String completedDates);

    @Delete
    void delete(Task task);
}
