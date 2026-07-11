package com.waterbuddy;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages water consumption reminders with snooze and quiet hours
 */
public class ReminderPreferences {
    private static final String PREFS_REMINDER = "reminder_prefs";
    private static final String KEY_SNOOZE_MINUTES = "snooze_minutes";
    private static final String KEY_QUIET_MODE_ENABLED = "quiet_mode_enabled";
    private static final String KEY_QUIET_START = "quiet_start_hour";
    private static final String KEY_QUIET_END = "quiet_end_hour";
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    private static final String KEY_REMINDER_SOUND = "reminder_sound";

    /**
     * Set snooze duration in minutes
     */
    public static void setSnoozeMinutes(Context context, int minutes) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_SNOOZE_MINUTES, minutes).apply();
    }

    /**
     * Get snooze duration
     */
    public static int getSnoozeMinutes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_SNOOZE_MINUTES, 15);
    }

    /**
     * Enable/disable quiet hours (no notifications during specific time)
     */
    public static void setQuietMode(Context context, boolean enabled, int startHour, int endHour) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_QUIET_MODE_ENABLED, enabled)
                .putInt(KEY_QUIET_START, startHour)
                .putInt(KEY_QUIET_END, endHour)
                .apply();
    }

    /**
     * Check if currently in quiet hours
     */
    public static boolean isInQuietHours(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_QUIET_MODE_ENABLED, false)) {
            return false;
        }

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        int startHour = prefs.getInt(KEY_QUIET_START, 22);
        int endHour = prefs.getInt(KEY_QUIET_END, 7);

        if (startHour < endHour) {
            return currentHour >= startHour && currentHour < endHour;
        } else {
            return currentHour >= startHour || currentHour < endHour;
        }
    }

    /**
     * Enable/disable vibration
     */
    public static void setVibrationEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
    }

    /**
     * Check if vibration is enabled
     */
    public static boolean isVibrationEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    /**
     * Get quiet hours start time
     */
    public static int getQuietStartHour(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_QUIET_START, 22);
    }

    /**
     * Get quiet hours end time
     */
    public static int getQuietEndHour(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_QUIET_END, 7);
    }

    /**
     * Check if quiet mode is enabled
     */
    public static boolean isQuietModeEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMINDER, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_QUIET_MODE_ENABLED, false);
    }
}
