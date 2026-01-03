package com.example.dailybalance.ui;

import android.graphics.Color;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.example.dailybalance.R;
import com.example.dailybalance.ui.base.BaseActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsActivity extends BaseActivity {

    private DailyViewModel viewModel;
    private BarChart barChart;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);

        viewModel = new ViewModelProvider(this).get(DailyViewModel.class);

        setupCharts();
    }

    private void setupCharts() {
        setupBarChart();
        setupPieChart();
    }

    private void setupBarChart() {
        // Get all tasks for analysis
        viewModel.getAllTasks().observe(this, tasks -> {
            if (tasks == null)
                return;
            updateBarChart(tasks);
        });
    }

    private void updateBarChart(List<com.example.dailybalance.data.local.entity.Task> tasks) {
        List<BarEntry> entries = new ArrayList<>();

        // Use AnalyticsHelper to get weekly completion counts
        int[] weekCounts = com.example.dailybalance.utils.AnalyticsHelper.getWeeklyCompletionCounts(tasks);

        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i + 1, weekCounts[i]));
        }

        BarDataSet set = new BarDataSet(entries, "Completed Tasks (Last 7 Days)");
        set.setColor(Color.parseColor("#9013FE"));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(12f);

        BarData data = new BarData(set);
        barChart.setData(data);
        barChart.getXAxis().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setTextColor(Color.WHITE);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate(); // Refresh
        barChart.animateY(1000);
    }

    private void setupPieChart() {
        viewModel.getAllCompletedTasks().observe(this, tasks -> {
            if (tasks == null || tasks.isEmpty())
                return;
            updatePieChart(tasks);
        });
    }

    private void updatePieChart(List<com.example.dailybalance.data.local.entity.Task> tasks) {
        java.util.Map<String, Integer> categoryCount = new java.util.HashMap<>();

        for (com.example.dailybalance.data.local.entity.Task t : tasks) {
            String cat = t.category != null ? t.category : "Uncategorized";
            categoryCount.put(cat, categoryCount.getOrDefault(cat, 0) + 1);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (java.util.Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet set = new PieDataSet(entries, "Categories");
        int[] colors = {
                Color.parseColor("#4A90E2"),
                Color.parseColor("#9013FE"),
                Color.parseColor("#50E3C2"),
                Color.parseColor("#F5A623"),
                Color.parseColor("#FF5722")
        };
        set.setColors(colors);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(14f);

        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
        pieChart.animateXY(1000, 1000);
    }
}
