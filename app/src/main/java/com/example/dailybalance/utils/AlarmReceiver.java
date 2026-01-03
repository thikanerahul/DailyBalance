package com.example.dailybalance.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.dailybalance.R;
import com.example.dailybalance.data.local.AppDatabase;
import com.example.dailybalance.data.local.entity.Task;
import com.example.dailybalance.ui.DashboardActivity;

public class AlarmReceiver extends BroadcastReceiver {
        private static final String CHANNEL_ID = "DAILY_BALANCE_CHANNEL";

        @Override
        public void onReceive(Context context, Intent intent) {
                final String title = intent.getStringExtra("TITLE") != null ? intent.getStringExtra("TITLE")
                                : "Task Reminder";
                final long taskId = intent.getLongExtra("TASK_ID", -1);
                final boolean isRecurring = intent.getBooleanExtra("IS_RECURRING", false);
                final boolean enableNotification = intent.getBooleanExtra("ENABLE_NOTIFICATION", true);
                final boolean enableAlarm = intent.getBooleanExtra("ENABLE_ALARM", true);

                // Check if task is completed for today (in background thread)
                new Thread(() -> {
                        boolean shouldShowNotification = true;

                        if (taskId != -1) {
                                AppDatabase db = AppDatabase.getInstance(context);
                                Task task = db.taskDao().getTaskById(taskId);

                                if (task != null) {
                                        // Check if already completed today
                                        if (TaskCompletionHelper.isCompletedToday(task)) {
                                                shouldShowNotification = false;
                                        }

                                        // If recurring, reschedule for tomorrow
                                        if (isRecurring) {
                                                AlarmUtils.rescheduleForNextDay(context, taskId, title, task.dateTime);
                                        }
                                }
                        }

                        // Show notification only if not completed
                        if (shouldShowNotification) {
                                showNotification(context, title, taskId, enableNotification, enableAlarm);
                        }
                }).start();
        }

        private void showNotification(Context context, String title, long taskId, boolean enableNotification, boolean enableAlarm) {
                NotificationManager notificationManager = (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Task Notifications",
                                        NotificationManager.IMPORTANCE_HIGH);
                        channel.setDescription("Channel for DailyBalance Task Reminders");
                        channel.enableVibration(true);
                        channel.setVibrationPattern(new long[] { 0, 500, 200, 500 });
                        android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                                        .build();
                        channel.setSound(
                                        android.media.RingtoneManager
                                                        .getDefaultUri(android.media.RingtoneManager.TYPE_ALARM),
                                        audioAttributes);
                        notificationManager.createNotificationChannel(channel);
                }

                Intent openAppIntent = new Intent(context, DashboardActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openAppIntent,
                                PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle("DailyBalance Reminder")
                                .setContentText(title)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setCategory(NotificationCompat.CATEGORY_ALARM)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setDefaults(NotificationCompat.DEFAULT_LIGHTS);

                // Add alarm sound and vibration only if enabled
                if (enableAlarm) {
                        builder.setSound(android.media.RingtoneManager
                                        .getDefaultUri(android.media.RingtoneManager.TYPE_ALARM))
                                .setVibrate(new long[] { 0, 1000, 500, 1000 })
                                .setFullScreenIntent(pendingIntent, true)
                                .setOngoing(true);
                } else {
                        // Silent notification - only vibrate once
                        builder.setVibrate(new long[] { 0, 300 });
                }

                android.app.Notification notification = builder.build();
                
                if (enableAlarm) {
                        notification.flags |= android.app.Notification.FLAG_INSISTENT; // Loop sound until cancelled
                }

                // Use task ID as notification ID to ensure unique notifications
                int notificationId = (int) (taskId != -1 ? taskId : System.currentTimeMillis());
                notificationManager.notify(notificationId, notification);
        }
}
