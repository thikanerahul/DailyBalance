package com.example.dailybalance.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_items")
public class FoodItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public float servingSize; // in grams
    public float calories;
    public float protein;
    public float carbs;
    public float fats;
    public String category; // "veg", "non_veg", "fruit", "snack"

    public FoodItem(String name, float servingSize, float calories, float protein, float carbs, float fats, String category) {
        this.name = name;
        this.servingSize = servingSize;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
        this.category = category;
    }
}
