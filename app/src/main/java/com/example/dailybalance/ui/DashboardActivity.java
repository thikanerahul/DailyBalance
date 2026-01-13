package com.example.dailybalance.ui;

import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dailybalance.R;
import com.example.dailybalance.ui.base.BaseActivity;
import com.example.dailybalance.ui.components.Custom3DProgressBar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class DashboardActivity extends BaseActivity {

    private DailyViewModel viewModel;
    private TaskAdapter adapter;
    private Custom3DProgressBar productivityRing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before super.onCreate
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Request Notification Permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[] {
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.SCHEDULE_EXACT_ALARM
            }, 101);
        }

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(DailyViewModel.class);
        setupHabits(); // Call the new setupHabits method

        findViewById(R.id.btnAnalytics).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, AnalyticsActivity.class));
        });

        // Diet button
        findViewById(R.id.btnDiet).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, DietActivity.class));
        });

        findViewById(R.id.btnCalendar).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, CalendarActivity.class));
        });

        productivityRing = findViewById(R.id.productivityRing);
        productivityRing.setProgress(70f); // Dummy initial value

        RecyclerView recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        viewModel.getAllTasks().observe(this, tasks -> {
            adapter.setTasks(tasks);
        });

        viewModel.getProductivityPercentage().observe(this, percent -> {
            productivityRing.setProgress(percent);
        });

        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onTaskCheckChanged(com.example.dailybalance.data.local.entity.Task task) {
                viewModel.update(task);
            }

            @Override
            public void onTaskLongClick(com.example.dailybalance.data.local.entity.Task task) {
                new android.app.AlertDialog.Builder(DashboardActivity.this)
                        .setTitle("Delete Task")
                        .setMessage("Delete " + task.title + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            viewModel.delete(task);
                            android.widget.Toast
                                    .makeText(DashboardActivity.this, "Task Deleted", android.widget.Toast.LENGTH_SHORT)
                                    .show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        ExtendedFloatingActionButton fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            AddTaskBottomSheet bottomSheet = new AddTaskBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "AddTaskBottomSheet");
        });
        // Initialize Drawer
        androidx.drawerlayout.widget.DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        com.google.android.material.navigation.NavigationView navView = findViewById(R.id.nav_view);
        android.widget.ImageButton btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(v -> drawerLayout.open());

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_task_list) {
                // Already on task list
                drawerLayout.close();
            } else if (id == R.id.nav_food_scanner) {
                startActivity(new android.content.Intent(this, FoodScannerActivity.class));
                drawerLayout.close();
            } else if (id == R.id.nav_batch_add) {
                android.widget.Toast.makeText(this, "Batch Mode Coming Soon", android.widget.Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_remove_ads) {
                android.widget.Toast.makeText(this, "Ads Removed", android.widget.Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_more_apps) {
                // Open developer page or similar
                android.widget.Toast.makeText(this, "More Apps Logic", android.widget.Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_feedback) {
                // Send Feedback via Email
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                intent.setData(android.net.Uri.parse("mailto:support@dailybalance.com"));
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback for DailyBalance");
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    android.widget.Toast.makeText(this, "No email app found", android.widget.Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.nav_follow_us) {
                // Open Website/Social
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://www.example.com"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                }
            } else if (id == R.id.nav_invite_friends) {
                // Share App
                android.content.Intent sendIntent = new android.content.Intent();
                sendIntent.setAction(android.content.Intent.ACTION_SEND);
                sendIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        "Check out DailyBalance, the best productivity app! Download it now.");
                sendIntent.setType("text/plain");
                startActivity(android.content.Intent.createChooser(sendIntent, "Share via"));
            } else if (id == R.id.nav_settings) {
                showThemeDialog();
            }
            drawerLayout.close();
            return true;
        });

        updateGreeting();
    }

    private void updateGreeting() {
        android.widget.TextView textGreeting = findViewById(R.id.textGreeting);

        // Time logic
        java.util.Calendar c = java.util.Calendar.getInstance();
        int timeOfDay = c.get(java.util.Calendar.HOUR_OF_DAY);
        String timeGreeting;

        if (timeOfDay >= 0 && timeOfDay < 12) {
            timeGreeting = "Good Morning";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            timeGreeting = "Good Afternoon";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            timeGreeting = "Good Evening";
        } else if (timeOfDay >= 21 && timeOfDay < 24) {
            timeGreeting = "Good Night";
        } else {
            timeGreeting = "Hello";
        }

        // Fetch User Name
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser();
        if (user != null) {
            android.content.SharedPreferences prefs = getSharedPreferences("DailyBalancePrefs", MODE_PRIVATE);
            String cachedName = prefs.getString("user_name_" + user.getUid(), null);

            if (cachedName != null) {
                String firstName = cachedName.split(" ")[0];
                textGreeting.setText(timeGreeting + ",\n" + firstName);
            }

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                // Update Cache
                                prefs.edit().putString("user_name_" + user.getUid(), name).apply();

                                // Update UI if changed
                                String firstName = name.split(" ")[0];
                                textGreeting.setText(timeGreeting + ",\n" + firstName);
                            } else {
                                textGreeting.setText(timeGreeting);
                            }
                        } else {
                            textGreeting.setText(timeGreeting);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (cachedName == null) {
                            textGreeting.setText(timeGreeting);
                        }
                    });
        } else {
            textGreeting.setText(timeGreeting);
        }
    }

    // Removed updateProductivity as it's now handled via LiveData observation

    private void showThemeDialog() {
        String[] themes = { "Light Mode", "Dark Mode", "System Default" };
        int currentMode = getCurrentThemeMode();

        new android.app.AlertDialog.Builder(this)
                .setTitle("Choose Theme")
                .setSingleChoiceItems(themes, currentMode, (dialog, which) -> {
                    setThemeMode(which);
                    dialog.dismiss();
                    recreate(); // Restart activity to apply theme
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void applyTheme() {
        int mode = getCurrentThemeMode();
        switch (mode) {
            case 0: // Light
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 1: // Dark
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2: // System
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private int getCurrentThemeMode() {
        android.content.SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        return prefs.getInt("theme_mode", 2); // Default: System
    }

    private void setThemeMode(int mode) {
        android.content.SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        prefs.edit().putInt("theme_mode", mode).apply();

        switch (mode) {
            case 0: // Light
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 1: // Dark
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2: // System
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private void setupHabits() {
        androidx.recyclerview.widget.RecyclerView recyclerHabits = findViewById(R.id.recyclerViewHabits);
        recyclerHabits.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));

        // Pass empty list initially, and a click listener for streak updates
        HabitAdapter habitAdapter = new HabitAdapter(new java.util.ArrayList<>(),
                new HabitAdapter.OnHabitClickListener() {
                    @Override
                    public void onHabitClick(com.example.dailybalance.data.local.entity.Habit habit) {
                        // Increment streak
                        habit.streak++;
                        habit.lastCompletedDate = System.currentTimeMillis();
                        viewModel.updateHabit(habit);
                        android.widget.Toast
                                .makeText(DashboardActivity.this, "Streak Updated: " + habit.name,
                                        android.widget.Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onHabitLongClick(com.example.dailybalance.data.local.entity.Habit habit) {
                        new android.app.AlertDialog.Builder(DashboardActivity.this)
                                .setTitle("Delete Habit")
                                .setMessage("Are you sure you want to delete " + habit.name + "?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    viewModel.deleteHabit(habit);
                                    android.widget.Toast.makeText(DashboardActivity.this, "Habit Deleted",
                                            android.widget.Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                }, () -> {
                    // On Add Click
                    showAddHabitDialog();
                });

        recyclerHabits.setAdapter(habitAdapter);

        // Seed default habits if needed
        viewModel.checkAndSeedHabits();

        // Observe real data
        viewModel.getAllHabits().observe(this, habits -> {
            habitAdapter.setHabits(habits);
        });
    }

    private void showAddHabitDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("New Habit");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Habit Name (e.g. Drink Water)");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String habitName = input.getText().toString().trim();
            if (!habitName.isEmpty()) {
                viewModel.insertHabit(new com.example.dailybalance.data.local.entity.Habit(habitName, 0, 0));
                android.widget.Toast.makeText(this, "Habit Added!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
