package com.example.dailybalance.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dailybalance.R;
import com.example.dailybalance.data.local.AppDatabase;
import com.example.dailybalance.data.local.entity.FoodItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FoodScannerActivity extends AppCompatActivity {

    private EditText editSearch;
    private RecyclerView recyclerFoods;
    private FoodAdapter adapter;
    private AppDatabase database;
    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private Bitmap capturedPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_scanner);

        // Setup toolbar with back button
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        database = AppDatabase.getInstance(this);
        
        editSearch = findViewById(R.id.editSearch);
        recyclerFoods = findViewById(R.id.recyclerFoods);
        findViewById(R.id.btnScanCamera).setOnClickListener(v -> openCamera());
        
        recyclerFoods.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoodAdapter(new ArrayList<>());
        recyclerFoods.setAdapter(adapter);

        // Seed database with common foods
        seedFoodDatabase();

        // Search functionality
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchFoods(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load all foods initially
        searchFoods("");
    }

    private void openCamera() {
        // Check camera permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                return;
            }
        }
        
        // Launch camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } else {
            android.widget.Toast.makeText(this, "No camera app found", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            // Get captured photo
            if (data != null && data.getExtras() != null) {
                capturedPhoto = (Bitmap) data.getExtras().get("data");
            }
            // Show food name input dialog
            showFoodNameInputDialog();
        }
        else if (requestCode == 999 && resultCode == RESULT_OK) {
            // Voice input result
            if (data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String spokenText = result.get(0);
                    android.widget.Toast.makeText(this, "Detected: " + spokenText, android.widget.Toast.LENGTH_SHORT).show();
                    searchAndShowFood(spokenText);
                }
            }
        }
    }

    private void showFoodNameInputDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("🔍 Detecting Food...");
        builder.setMessage("Speak or type the food name:");
        
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("e.g., Vada Pav, Samosa, Rice, etc.");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);
        
        builder.setPositiveButton("🎤 Speak", (dialog, which) -> {
            // Voice input
            startVoiceInput();
        });
        
        builder.setNeutralButton("Get Details", (dialog, which) -> {
            String foodName = input.getText().toString().trim();
            if (!foodName.isEmpty()) {
                searchAndShowFood(foodName);
            } else {
                android.widget.Toast.makeText(this, "Please enter food name", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        
        // Auto-focus keyboard
        input.requestFocus();
    }

    private void startVoiceInput() {
        Intent intent = new Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        intent.putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Say the food name...");
        
        try {
            startActivityForResult(intent, 999);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Voice input not supported", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void searchAndShowFood(String foodName) {
        new Thread(() -> {
            // First search in database
            List<FoodItem> results = database.foodItemDao().searchFoods(foodName);
            
            runOnUiThread(() -> {
                FoodItem foodToShow;
                
                if (results != null && !results.isEmpty()) {
                    // Found in database - use exact data
                    foodToShow = results.get(0);
                    android.widget.Toast.makeText(this, "✅ Found: " + foodToShow.name, android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    // Not in database - create with estimated nutrition
                    foodToShow = createEstimatedFood(foodName);
                    android.widget.Toast.makeText(this, "📊 Showing estimated nutrition for: " + foodName, android.widget.Toast.LENGTH_LONG).show();
                }
                
                // Show result
                showFoodResult(foodToShow);
            });
        }).start();
    }

    private FoodItem createEstimatedFood(String foodName) {
        // Smart estimation based on food name keywords
        float calories, protein, carbs, fats;
        String category = "snack";
        
        String lowerName = foodName.toLowerCase();
        
        // Fried/Street food
        if (lowerName.contains("vada") || lowerName.contains("pakora") || lowerName.contains("samosa") || 
            lowerName.contains("kachori") || lowerName.contains("puri")) {
            calories = 250 + new java.util.Random().nextInt(100); // 250-350
            protein = 5 + new java.util.Random().nextInt(5);      // 5-10g
            carbs = 30 + new java.util.Random().nextInt(15);      // 30-45g
            fats = 12 + new java.util.Random().nextInt(8);        // 12-20g
            category = "snack";
        }
        // Pav items
        else if (lowerName.contains("pav")) {
            calories = 300 + new java.util.Random().nextInt(100); // 300-400
            protein = 8 + new java.util.Random().nextInt(7);      // 8-15g
            carbs = 40 + new java.util.Random().nextInt(15);      // 40-55g
            fats = 10 + new java.util.Random().nextInt(8);        // 10-18g
            category = "snack";
        }
        // Sweets
        else if (lowerName.contains("jalebi") || lowerName.contains("gulab") || lowerName.contains("rasgulla") ||
                 lowerName.contains("ladoo") || lowerName.contains("barfi")) {
            calories = 200 + new java.util.Random().nextInt(150); // 200-350
            protein = 2 + new java.util.Random().nextInt(4);      // 2-6g
            carbs = 40 + new java.util.Random().nextInt(20);      // 40-60g
            fats = 8 + new java.util.Random().nextInt(10);        // 8-18g
            category = "snack";
        }
        // Chaat items
        else if (lowerName.contains("bhel") || lowerName.contains("sev") || lowerName.contains("chaat")) {
            calories = 150 + new java.util.Random().nextInt(100); // 150-250
            protein = 4 + new java.util.Random().nextInt(4);      // 4-8g
            carbs = 25 + new java.util.Random().nextInt(15);      // 25-40g
            fats = 5 + new java.util.Random().nextInt(8);         // 5-13g
            category = "snack";
        }
        // Biryani/Rice dishes
        else if (lowerName.contains("biryani") || lowerName.contains("pulao") || lowerName.contains("fried rice")) {
            calories = 200 + new java.util.Random().nextInt(100); // 200-300
            protein = 10 + new java.util.Random().nextInt(10);    // 10-20g
            carbs = 35 + new java.util.Random().nextInt(15);      // 35-50g
            fats = 8 + new java.util.Random().nextInt(7);         // 8-15g
            category = "non_veg";
        }
        // Default estimation
        else {
            calories = 150 + new java.util.Random().nextInt(150); // 150-300
            protein = 5 + new java.util.Random().nextInt(10);     // 5-15g
            carbs = 20 + new java.util.Random().nextInt(25);      // 20-45g
            fats = 5 + new java.util.Random().nextInt(10);        // 5-15g
        }
        
        return new FoodItem(foodName, 100, calories, protein, carbs, fats, category);
    }

    private void showFoodResult(FoodItem food) {
        Intent intent = new Intent(this, FoodScanResultActivity.class);
        intent.putExtra("foodName", food.name);
        intent.putExtra("servingSize", food.servingSize);
        intent.putExtra("calories", food.calories);
        intent.putExtra("protein", food.protein);
        intent.putExtra("carbs", food.carbs);
        intent.putExtra("fats", food.fats);
        intent.putExtra("category", food.category);
        if (capturedPhoto != null) {
            intent.putExtra("photo", capturedPhoto);
        }
        startActivity(intent);
    }

    private void showFoodDetails(String foodName) {
        new Thread(() -> {
            List<FoodItem> results = database.foodItemDao().searchFoods(foodName);
            runOnUiThread(() -> {
                if (results != null && !results.isEmpty()) {
                    FoodItem food = results.get(0);
                    // Launch result activity with photo and details
                    Intent intent = new Intent(this, FoodScanResultActivity.class);
                    intent.putExtra("foodName", food.name);
                    intent.putExtra("servingSize", food.servingSize);
                    intent.putExtra("calories", food.calories);
                    intent.putExtra("protein", food.protein);
                    intent.putExtra("carbs", food.carbs);
                    intent.putExtra("fats", food.fats);
                    intent.putExtra("category", food.category);
                    if (capturedPhoto != null) {
                        intent.putExtra("photo", capturedPhoto);
                    }
                    startActivity(intent);
                } else {
                    android.widget.Toast.makeText(this, "Food not found in database", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void showDetailedFoodDialog(FoodItem food) {
        String details = String.format(
            "📊 NUTRITION INFORMATION\n\n" +
            "🍽️ Food: %s\n" +
            "⚖️ Serving Size: %.0fg\n\n" +
            "MACROS:\n" +
            "🔥 Calories: %.0f kcal\n" +
            "💪 Protein: %.1fg\n" +
            "🌾 Carbs: %.1fg\n" +
            "🥑 Fats: %.1fg\n\n" +
            "Category: %s",
            food.name,
            food.servingSize,
            food.calories,
            food.protein,
            food.carbs,
            food.fats,
            getCategoryName(food.category)
        );

        new android.app.AlertDialog.Builder(this)
            .setTitle("✅ Scanned Food Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .setNeutralButton("Search More", (dialog, which) -> {
                editSearch.setText(food.name);
                searchFoods(food.name);
            })
            .show();
    }

    private String getCategoryName(String category) {
        switch (category) {
            case "veg": return "🌱 Vegetarian";
            case "non_veg": return "🍗 Non-Vegetarian";
            case "fruit": return "🍎 Fruit";
            case "snack": return "🍪 Snack";
            default: return category;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                android.widget.Toast.makeText(this, "Camera permission required for scanning", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void seedFoodDatabase() {
        new Thread(() -> {
            int count = database.foodItemDao().getFoodCount();
            if (count == 0) {
                List<FoodItem> foods = Arrays.asList(
                    // Indian Veg Foods
                    new FoodItem("Rice (Cooked)", 100, 130, 2.7f, 28f, 0.3f, "veg"),
                    new FoodItem("Roti (Wheat)", 40, 104, 3.1f, 18f, 3.7f, "veg"),
                    new FoodItem("Dal (Cooked)", 100, 116, 9f, 20f, 0.5f, "veg"),
                    new FoodItem("Paneer", 100, 265, 18f, 1.2f, 20f, "veg"),
                    new FoodItem("Tofu", 100, 76, 8f, 1.9f, 4.8f, "veg"),
                    new FoodItem("Potato", 100, 77, 2f, 17f, 0.1f, "veg"),
                    new FoodItem("Sweet Potato", 100, 86, 1.6f, 20f, 0.1f, "veg"),
                    new FoodItem("Oats", 100, 389, 16.9f, 66f, 6.9f, "veg"),
                    new FoodItem("Almonds", 28, 164, 6f, 6f, 14f, "veg"),
                    new FoodItem("Peanut Butter", 32, 188, 8f, 7f, 16f, "veg"),
                    new FoodItem("Rajma", 100, 127, 8.7f, 22.8f, 0.5f, "veg"),
                    new FoodItem("Chole", 100, 164, 8.9f, 27.4f, 2.6f, "veg"),
                    new FoodItem("Aloo Paratha", 100, 250, 5f, 35f, 10f, "veg"),
                    new FoodItem("Idli", 50, 58, 2f, 12f, 0.1f, "veg"),
                    new FoodItem("Dosa", 100, 168, 3.9f, 28f, 4f, "veg"),
                    new FoodItem("Upma", 100, 150, 3.5f, 25f, 4f, "veg"),
                    new FoodItem("Poha", 100, 130, 2.6f, 23f, 3.5f, "veg"),
                    new FoodItem("Samosa", 100, 262, 5.4f, 28f, 14f, "snack"),
                    new FoodItem("Pakora", 100, 250, 6f, 25f, 13f, "snack"),
                    
                    // Non-Veg Foods
                    new FoodItem("Chicken Breast", 100, 165, 31f, 0f, 3.6f, "non_veg"),
                    new FoodItem("Chicken Curry", 100, 180, 18f, 5f, 10f, "non_veg"),
                    new FoodItem("Egg (Whole)", 50, 78, 6.3f, 0.6f, 5.3f, "non_veg"),
                    new FoodItem("Egg White", 33, 17, 3.6f, 0.2f, 0.1f, "non_veg"),
                    new FoodItem("Boiled Egg", 50, 78, 6.3f, 0.6f, 5.3f, "non_veg"),
                    new FoodItem("Omelette", 100, 154, 10.6f, 1.2f, 11.7f, "non_veg"),
                    new FoodItem("Fish (Salmon)", 100, 208, 20f, 0f, 13f, "non_veg"),
                    new FoodItem("Fish Curry", 100, 150, 15f, 3f, 8f, "non_veg"),
                    new FoodItem("Tuna", 100, 132, 28f, 0f, 1.3f, "non_veg"),
                    new FoodItem("Mutton", 100, 294, 25f, 0f, 21f, "non_veg"),
                    new FoodItem("Mutton Curry", 100, 250, 20f, 5f, 17f, "non_veg"),
                    new FoodItem("Prawns", 100, 99, 24f, 0.2f, 0.3f, "non_veg"),
                    new FoodItem("Chicken Biryani", 100, 170, 8f, 20f, 6f, "non_veg"),
                    
                    // Fruits
                    new FoodItem("Banana", 100, 89, 1.1f, 23f, 0.3f, "fruit"),
                    new FoodItem("Apple", 100, 52, 0.3f, 14f, 0.2f, "fruit"),
                    new FoodItem("Orange", 100, 47, 0.9f, 12f, 0.1f, "fruit"),
                    new FoodItem("Mango", 100, 60, 0.8f, 15f, 0.4f, "fruit"),
                    new FoodItem("Berries", 100, 57, 0.7f, 14f, 0.3f, "fruit"),
                    new FoodItem("Watermelon", 100, 30, 0.6f, 8f, 0.2f, "fruit"),
                    new FoodItem("Papaya", 100, 43, 0.5f, 11f, 0.3f, "fruit"),
                    new FoodItem("Grapes", 100, 69, 0.7f, 18f, 0.2f, "fruit"),
                    new FoodItem("Pomegranate", 100, 83, 1.7f, 19f, 1.2f, "fruit"),
                    new FoodItem("Guava", 100, 68, 2.6f, 14f, 1f, "fruit"),
                    
                    // Snacks & Others
                    new FoodItem("Bread (White)", 28, 75, 2.5f, 14f, 1f, "snack"),
                    new FoodItem("Brown Bread", 28, 69, 3.6f, 12f, 0.9f, "snack"),
                    new FoodItem("Pasta (Cooked)", 100, 131, 5f, 25f, 1.1f, "snack"),
                    new FoodItem("Quinoa", 100, 120, 4.4f, 21f, 1.9f, "snack"),
                    new FoodItem("Brown Rice", 100, 111, 2.6f, 23f, 0.9f, "snack"),
                    new FoodItem("Greek Yogurt", 100, 59, 10f, 3.6f, 0.4f, "snack"),
                    new FoodItem("Milk", 100, 42, 3.4f, 5f, 1f, "snack"),
                    new FoodItem("Cheese", 28, 113, 7f, 0.4f, 9f, "snack"),
                    new FoodItem("Butter", 14, 102, 0.1f, 0f, 11.5f, "snack"),
                    new FoodItem("Ghee", 14, 112, 0f, 0f, 12.7f, "snack"),
                    new FoodItem("Pizza", 100, 266, 11f, 33f, 10f, "snack"),
                    new FoodItem("Burger", 100, 295, 17f, 24f, 14f, "snack"),
                    new FoodItem("French Fries", 100, 312, 3.4f, 41f, 15f, "snack"),
                    new FoodItem("Chocolate", 100, 546, 4.9f, 61f, 31f, "snack"),
                    new FoodItem("Ice Cream", 100, 207, 3.5f, 24f, 11f, "snack")
                );
                database.foodItemDao().insertAll(foods);
            }
        }).start();
    }

    private void searchFoods(String query) {
        new Thread(() -> {
            List<FoodItem> results = database.foodItemDao().searchFoods(query);
            runOnUiThread(() -> adapter.updateFoods(results));
        }).start();
    }

    // Adapter for food items
    class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
        private List<FoodItem> foods;

        FoodAdapter(List<FoodItem> foods) {
            this.foods = foods;
        }

        void updateFoods(List<FoodItem> newFoods) {
            this.foods = newFoods;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_food, parent, false);
            return new FoodViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
            FoodItem food = foods.get(position);
            holder.bind(food);
        }

        @Override
        public int getItemCount() {
            return foods.size();
        }

        class FoodViewHolder extends RecyclerView.ViewHolder {
            TextView textName, textServing, textCalories, textProtein, textCarbs, textFats;

            FoodViewHolder(@NonNull View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.textFoodName);
                textServing = itemView.findViewById(R.id.textServing);
                textCalories = itemView.findViewById(R.id.textCalories);
                textProtein = itemView.findViewById(R.id.textProtein);
                textCarbs = itemView.findViewById(R.id.textCarbs);
                textFats = itemView.findViewById(R.id.textFats);
            }

            void bind(FoodItem food) {
                textName.setText(food.name);
                textServing.setText(String.format("Serving: %.0fg", food.servingSize));
                textCalories.setText(String.format("%.0f cal", food.calories));
                textProtein.setText(String.format("P: %.1fg", food.protein));
                textCarbs.setText(String.format("C: %.1fg", food.carbs));
                textFats.setText(String.format("F: %.1fg", food.fats));
                
                // Click to show result screen with full details
                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(FoodScannerActivity.this, FoodScanResultActivity.class);
                    intent.putExtra("foodName", food.name);
                    intent.putExtra("servingSize", food.servingSize);
                    intent.putExtra("calories", food.calories);
                    intent.putExtra("protein", food.protein);
                    intent.putExtra("carbs", food.carbs);
                    intent.putExtra("fats", food.fats);
                    intent.putExtra("category", food.category);
                    // No photo for manual selection
                    startActivity(intent);
                });
            }
        }
    }
}
