package com.waterbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Tracks hydration data including daily goals, streaks, and statistics
 */
public class HydrationTracker {
    private static final String PREFS_HYDRATION = "hydration_data";
    private static final String KEY_DAILY_GOAL = "daily_goal_glasses";
    private static final String KEY_GLASS_SIZE_ML = "glass_size_ml";
    private static final String KEY_STREAK = "current_streak";
    private static final String KEY_LAST_DRINK_DATE = "last_drink_date";
    private static final String KEY_TOTAL_GLASSES = "total_glasses_all_time";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static String getToday() {
        return dateFormat.format(new Date());
    }

    public static int getGlassesToday(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("water", Context.MODE_PRIVATE);
        String today = getToday();
        if (!today.equals(prefs.getString("date", ""))) {
            return 0;
        }
        return prefs.getInt("glasses", 0);
    }

    public static void addGlass(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("water", Context.MODE_PRIVATE);
        String today = getToday();

        if (!today.equals(prefs.getString("date", ""))) {
            prefs.edit()
                    .putString("date", today)
                    .putInt("glasses", 1)
                    .apply();
        } else {
            prefs.edit()
                    .putInt("glasses", prefs.getInt("glasses", 0) + 1)
                    .apply();
        }

        updateStreak(context);

        SharedPreferences hydrationPrefs = context.getSharedPreferences(PREFS_HYDRATION, Context.MODE_PRIVATE);
        hydrationPrefs.edit()
                .putInt(KEY_TOTAL_GLASSES, hydrationPrefs.getInt(KEY_TOTAL_GLASSES, 0) + 1)
                .apply();
    }

    private static void updateStreak(Context context) {
        SharedPreferences hydrationPrefs = context.getSharedPreferences(PREFS_HYDRATION, Context.MODE_PRIVATE);
        String today = getToday();
        String lastDrinkDate = hydrationPrefs.getString(KEY_LAST_DRINK_DATE, "");

        if (!today.equals(lastDrinkDate)) {
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            String yesterdayStr = dateFormat.format(yesterday.getTime());

            if (yesterdayStr.equals(lastDrinkDate)) {
                int streak = hydrationPrefs.getInt(KEY_STREAK, 0);
                hydrationPrefs.edit()
                        .putInt(KEY_STREAK, streak + 1)
                        .putString(KEY_LAST_DRINK_DATE, today)
                        .apply();
            } else {
                hydrationPrefs.edit()
                        .putInt(KEY_STREAK, 1)
                        .putString(KEY_LAST_DRINK_DATE, today)
                        .apply();
            }
        }
    }

    public static int getCurrentStreak(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_HYDRATION, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_STREAK, 0);
    }

    public static int getDailyGoal(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_HYDRATION, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_DAILY_GOAL, 8);
    }

    public static void setDailyGoal(Context context, int glasses) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_HYDRATION, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_DAILY_GOAL, glasses).apply();
    }

    public static int getGlassSizeMl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_HYDRATION, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_GLASS_SIZE_ML, 250);
    }

    public static void setGlassSizeMl(Context context, int ml) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_HYDRATION, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_GLASS_SIZE_ML, ml).apply();
    }

    public static double getLitersToday(Context context) {
        int glasses = getGlassesToday(context);
        int glassSize = getGlassSizeMl(context);
        return (glasses * glassSize) / 1000.0;
    }

    public static int getProgressPercentage(Context context) {
        int glasses = getGlassesToday(context);
        int goal = getDailyGoal(context);
        return goal > 0 ? Math.min((glasses * 100) / goal, 100) : 0;
    }

    public static boolean isDailyGoalReached(Context context) {
        return getGlassesToday(context) >= getDailyGoal(context);
    }

    public static int getTotalGlassesAllTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_HYDRATION, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_TOTAL_GLASSES, 0);
    }

    public static String getAchievementBadge(Context context) {
        int streak = getCurrentStreak(context);
        int totalGlasses = getTotalGlassesAllTime(context);

        if (streak >= 30) return "🏆 Legend: 30-day streak!";
        if (streak >= 7) return "⭐ Week Warrior: 7-day streak!";
        if (totalGlasses >= 1000) return "💪 Hydration Champion: 1000 glasses!";
        if (totalGlasses >= 500) return "🌟 Super Hydrator: 500 glasses!";
        if (totalGlasses >= 100) return "🎯 Consistent Drinker: 100 glasses!";

        return "";
    }
}
