package com.waterbuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import java.util.Calendar;

/**
 * Handles scheduling hourly water reminders with customizable hours
 */
public class WaterReminderScheduler {
    private static final String PREFS_REMINDER = "reminder_prefs";
    private static final String KEY_START_HOUR = "start_hour";
    private static final String KEY_END_HOUR = "end_hour";
    private static final String KEY_INTERVAL = "interval_minutes";
    private static final String KEY_ENABLED = "reminders_enabled";

    public static void scheduleHourlyReminders(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        int startHour = prefs.getInt(KEY_START_HOUR, 8);      // Default: 8 AM
        int endHour = prefs.getInt(KEY_END_HOUR, 22);         // Default: 10 PM
        int interval = prefs.getInt(KEY_INTERVAL, 60);        // Default: 60 minutes

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Schedule reminders for each hour in the active range
        for (int hour = startHour; hour < endHour; hour++) {
            calendar.set(Calendar.HOUR_OF_DAY, hour);

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.setAction("REMIND");

            int requestCode = 1000 + hour;
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
    }

    public static void cancelAllReminders(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        int startHour = prefs.getInt(KEY_START_HOUR, 8);
        int endHour = prefs.getInt(KEY_END_HOUR, 22);

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        for (int hour = startHour; hour < endHour; hour++) {
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.setAction("REMIND");

            int requestCode = 1000 + hour;
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    public static void setReminderHours(Context context, int startHour, int endHour) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_START_HOUR, startHour)
                .putInt(KEY_END_HOUR, endHour)
                .apply();

        cancelAllReminders(context);
        scheduleHourlyReminders(context);
    }

    public static int getStartHour(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_START_HOUR, 8);
    }

    public static int getEndHour(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_END_HOUR, 22);
    }

    public static void setRemindersEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();

        if (enabled) {
            scheduleHourlyReminders(context);
        } else {
            cancelAllReminders(context);
        }
    }

    public static boolean areRemindersEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLED, true);
    }
}
