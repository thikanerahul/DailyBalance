package com.example.dailybalance.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dailybalance.R;

public class FoodScanResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_scan_result);

        // Setup toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get data from intent
        String foodName = getIntent().getStringExtra("foodName");
        float servingSize = getIntent().getFloatExtra("servingSize", 0);
        float calories = getIntent().getFloatExtra("calories", 0);
        float protein = getIntent().getFloatExtra("protein", 0);
        float carbs = getIntent().getFloatExtra("carbs", 0);
        float fats = getIntent().getFloatExtra("fats", 0);
        String category = getIntent().getStringExtra("category");
        Bitmap photo = getIntent().getParcelableExtra("photo");

        // Display data
        ImageView imageScanned = findViewById(R.id.imageScanned);
        TextView textFoodName = findViewById(R.id.textFoodName);
        TextView textCategory = findViewById(R.id.textCategory);
        TextView textServing = findViewById(R.id.textServing);
        TextView textCalories = findViewById(R.id.textCalories);
        TextView textProtein = findViewById(R.id.textProtein);
        TextView textCarbs = findViewById(R.id.textCarbs);
        TextView textFats = findViewById(R.id.textFats);
        Button btnScanAnother = findViewById(R.id.btnScanAnother);
        Button btnClose = findViewById(R.id.btnClose);

        // Set photo if available
        if (photo != null) {
            imageScanned.setImageBitmap(photo);
        }

        textFoodName.setText(foodName);
        textCategory.setText(getCategoryName(category));
        textServing.setText(String.format("%.0fg", servingSize));
        textCalories.setText(String.format("%.0f kcal", calories));
        textProtein.setText(String.format("%.1fg", protein));
        textCarbs.setText(String.format("%.1fg", carbs));
        textFats.setText(String.format("%.1fg", fats));

        btnScanAnother.setOnClickListener(v -> {
            finish();
        });

        btnClose.setOnClickListener(v -> {
            finish();
        });
    }

    private String getCategoryName(String category) {
        if (category == null) return "";
        switch (category) {
            case "veg": return "🌱 Vegetarian";
            case "non_veg": return "🍗 Non-Vegetarian";
            case "fruit": return "🍎 Fruit";
            case "snack": return "🍪 Snack";
            default: return category;
        }
    }
}
