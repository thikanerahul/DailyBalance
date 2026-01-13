package com.example.dailybalance.ui;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.dailybalance.data.local.AppDatabase;
import com.example.dailybalance.data.local.dao.TaskDao;
import com.example.dailybalance.data.local.dao.HabitDao;
import com.example.dailybalance.data.local.entity.Task;
import com.example.dailybalance.data.local.entity.Habit;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.lifecycle.Transformations;
import java.util.ArrayList;

public class DailyViewModel extends AndroidViewModel {

    private TaskDao taskDao;
    private HabitDao habitDao;
    private LiveData<List<Task>> allTasks;
    private LiveData<List<Habit>> allHabits;
    private ExecutorService executorService;

    public DailyViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        taskDao = database.taskDao();
        habitDao = database.habitDao();
        allTasks = taskDao.getAllTasks();
        allHabits = habitDao.getAllHabits();
        executorService = Executors.newSingleThreadExecutor();
    }

    public TaskDao getTaskDao() {
        return taskDao;
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<Float> getProductivityPercentage() {
        return Transformations.map(allTasks, tasks -> {
            if (tasks == null || tasks.isEmpty()) {
                return 0f;
            }
            int total = tasks.size();
            int completed = 0;
            for (Task t : tasks) {
                if (t.isCompleted)
                    completed++;
            }
            return ((float) completed / total) * 100f;
        });
    }

    public LiveData<List<Task>> getPendingTasks() {
        return taskDao.getPendingTasks();
    }

    public LiveData<List<Task>> getTasksForDateRange(long start, long end) {
        return taskDao.getTasksForDateRange(start, end);
    }

    public LiveData<List<Task>> getCompletedTasksBetween(long start, long end) {
        return taskDao.getCompletedTasksBetween(start, end);
    }

    public LiveData<List<Task>> getAllCompletedTasks() {
        return taskDao.getAllCompletedTasks();
    }

    public void insert(Task task) {
        executorService.execute(() -> {
            long id = taskDao.insert(task);
            task.id = id;
        });
    }

    public void update(Task task) {
        executorService.execute(() -> taskDao.update(task));
    }

    public void delete(Task task) {
        executorService.execute(() -> taskDao.delete(task));
    }

    // Habit methods
    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
    }

    public void insertHabit(Habit habit) {
        executorService.execute(() -> habitDao.insert(habit));
    }

    public void updateHabit(Habit habit) {
        executorService.execute(() -> habitDao.update(habit));
    }

    public void deleteHabit(Habit habit) {
        executorService.execute(() -> habitDao.delete(habit));
    }

    public void checkAndSeedHabits() {
        executorService.execute(() -> {
            int count = habitDao.getHabitCount();
            if (count == 0) {
                // Seed default habits
                habitDao.insert(new Habit("Drink Water", 0, 0));
                habitDao.insert(new Habit("Exercise", 0, 0));
                habitDao.insert(new Habit("Read Book", 0, 0));
            }
        });
    }
}
