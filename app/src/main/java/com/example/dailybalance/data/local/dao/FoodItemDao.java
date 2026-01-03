package com.example.dailybalance.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.dailybalance.data.local.entity.FoodItem;
import java.util.List;

@Dao
public interface FoodItemDao {

    @Query("SELECT * FROM food_items ORDER BY name ASC")
    LiveData<List<FoodItem>> getAllFoods();

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    List<FoodItem> searchFoods(String query);

    @Query("SELECT * FROM food_items WHERE category = :category ORDER BY name ASC")
    List<FoodItem> getFoodsByCategory(String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FoodItem foodItem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FoodItem> foodItems);

    @Query("SELECT COUNT(*) FROM food_items")
    int getFoodCount();

    @Query("DELETE FROM food_items")
    void deleteAll();
}
