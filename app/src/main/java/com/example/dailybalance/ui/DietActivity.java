package com.example.dailybalance.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dailybalance.R;
import com.example.dailybalance.data.local.AppDatabase;
import com.example.dailybalance.data.local.entity.DietProfile;

public class DietActivity extends AppCompatActivity {

    private EditText editWeight, editHeight, editAge;
    private RadioGroup radioGender, radioGoal, radioActivity, radioDietType, radioWorkoutTime;
    private Button btnCalculate;
    private View layoutResults;
    private TextView textCalories, textProtein, textCarbs, textFats, textBMI, textDietPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet);

        editWeight = findViewById(R.id.editWeight);
        editHeight = findViewById(R.id.editHeight);
        editAge = findViewById(R.id.editAge);
        radioGender = findViewById(R.id.radioGender);
        radioGoal = findViewById(R.id.radioGoal);
        radioActivity = findViewById(R.id.radioActivity);
        radioDietType = findViewById(R.id.radioDietType);
        radioWorkoutTime = findViewById(R.id.radioWorkoutTime);
        btnCalculate = findViewById(R.id.btnCalculate);
        layoutResults = findViewById(R.id.layoutResults);
        textCalories = findViewById(R.id.textCalories);
        textProtein = findViewById(R.id.textProtein);
        textCarbs = findViewById(R.id.textCarbs);
        textFats = findViewById(R.id.textFats);
        textBMI = findViewById(R.id.textBMI);
        textDietPlan = findViewById(R.id.textDietPlan);

        // Load existing profile if available
        loadExistingProfile();

        btnCalculate.setOnClickListener(v -> calculateDiet());
    }

    private void loadExistingProfile() {
        new Thread(() -> {
            DietProfile profile = AppDatabase.getInstance(this).dietProfileDao().getCurrentProfileSync();
            if (profile != null) {
                runOnUiThread(() -> {
                    editWeight.setText(String.valueOf(profile.weight));
                    editHeight.setText(String.valueOf(profile.height));
                    editAge.setText(String.valueOf(profile.age));
                    displayResults(profile);
                });
            }
        }).start();
    }

    private void calculateDiet() {
        String weightStr = editWeight.getText().toString();
        String heightStr = editHeight.getText().toString();
        String ageStr = editAge.getText().toString();

        if (weightStr.isEmpty() || heightStr.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        float weight = Float.parseFloat(weightStr);
        float height = Float.parseFloat(heightStr);
        int age = Integer.parseInt(ageStr);

        int genderId = radioGender.getCheckedRadioButtonId();
        int goalId = radioGoal.getCheckedRadioButtonId();
        int activityId = radioActivity.getCheckedRadioButtonId();
        int dietTypeId = radioDietType.getCheckedRadioButtonId();
        int workoutTimeId = radioWorkoutTime.getCheckedRadioButtonId();

        if (genderId == -1 || goalId == -1 || activityId == -1 || dietTypeId == -1 || workoutTimeId == -1) {
            Toast.makeText(this, "Please select all options", Toast.LENGTH_SHORT).show();
            return;
        }

        String gender = ((RadioButton) findViewById(genderId)).getText().toString().toLowerCase();
        String goal = ((RadioButton) findViewById(goalId)).getText().toString().toLowerCase().contains("bulk") ? "bulk"
                : "loss";
        String activity = getActivityLevel(activityId);
        String dietType = ((RadioButton) findViewById(dietTypeId)).getText().toString().toLowerCase().contains("veg")
                ? "veg"
                : "non_veg";
        String workoutTime = getWorkoutTime(workoutTimeId);

        DietProfile profile = new DietProfile(weight, height, goal, age, gender, activity, dietType, workoutTime);

        // Save to database
        new Thread(() -> {
            AppDatabase.getInstance(this).dietProfileDao().insert(profile);
        }).start();

        // Navigate to result activity
        android.content.Intent intent = new android.content.Intent(this, DietResultActivity.class);
        intent.putExtra("weight", weight);
        intent.putExtra("height", height);
        intent.putExtra("age", age);
        intent.putExtra("gender", gender);
        intent.putExtra("goal", goal);
        intent.putExtra("activity", activity);
        intent.putExtra("dietType", dietType);
        intent.putExtra("workoutTime", workoutTime);
        startActivity(intent);
    }

    private String getActivityLevel(int activityId) {
        RadioButton rb = findViewById(activityId);
        String text = rb.getText().toString().toLowerCase();
        if (text.contains("sedentary"))
            return "sedentary";
        if (text.contains("light"))
            return "light";
        if (text.contains("moderate"))
            return "moderate";
        if (text.contains("active") && !text.contains("very"))
            return "active";
        return "very_active";
    }

    private String getWorkoutTime(int workoutTimeId) {
        RadioButton rb = findViewById(workoutTimeId);
        String text = rb.getText().toString().toLowerCase();
        if (text.contains("morning"))
            return "morning";
        if (text.contains("afternoon"))
            return "afternoon";
        return "evening";
    }

    private void displayResults(DietProfile profile) {
        layoutResults.setVisibility(View.VISIBLE);

        // Calculate BMI
        float bmi = profile.weight / ((profile.height / 100) * (profile.height / 100));

        textCalories.setText(String.format("%.0f kcal/day", profile.targetCalories));
        textProtein.setText(String.format("%.0f g/day", profile.targetProtein));
        textCarbs.setText(String.format("%.0f g/day", profile.targetCarbs));
        textFats.setText(String.format("%.0f g/day", profile.targetFats));
        textBMI.setText(String.format("BMI: %.1f", bmi));

        // Generate diet plan
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
