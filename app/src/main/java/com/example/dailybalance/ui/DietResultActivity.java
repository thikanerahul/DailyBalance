package com.example.dailybalance.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dailybalance.R;
import com.example.dailybalance.data.local.entity.DietProfile;

public class DietResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet_result);

        // Get data from intent
        float weight = getIntent().getFloatExtra("weight", 0);
        float height = getIntent().getFloatExtra("height", 0);
        int age = getIntent().getIntExtra("age", 0);
        String gender = getIntent().getStringExtra("gender");
        String goal = getIntent().getStringExtra("goal");
        String activity = getIntent().getStringExtra("activity");
        String dietType = getIntent().getStringExtra("dietType");
        String workoutTime = getIntent().getStringExtra("workoutTime");

        // Create profile and calculate
        DietProfile profile = new DietProfile(weight, height, goal, age, gender, activity, dietType, workoutTime);

        // Display results
        displayResults(profile);
    }

    private void displayResults(DietProfile profile) {
        TextView textBMI = findViewById(R.id.textBMI);
        TextView textCalories = findViewById(R.id.textCalories);
        TextView textProtein = findViewById(R.id.textProtein);
        TextView textCarbs = findViewById(R.id.textCarbs);
        TextView textFats = findViewById(R.id.textFats);
        TextView textDietPlan = findViewById(R.id.textDietPlan);
        TextView textGoalTitle = findViewById(R.id.textGoalTitle);

        // Calculate BMI
        float bmi = profile.weight / ((profile.height / 100) * (profile.height / 100));
        
        textBMI.setText(String.format("BMI: %.1f", bmi));
        textCalories.setText(String.format("%.0f kcal/day", profile.targetCalories));
        textProtein.setText(String.format("%.0f g", profile.targetProtein));
        textCarbs.setText(String.format("%.0f g", profile.targetCarbs));
        textFats.setText(String.format("%.0f g", profile.targetFats));
        
        // Set goal title
        if (profile.goal.equals("bulk")) {
            textGoalTitle.setText("🏋️ MUSCLE GAIN PLAN");
        } else {
            textGoalTitle.setText("🔥 FAT LOSS PLAN");
        }

        // Generate and display diet plan
        String dietPlan = generateDietPlan(profile);
        textDietPlan.setText(dietPlan);
    }

    private String generateDietPlan(DietProfile profile) {
        StringBuilder plan = new StringBuilder();
        boolean isVeg = profile.dietType.equals("veg");
        
        plan.append(isVeg ? "🌱 VEGETARIAN DIET\n\n" : "🍗 NON-VEGETARIAN DIET\n\n");
        
        if (profile.goal.equals("bulk")) {
            plan.append("BREAKFAST (7-8 AM):\n");
            if (isVeg) {
                plan.append("• 4 Whole Eggs + 2 Egg Whites\n");
                plan.append("• 2 Slices Whole Wheat Bread\n");
                plan.append("• 1 Banana\n");
                plan.append("• Protein Shake\n\n");
            } else {
                plan.append("• 4 Whole Eggs + 2 Egg Whites\n");
                plan.append("• 2 Slices Whole Wheat Bread\n");
                plan.append("• 1 Banana\n");
                plan.append("• Protein Shake\n\n");
            }
            
            plan.append("MID-MORNING SNACK (10-11 AM):\n");
            plan.append("• Handful of Almonds\n");
            plan.append("• 1 Apple\n");
            if (isVeg) plan.append("• Peanut Butter\n");
            plan.append("\n");
            
            plan.append("LUNCH (1-2 PM):\n");
            if (isVeg) {
                plan.append("• 200g Paneer/Tofu\n");
                plan.append("• 2 Cups Rice/Roti\n");
                plan.append("• Dal (Lentils)\n");
            } else {
                plan.append("• 200g Chicken/Fish\n");
                plan.append("• 2 Cups Rice/Pasta\n");
                plan.append("• Egg Curry\n");
            }
            plan.append("• Mixed Vegetables\n");
            plan.append("• Salad\n\n");
            
            // Workout time specific meals
            if (profile.workoutTime.equals("morning")) {
                plan.append("PRE-WORKOUT (6-7 AM):\n");
                plan.append("• Banana + Peanut Butter\n");
                plan.append("• Black Coffee\n\n");
                
                plan.append("POST-WORKOUT (9-10 AM):\n");
                plan.append("• Protein Shake\n");
                plan.append("• Sweet Potato\n\n");
            } else if (profile.workoutTime.equals("afternoon")) {
                plan.append("PRE-WORKOUT (11-12 PM):\n");
                plan.append("• Banana + Dates\n");
                plan.append("• Black Coffee\n\n");
                
                plan.append("POST-WORKOUT (3-4 PM):\n");
                plan.append("• Protein Shake\n");
                plan.append("• Brown Rice\n\n");
            } else {
                plan.append("PRE-WORKOUT (4-5 PM):\n");
                plan.append("• Banana + Peanut Butter\n");
                plan.append("• Black Coffee\n\n");
                
                plan.append("POST-WORKOUT (6-7 PM):\n");
                plan.append("• Protein Shake\n");
                plan.append("• Sweet Potato\n\n");
            }
            
            plan.append("DINNER (8-9 PM):\n");
            if (isVeg) {
                plan.append("• 200g Paneer/Soya\n");
                plan.append("• 1.5 Cups Rice/Roti\n");
            } else {
                plan.append("• 200g Chicken/Fish\n");
                plan.append("• 1.5 Cups Rice\n");
            }
            plan.append("• Vegetables\n\n");
            
            plan.append("BEFORE BED:\n");
            plan.append(isVeg ? "• Casein Protein/Paneer\n" : "• Casein Protein/Greek Yogurt\n");
            
        } else {
            plan.append("BREAKFAST (7-8 AM):\n");
            plan.append("• 3 Egg Whites + 1 Whole Egg\n");
            plan.append("• Oatmeal (1/2 cup)\n");
            plan.append("• Green Tea\n\n");
            
            plan.append("MID-MORNING SNACK (10-11 AM):\n");
            if (isVeg) {
                plan.append("• Sprouts\n");
                plan.append("• Berries\n\n");
            } else {
                plan.append("• Greek Yogurt\n");
                plan.append("• Berries\n\n");
            }
            
            plan.append("LUNCH (1-2 PM):\n");
            if (isVeg) {
                plan.append("• 150g Paneer/Tofu\n");
                plan.append("• 1 Cup Brown Rice\n");
                plan.append("• Dal\n");
            } else {
                plan.append("• 150g Grilled Chicken/Fish\n");
                plan.append("• 1 Cup Brown Rice\n");
                plan.append("• Boiled Eggs\n");
            }
            plan.append("• Large Salad\n");
            plan.append("• Vegetables\n\n");
            
            if (profile.workoutTime.equals("morning")) {
                plan.append("PRE-WORKOUT (6-7 AM):\n");
                plan.append("• Black Coffee\n");
                plan.append("• 5 Almonds\n\n");
                
                plan.append("POST-WORKOUT (9-10 AM):\n");
                plan.append("• Protein Shake\n\n");
            } else if (profile.workoutTime.equals("afternoon")) {
                plan.append("AFTERNOON SNACK (3-4 PM):\n");
                plan.append("• Protein Shake\n");
                plan.append("• 10 Almonds\n\n");
            } else {
                plan.append("AFTERNOON SNACK (4-5 PM):\n");
                plan.append("• Protein Shake\n");
                plan.append("• 10 Almonds\n\n");
            }
            
            plan.append("DINNER (7-8 PM):\n");
            if (isVeg) {
                plan.append("• 150g Paneer/Tofu\n");
                plan.append("• Lots of Vegetables\n");
                plan.append("• Small portion Quinoa\n\n");
            } else {
                plan.append("• 150g Grilled Chicken/Fish\n");
                plan.append("• Lots of Vegetables\n");
                plan.append("• Small portion Quinoa\n\n");
            }
            
            plan.append("TIPS:\n");
            plan.append("• Drink 3-4L water daily\n");
            plan.append("• Avoid sugar & processed foods\n");
            plan.append("• Sleep 7-8 hours\n");
            if (isVeg) {
                plan.append("• Take B12 supplement\n");
                plan.append("• Include variety of protein sources\n");
            }
        }
        
        return plan.toString();
    }
}
