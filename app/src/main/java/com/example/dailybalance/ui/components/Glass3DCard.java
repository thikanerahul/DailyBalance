package com.example.dailybalance.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A custom FrameLayout that applies a 3D tilt effect when touched.
 * Mimics the iOS card parallax effect.
 */
public class Glass3DCard extends FrameLayout {

    private static final int MAX_ROTATION_DEGREES = 10;
    private static final long ANIMATION_DURATION = 200;

    public Glass3DCard(@NonNull Context context) {
        super(context);
        init();
    }

    public Glass3DCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Glass3DCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Set default elevation for depth
        setElevation(20f);
        // Ensure children don't clip during 3D rotation
        setClipChildren(false);
        setClipToPadding(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Apply 3D Effect
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float width = getWidth();
                float height = getHeight();

                // Calculate percentage from center (-0.5 to 0.5)
                float pX = (event.getX() / width) - 0.5f;
                float pY = (event.getY() / height) - 0.5f;

                // Rotation: Input Y affects X rotation, Input X affects Y rotation
                float rotationX = -pY * MAX_ROTATION_DEGREES * 2;
                float rotationY = pX * MAX_ROTATION_DEGREES * 2;

                animate()
                        .rotationX(rotationX)
                        .rotationY(rotationY)
                        .scaleX(0.98f) // Slight press effect
                        .scaleY(0.98f)
                        .setDuration(0)
                        .start();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                animate()
                        .rotationX(0f)
                        .rotationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                break;
        }

        // Pass event to super to handle Clicks and Long Clicks
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
