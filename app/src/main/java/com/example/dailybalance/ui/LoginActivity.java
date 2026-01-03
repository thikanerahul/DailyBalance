package com.example.dailybalance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.dailybalance.R;
import com.example.dailybalance.ui.base.BaseActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for persistent login
        android.content.SharedPreferences prefs = getSharedPreferences("DailyBalancePrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        if (isLoggedIn) {
            navigateToDashboard();
            return; // Skip setting content view
        }

        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        TextInputEditText emailInput = findViewById(R.id.inputEmail);
        TextInputEditText passInput = findViewById(R.id.inputPassword);
        TextView registerText = findViewById(R.id.textRegister);

        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                android.widget.Toast
                        .makeText(this, "Please enter email and password", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading (optional, for now just toast)
            android.widget.Toast.makeText(this, "Logging in...", android.widget.Toast.LENGTH_SHORT).show();

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success
                            prefs.edit().putBoolean("is_logged_in", true).apply();
                            navigateToDashboard();
                        } else {
                            android.widget.Toast
                                    .makeText(this, "Login Failed: " + task.getException().getMessage(),
                                            android.widget.Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        });

        registerText.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
