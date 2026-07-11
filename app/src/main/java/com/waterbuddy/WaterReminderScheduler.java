package com.waterbuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                // Only allowed once the user has granted "Alarms & reminders" access.
                // On Android 14+ this permission is OFF by default, so we must check
                // canScheduleExactAlarms() first or the app crashes with a SecurityException.
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                // Fallback: inexact alarm, still works, just may fire a little late.
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
    }

    /**
     * Returns true if this app is currently allowed to schedule exact alarms.
     * Always true below Android 12 (S); must be checked on S and above.
     */
    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        return alarmManager != null && alarmManager.canScheduleExactAlarms();
    }

    /**
     * Sends the user to the system "Alarms & reminders" screen so they can grant
     * exact-alarm access. Only needed on Android 12+ (required in practice from
     * Android 14 onward, since that's when the permission stopped being auto-granted).
     */
    public static void requestExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
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
