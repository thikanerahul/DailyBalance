package com.example.dailybalance.data.local;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.dailybalance.data.local.dao.HabitDao;
import com.example.dailybalance.data.local.dao.TaskDao;
import com.example.dailybalance.data.local.dao.DietProfileDao;
import com.example.dailybalance.data.local.dao.FoodItemDao;
import com.example.dailybalance.data.local.entity.Habit;
import com.example.dailybalance.data.local.entity.Task;
import com.example.dailybalance.data.local.entity.DietProfile;
import com.example.dailybalance.data.local.entity.FoodItem;

@Database(entities = { Task.class, Habit.class, DietProfile.class, FoodItem.class }, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract TaskDao taskDao();

    public abstract HabitDao habitDao();

    public abstract DietProfileDao dietProfileDao();

    public abstract FoodItemDao foodItemDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "daily_balance_db_v7") // New database name
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
