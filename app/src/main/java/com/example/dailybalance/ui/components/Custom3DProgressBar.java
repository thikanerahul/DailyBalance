package com.example.dailybalance.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * A custom View that draws a 3D-style progress ring.
 * Uses gradients and shadow layers to simulate depth (tube effect).
 */
public class Custom3DProgressBar extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;
    private float progress = 0;
    private int max = 100;
    private int strokeWidth = 40;

    public Custom3DProgressBar(Context context) {
        super(context);
        init();
    }

    public Custom3DProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rectF = new RectF();

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setColor(Color.parseColor("#33FFFFFF")); // Glassy track
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Gradient for 3D effect
        int[] colors = { Color.parseColor("#4A90E2"), Color.parseColor("#9013FE"), Color.parseColor("#4A90E2") };
        float[] positions = { 0f, 0.5f, 1f };
        SweepGradient gradient = new SweepGradient(0, 0, colors, positions);
        // We will Apply gradient in onDraw since we need width/height
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int padding = strokeWidth / 2;
        rectF.set(padding, padding, w - padding, h - padding);

        // Refine gradient position
        SweepGradient gradient = new SweepGradient(w / 2f, h / 2f,
                new int[] { Color.parseColor("#4A90E2"), Color.parseColor("#9013FE"), Color.parseColor("#4A90E2") },
                null);
        progressPaint.setShader(gradient);

        // Add shadow for depth
        progressPaint.setShadowLayer(10f, 0f, 5f, Color.parseColor("#80000000"));
        setLayerType(LAYER_TYPE_SOFTWARE, progressPaint); // Shadow needs software layer
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw Track
        canvas.drawArc(rectF, 135, 270, false, backgroundPaint);

        // Draw Progress
        float sweepAngle = (270f * progress) / max;
        canvas.drawArc(rectF, 135, sweepAngle, false, progressPaint);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }
}
