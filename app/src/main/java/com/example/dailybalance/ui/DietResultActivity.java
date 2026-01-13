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

        // Calculate scale factor based on standard 2000kcal diet
        // If target is 3000, portions will be 1.5x
        float scale = profile.targetCalories / 2000.0f;
        if (scale < 0.6f)
            scale = 0.6f; // Minimum portion limit

        // Helper to format quantities
        String meatQty = String.format("%.0fg", 200 * scale);
        String paneerQty = String.format("%.0fg", 200 * scale);
        String riceQty = String.format("%.1f cups", 2 * scale);
        String riceSmallQty = String.format("%.1f cups", 1.5 * scale);
        String oatsQty = String.format("%.1f cup", 0.5 * scale);
        String eggQty = String.format("%.0f", 6 * scale); // 4 whole + 2 whites approx

        plan.append(isVeg ? "🌱 VEGETARIAN DIET\n" : "🍗 NON-VEGETARIAN DIET\n");
        plan.append(String.format("Daily Target: %.0f kcal\n\n", profile.targetCalories));

        if (profile.goal.equals("bulk")) {
            plan.append("🏋️ MUSCLE GAIN PLAN\n\n");

            plan.append("BREAKFAST (7-8 AM):\n");
            if (isVeg) {
                plan.append("• Paneer Bhurji (" + paneerQty + ")\n");
                plan.append("• 2 Slices Whole Wheat Bread\n");
                plan.append("• 1 Banana\n");
                plan.append("• 1 Scoop Whey Protein / Large Glass Milk\n\n");
            } else {
                plan.append("• " + eggQty + " Eggs (Scrambled/Boiled)\n");
                plan.append("• 2 Slices Whole Wheat Bread\n");
                plan.append("• 1 Banana\n");
                plan.append("• 1 Scoop Whey Protein\n\n");
            }

            plan.append("MID-MORNING SNACK (10-11 AM):\n");
            plan.append("• Handful of Mixed Nuts (Almonds/Walnuts)\n");
            plan.append("• 1 Apple/Pear\n");
            if (isVeg)
                plan.append("• 1 tbsp Peanut Butter\n");
            plan.append("\n");

            plan.append("LUNCH (1-2 PM):\n");
            if (isVeg) {
                plan.append("• " + paneerQty + " Paneer/Tofu Curry\n");
                plan.append("• " + riceQty + " Rice / 3 Chapatis\n");
                plan.append("• 1 Bowl Yellow Dal\n");
            } else {
                plan.append("• " + meatQty + " Chicken Breast/Fish Curry\n");
                plan.append("• " + riceQty + " Rice / 3 Chapatis\n");
                plan.append("• 1 Bowl Dal/Legumes\n");
            }
            plan.append("• Mixed Vegetable Sabzi\n");
            plan.append("• Green Salad\n\n");

            // Workout meals
            String preWorkout = "• 1 Banana + Black Coffee\n• 1 Slice Bread + Peanut Butter\n\n";
            String postWorkout = "• 1 Scoop Whey Protein\n• 2 Boiled Potatoes / Sweet Potato\n\n"; // Removed meat
                                                                                                   // requirement from
                                                                                                   // immediate post
                                                                                                   // workout for
                                                                                                   // universal fit

            if (profile.workoutTime.equals("morning")) {
                plan.append("PRE-WORKOUT (6-7 AM):\n" + preWorkout);
                plan.append("POST-WORKOUT (9-10 AM):\n" + postWorkout);
            } else if (profile.workoutTime.equals("afternoon")) {
                plan.append("PRE-WORKOUT (3-4 PM):\n" + preWorkout);
                plan.append("POST-WORKOUT (5-6 PM):\n" + postWorkout);
            } else {
                plan.append("PRE-WORKOUT (5-6 PM):\n" + preWorkout);
                plan.append("POST-WORKOUT (7-8 PM):\n" + postWorkout);
            }

            plan.append("DINNER (8-9 PM):\n");
            if (isVeg) {
                plan.append("• Soya Chunks/Paneer (" + paneerQty + ")\n");
                plan.append("• " + riceSmallQty + " Rice / 2 Chapatis\n");
            } else {
                plan.append("• " + meatQty + " Grilled Chicken/Fish\n");
                plan.append("• " + riceSmallQty + " Rice / 2 Chapatis\n");
            }
            plan.append("• Green Salad\n\n");

            plan.append("BEFORE BED:\n");
            plan.append(isVeg ? "• 1 Glass Warm Milk with Turmeric\n" : "• Casein Protein / 1 Glass Milk\n");

        } else { // FAT LOSS
            plan.append("🔥 FAT LOSS PLAN\n\n");

            plan.append("BREAKFAST (7-8 AM):\n");
            if (isVeg) {
                plan.append("• Moong Dal Chilla (2-3 pcs) with Mint Chutney\n");
                plan.append("• " + oatsQty + " Milk Oats (No Sugar)\n");
                plan.append("• Green Tea\n\n");
            } else {
                plan.append("• 3 Egg Whites + 1 Whole Egg Omelette\n");
                plan.append("• " + oatsQty + " Masala Oats\n");
                plan.append("• Green Tea\n\n");
            }

            plan.append("MID-MORNING SNACK (10-11 AM):\n");
            plan.append("• 1 Bowl Watermelon/Papaya\n");
            plan.append("• 5-6 Almonds\n\n");

            plan.append("LUNCH (1-2 PM):\n");
            if (isVeg) {
                plan.append("• " + paneerQty + " Paneer Tikka/Salad\n");
                plan.append("• 1 Cup Brown Rice / 1 Multigrain Roti\n");
                plan.append("• 1 Bowl Dal Tadka (Less Oil)\n");
            } else {
                plan.append("• " + meatQty + " Grilled Chicken Salad\n");
                plan.append("• 1 Cup Brown Rice / 1 Multigrain Roti\n");
            }
            plan.append("• Cucumber Raita\n\n");

            // Workout meals
            String preWorkout = "• 1 Apple + Black Coffee\n\n";
            String postWorkout = "• 1 Scoop Whey Protein in Water\n\n";

            if (profile.workoutTime.equals("morning")) {
                plan.append("PRE-WORKOUT (6-7 AM):\n" + preWorkout);
                plan.append("POST-WORKOUT (8-9 AM):\n" + postWorkout);
            } else if (profile.workoutTime.equals("afternoon")) {
                plan.append("PRE-WORKOUT (3-4 PM):\n" + preWorkout);
                plan.append("POST-WORKOUT (5-6 PM):\n" + postWorkout);
            } else {
                plan.append("PRE-WORKOUT (5-6 PM):\n" + preWorkout);
                plan.append("POST-WORKOUT (7-8 PM):\n" + postWorkout);
            }

            plan.append("DINNER (7-8 PM):\n");
            if (isVeg) {
                plan.append("• 1 Bowl Stir-fry Tofu/Mushrooms\n");
                plan.append("• Large Bowl Soup (Tomato/Spinach)\n");
                plan.append("• No Rice/Roti (Low Carb)\n\n");
            } else {
                plan.append("• " + meatQty + " Grilled Fish/Chicken\n");
                plan.append("• Large Bowl Clear Soup\n");
                plan.append("• Steamed Broccoli\n\n");
            }

            plan.append("TIPS:\n");
            plan.append("• Drink 4L Water Daily\n");
            plan.append("• 10k Steps Walk Daily\n");
            plan.append("• ZERO Sugar\n");
            plan.append("• Sleep 7-8 Hours\n");
        }

        return plan.toString();
    }
}
