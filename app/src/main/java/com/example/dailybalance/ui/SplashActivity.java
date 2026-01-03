package com.example.dailybalance.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import com.example.dailybalance.R;
import com.example.dailybalance.ui.base.BaseActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView textLogo = findViewById(R.id.textLogo);
        View viewLight = findViewById(R.id.viewLight);

        // Prep animations
        // 1. Logo Scale Up
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(textLogo, "scaleX", 0.8f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(textLogo, "scaleY", 0.8f, 1.0f);
        ObjectAnimator alphaLogo = ObjectAnimator.ofFloat(textLogo, "alpha", 0f, 1f);

        // 2. Light Sweep (Move from left of screen to right)
        // Calculate screen width approx or just move strictly relative to logo
        float startX = -500f;
        float endX = 1500f; // Roughly significantly across
        ObjectAnimator lightSweep = ObjectAnimator.ofFloat(viewLight, "translationX", startX, endX);
        lightSweep.setDuration(1500); // 1.5s sweep

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(scaleX, scaleY, alphaLogo);
        logoSet.setDuration(1000);

        AnimatorSet fullSet = new AnimatorSet();
        fullSet.playTogether(logoSet, lightSweep);
        fullSet.setInterpolator(new AccelerateDecelerateInterpolator());
        fullSet.start();

        // Navigate after animation
        new Handler().postDelayed(() -> {
            checkLoginAndNavigate();
        }, 2500);
    }

    private void checkLoginAndNavigate() {
        // Robust Check: Use Firebase Auth State
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        boolean isLoggedIn = auth.getCurrentUser() != null;

        Intent intent;
        if (isLoggedIn) {
            intent = new Intent(SplashActivity.this, DashboardActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
