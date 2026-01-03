package com.example.dailybalance.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.dailybalance.R;
import com.example.dailybalance.data.local.entity.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private DailyViewModel viewModel;
    private long selectedDateTime = System.currentTimeMillis();
    private Calendar calendar = Calendar.getInstance();
    private boolean isRecurring = false;
    private boolean dateSelected = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DailyViewModel.class);

        TextInputEditText titleInput = view.findViewById(R.id.editTextTitle);
        SwitchMaterial switchRecurring = view.findViewById(R.id.switchRecurring);
        android.widget.CheckBox checkNotification = view.findViewById(R.id.checkNotification);
        android.widget.CheckBox checkAlarm = view.findViewById(R.id.checkAlarm);
        Button btnDate = view.findViewById(R.id.btnDate);
        Button btnTime = view.findViewById(R.id.btnTime);
        Button btnSave = view.findViewById(R.id.btnSave);

        // Handle recurring switch
        switchRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isRecurring = isChecked;
            if (isChecked) {
                // Recurring task - date is optional
                btnDate.setEnabled(false);
                btnDate.setText("Daily");
                btnDate.setAlpha(0.5f);
                dateSelected = false;
            } else {
                // One-time task - date is required
                btnDate.setEnabled(true);
                btnDate.setText("Pick Date");
                btnDate.setAlpha(1.0f);
            }
        });

        btnDate.setOnClickListener(v -> {
            if (!isRecurring) {
                new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    btnDate.setText(String.format("%d/%d/%d", dayOfMonth, month + 1, year));
                    dateSelected = true;
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        btnTime.setOnClickListener(v -> {
            new TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                // Format time with AM/PM
                String amPm = hourOfDay >= 12 ? "PM" : "AM";
                int hour12 = hourOfDay % 12;
                if (hour12 == 0)
                    hour12 = 12;
                btnTime.setText(String.format("%02d:%02d %s", hour12, minute, amPm));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        });

        btnSave.setOnClickListener(v -> {
            String title = titleInput.getText().toString();
            if (title.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Please enter a title",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            boolean enableNotification = checkNotification.isChecked();
            boolean enableAlarm = checkAlarm.isChecked();

            if (!enableNotification && !enableAlarm) {
                android.widget.Toast.makeText(requireContext(), "Please select at least one reminder type",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            selectedDateTime = calendar.getTimeInMillis();

            // For recurring tasks, use today's time if not set
            if (isRecurring && !dateSelected) {
                Calendar todayCal = Calendar.getInstance();
                todayCal.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
                todayCal.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
                todayCal.set(Calendar.SECOND, 0);
                todayCal.set(Calendar.MILLISECOND, 0);
                selectedDateTime = todayCal.getTimeInMillis();
            }

            Task task = new Task(title, "", selectedDateTime, 1, "Work", isRecurring, enableNotification, enableAlarm);

            // Save context before background thread
            final android.content.Context context = requireContext().getApplicationContext();
            final boolean recurring = isRecurring;

            // Insert task in background thread and get the ID
            new Thread(() -> {
                long taskId = viewModel.getTaskDao().insert(task);
                task.id = taskId;

                // Schedule Alarm
                com.example.dailybalance.utils.AlarmUtils.scheduleAlarm(context, task);

                // Show toast on UI thread if fragment is still attached
                if (getActivity() != null && isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        String reminderType = "";
                        if (enableNotification && enableAlarm) {
                            reminderType = "Notification & Alarm";
                        } else if (enableNotification) {
                            reminderType = "Notification";
                        } else {
                            reminderType = "Alarm";
                        }
                        String message = recurring ? "Daily Task Added with " + reminderType + "!" : "Task Added with " + reminderType + "!";
                        android.widget.Toast.makeText(context, message,
                                android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();

            dismiss();
        });
    }
}
