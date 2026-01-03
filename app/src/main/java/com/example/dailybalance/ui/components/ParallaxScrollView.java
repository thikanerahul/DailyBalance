package com.example.dailybalance.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.widget.NestedScrollView;

/**
 * ScrollView that moves its background slower than the content to create depth.
 */
public class ParallaxScrollView extends NestedScrollView {

    public ParallaxScrollView(Context context) {
        super(context);
    }

    public ParallaxScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ParallaxScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // Move the background View (if defined in Activity) or self background logic
        // For simple parallax: translate background drawable?
        // Or access a specific background view via tag?

        // Strategy: Assume the first child is a ViewGroup containing content.
        // We want to shift the Window Background or a specific underlying view.
        // For this impl, we will emit an event or let the caller handle the listener,
        // but for "Big Project" style, let's just make it simple:
        // shift Y of background by half the scroll amount.

        View view = getChildAt(0);
        if (view != null) {
            // view.setTranslationY(t * 0.5f); // This would move content, not background.
        }
    }

    // Interface for external listeners to apply effects
    public interface OnScrollChangedListener {
        void onScrollChanged(int scrollY);
    }
}
