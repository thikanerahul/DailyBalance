package com.example.dailybalance.ui.base;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * BaseActivity serving as the foundation for all activities in the app.
 * It will eventually handle the global 3D background animations and common UI
 * states.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Common setup can go here
        setup3DBackground();
    }

    private void setup3DBackground() {
        // Placeholder for 3D background initialization
        // We will implement dynamic gradient shifting or Lottie background here
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        if (rootView instanceof ViewGroup) {
            // Future 3D logic
        }
    }
}
