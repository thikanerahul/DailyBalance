package com.example.dailybalance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.example.dailybalance.R;
import com.example.dailybalance.ui.base.BaseActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        TextInputEditText inputName = findViewById(R.id.inputName);
        TextInputEditText inputPhone = findViewById(R.id.inputPhone);
        TextInputEditText inputEmail = findViewById(R.id.inputEmail);
        TextInputEditText inputPassword = findViewById(R.id.inputPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView textLogin = findViewById(R.id.textLogin);

        btnRegister.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                android.widget.Toast.makeText(this, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                android.widget.Toast
                        .makeText(this, "Password must be at least 6 characters", android.widget.Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            android.widget.Toast.makeText(this, "Creating Account...", android.widget.Toast.LENGTH_SHORT).show();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Save User Details to Firestore
                            String uid = auth.getCurrentUser().getUid();
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("phone", phone);
                            user.put("email", email);

                            firestore.collection("users").document(uid).set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        // Save persistent login AND phone for SMS
                                        getSharedPreferences("DailyBalancePrefs", MODE_PRIVATE).edit()
                                                .putBoolean("is_logged_in", true)
                                                .putString("user_phone", phone) // Save for AlarmReceiver
                                                .apply();
                                        navigateToDashboard();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Navigate anyway if firestore fails, auth is critical
                                        navigateToDashboard();
                                    });
                        } else {
                            android.widget.Toast
                                    .makeText(this, "Registration Failed: " + task.getException().getMessage(),
                                            android.widget.Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        });

        textLogin.setOnClickListener(v -> {
            finish(); // Go back to Login
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
