package com.waterbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Manages water consumption history for weekly/monthly statistics
 */
public class HistoryTracker {
    private static final String PREFS_HISTORY = "water_history";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    /**
     * Record water consumption for a specific date
     */
    public static void recordGlasses(Context context, String date, int glasses) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_HISTORY, Context.MODE_PRIVATE);
        prefs.edit().putInt(date, glasses).apply();
    }

    /**
     * Get glasses consumed for a specific date
     */
    public static int getGlassesForDate(Context context, String date) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_HISTORY, Context.MODE_PRIVATE);
        return prefs.getInt(date, 0);
    }

    /**
     * Get average glasses per day for the week
     */
    public static double getWeeklyAverage(Context context) {
        int total = 0;
        Calendar cal = Calendar.getInstance();
        
        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(cal.getTime());
            total += getGlassesForDate(context, date);
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        
        return total / 7.0;
    }

    /**
     * Get total glasses for the week
     */
    public static int getWeeklyTotal(Context context) {
        int total = 0;
        Calendar cal = Calendar.getInstance();
        
        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(cal.getTime());
            total += getGlassesForDate(context, date);
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        
        return total;
    }

    /**
     * Get best day in the past week
     */
    public static int getBestDayOfWeek(Context context) {
        int maxGlasses = 0;
        Calendar cal = Calendar.getInstance();
        
        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(cal.getTime());
            int glasses = getGlassesForDate(context, date);
            maxGlasses = Math.max(maxGlasses, glasses);
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        
        return maxGlasses;
    }
}
