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
        String goal = ((RadioButton) findViewById(goalId)).getText().toString().toLowerCase().contains("bulk") ? "bulk" : "loss";
        String activity = getActivityLevel(activityId);
        String dietType = ((RadioButton) findViewById(dietTypeId)).getText().toString().toLowerCase().contains("veg") ? "veg" : "non_veg";
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
        if (text.contains("sedentary")) return "sedentary";
        if (text.contains("light")) return "light";
        if (text.contains("moderate")) return "moderate";
        if (text.contains("active") && !text.contains("very")) return "active";
        return "very_active";
    }

    private String getWorkoutTime(int workoutTimeId) {
        RadioButton rb = findViewById(workoutTimeId);
        String text = rb.getText().toString().toLowerCase();
        if (text.contains("morning")) return "morning";
        if (text.contains("afternoon")) return "afternoon";
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
        String proteinSource = isVeg ? "Paneer/Tofu" : "Chicken/Fish";
        String meatOption = isVeg ? "Soya Chunks" : "Lean Meat";
        
        if (profile.goal.equals("bulk")) {
            plan.append("🏋️ MUSCLE GAIN DIET PLAN\n");
            plan.append(isVeg ? "🌱 VEGETARIAN\n\n" : "🍗 NON-VEGETARIAN\n\n");
            
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
            } else { // evening
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
            
        } else { // Fat Loss
            plan.append("🔥 FAT LOSS DIET PLAN\n");
            plan.append(isVeg ? "🌱 VEGETARIAN\n\n" : "🍗 NON-VEGETARIAN\n\n");
            
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
            
            // Workout time specific meals
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
            } else { // evening
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
