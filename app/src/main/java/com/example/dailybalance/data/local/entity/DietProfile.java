package com.example.dailybalance.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diet_profile")
public class DietProfile {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public float weight; // in kg
    public float height; // in cm
    public String goal; // "bulk" or "loss"
    public int age;
    public String gender; // "male" or "female"
    public String activityLevel; // "sedentary", "light", "moderate", "active", "very_active"
    public String dietType; // "veg" or "non_veg"
    public String workoutTime; // "morning", "afternoon", "evening"
    public float targetCalories;
    public float targetProtein;
    public float targetCarbs;
    public float targetFats;
    public long createdDate;

    public DietProfile(float weight, float height, String goal, int age, String gender, String activityLevel, String dietType, String workoutTime) {
        this.weight = weight;
        this.height = height;
        this.goal = goal;
        this.age = age;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.dietType = dietType;
        this.workoutTime = workoutTime;
        this.createdDate = System.currentTimeMillis();
        calculateMacros();
    }

    public void calculateMacros() {
        // Calculate BMR using Mifflin-St Jeor Equation
        float bmr;
        if (gender.equals("male")) {
            bmr = (10 * weight) + (6.25f * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25f * height) - (5 * age) - 161;
        }

        // Calculate TDEE based on activity level
        float activityMultiplier = 1.2f; // sedentary
        switch (activityLevel) {
            case "light": activityMultiplier = 1.375f; break;
            case "moderate": activityMultiplier = 1.55f; break;
            case "active": activityMultiplier = 1.725f; break;
            case "very_active": activityMultiplier = 1.9f; break;
        }
        float tdee = bmr * activityMultiplier;

        // Adjust calories based on goal
        if (goal.equals("bulk")) {
            targetCalories = tdee + 300; // Surplus for muscle gain
            targetProtein = weight * 2.0f; // 2g per kg
            targetCarbs = (targetCalories - (targetProtein * 4) - (weight * 1.0f * 9)) / 4;
            targetFats = weight * 1.0f; // 1g per kg
        } else { // loss
            targetCalories = tdee - 500; // Deficit for fat loss
            targetProtein = weight * 2.2f; // Higher protein to preserve muscle
            targetFats = weight * 0.8f; // 0.8g per kg
            targetCarbs = (targetCalories - (targetProtein * 4) - (targetFats * 9)) / 4;
        }
    }
}
