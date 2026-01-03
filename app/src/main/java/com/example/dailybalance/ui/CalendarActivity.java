package com.example.dailybalance.ui;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import com.example.dailybalance.R;
import com.example.dailybalance.ui.base.BaseActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalendarActivity extends BaseActivity {

    private DailyViewModel viewModel;
    private TaskAdapter adapter;
    private TextView textEmpty;
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        CalendarView calendarView = findViewById(R.id.calendarView);
        TextView selectedDateText = findViewById(R.id.textSelectedDate);
        textEmpty = findViewById(R.id.textEmpty);

        androidx.recyclerview.widget.RecyclerView recyclerView = findViewById(R.id.recyclerViewCalendarTasks);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(DailyViewModel.class);

        // Initial load for today
        loadTasksForDate(System.currentTimeMillis());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            loadTasksForDate(cal.getTimeInMillis());

            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            selectedDateText.setText("Tasks for " + date);
        });

        // Handle task updates (e.g. checkbox click)
        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onTaskCheckChanged(com.example.dailybalance.data.local.entity.Task task) {
                viewModel.update(task);
            }

            @Override
            public void onTaskLongClick(com.example.dailybalance.data.local.entity.Task task) {
                new android.app.AlertDialog.Builder(CalendarActivity.this)
                        .setTitle("Delete Task")
                        .setMessage("Delete " + task.title + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            viewModel.delete(task);
                            android.widget.Toast
                                    .makeText(CalendarActivity.this, "Task Deleted", android.widget.Toast.LENGTH_SHORT)
                                    .show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void loadTasksForDate(long dateMillis) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);

        // Start of day
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        long start = calendar.getTimeInMillis();

        // End of day
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calendar.set(java.util.Calendar.MINUTE, 59);
        calendar.set(java.util.Calendar.SECOND, 59);
        long end = calendar.getTimeInMillis();

        viewModel.getTasksForDateRange(start, end).observe(this, tasks -> {
            if (tasks == null || tasks.isEmpty()) {
                adapter.setTasks(new java.util.ArrayList<>());
                textEmpty.setVisibility(android.view.View.VISIBLE);
            } else {
                adapter.setTasks(tasks);
                textEmpty.setVisibility(android.view.View.GONE);
            }
        });
    }
}
