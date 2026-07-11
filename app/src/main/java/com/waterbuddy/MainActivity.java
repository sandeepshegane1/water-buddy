package com.waterbuddy;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Main activity for Water Buddy app with hourly reminders and hydration tracking
 */
public class MainActivity extends Activity {
    SharedPreferences prefs;
    TextView total, message, streakDisplay, progressDisplay, weeklyStatsDisplay;
    Switch voice;
    ProgressBar dailyProgress;
    LinearLayout page;
    int blue = Color.rgb(48, 151, 222);

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("water", MODE_PRIVATE);
        build();
        createChannel();
        requestPermission();
        scheduleReminders();
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private TextView label(String x, int size) {
        TextView v = new TextView(this);
        v.setText(x);
        v.setTextSize(size);
        v.setTextColor(Color.rgb(30, 74, 101));
        v.setPadding(12, 12, 12, 12);
        return v;
    }

    private Button button(String x) {
        Button b = new Button(this);
        b.setText(x);
        b.setTextSize(17);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(blue);
        b.setAllCaps(false);
        b.setPadding(10, 12, 10, 12);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        b.setLayoutParams(params);
        return b;
    }

    private void build() {
        ScrollView scroll = new ScrollView(this);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(42, 55, 42, 42);
        page.setBackgroundColor(Color.rgb(234, 248, 255));

        TextView title = label("💧 Water Buddy", 30);
        title.setTextColor(blue);
        page.addView(title);

        TextView subtitle = label("Your cheerful hydration companion", 17);
        page.addView(subtitle);

        Space s1 = new Space(this);
        page.addView(s1, new LinearLayout.LayoutParams(1, 20));

        TextView progressLabel = label("Today's Progress", 20);
        progressLabel.setTextColor(blue);
        page.addView(progressLabel);

        total = label("", 25);
        total.setGravity(Gravity.CENTER);
        total.setPadding(5, 20, 5, 20);
        page.addView(total);

        dailyProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        dailyProgress.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 16));
        dailyProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(blue));
        page.addView(dailyProgress);

        progressDisplay = label("", 14);
        progressDisplay.setGravity(Gravity.CENTER);
        page.addView(progressDisplay);

        Space s2 = new Space(this);
        page.addView(s2, new LinearLayout.LayoutParams(1, 24));

        LinearLayout weeklyCard = new LinearLayout(this);
        weeklyCard.setOrientation(LinearLayout.VERTICAL);
        weeklyCard.setPadding(16, 16, 16, 16);
        weeklyCard.setBackgroundColor(Color.rgb(200, 230, 250));
        weeklyCard.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        weeklyStatsDisplay = label("📊 Weekly Average: -- glasses/day", 16);
        weeklyStatsDisplay.setGravity(Gravity.CENTER);
        weeklyCard.addView(weeklyStatsDisplay);

        page.addView(weeklyCard);

        Space s2b = new Space(this);
        page.addView(s2b, new LinearLayout.LayoutParams(1, 12));

        Button drink = button("🥤 I drank water (+1)");
        page.addView(drink);
        drink.setOnClickListener(v -> {
            HydrationTracker.addGlass(this);
            HistoryTracker.recordGlasses(this, HydrationTracker.getToday(), HydrationTracker.getGlassesToday(this));
            refresh();
            Toast.makeText(this, "Great job! 💪 Keep hydrating!", Toast.LENGTH_SHORT).show();
        });

        message = label("", 16);
        message.setGravity(Gravity.CENTER);
        page.addView(message);

        Space s3 = new Space(this);
        page.addView(s3, new LinearLayout.LayoutParams(1, 20));

        LinearLayout streakCard = new LinearLayout(this);
        streakCard.setOrientation(LinearLayout.VERTICAL);
        streakCard.setPadding(16, 16, 16, 16);
        streakCard.setBackgroundColor(Color.rgb(200, 230, 250));
        streakCard.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        streakDisplay = label("🔥 Streak: 0 days", 18);
        streakDisplay.setGravity(Gravity.CENTER);
        streakCard.addView(streakDisplay);

        page.addView(streakCard);

        Space s4 = new Space(this);
        page.addView(s4, new LinearLayout.LayoutParams(1, 24));

        TextView st = label("⏰ Reminder Settings", 21);
        st.setTextColor(blue);
        page.addView(st);

        voice = new Switch(this);
        voice.setText("🔊 Cute baby voice reminders");
        voice.setTextSize(17);
        voice.setChecked(prefs.getBoolean("voice", true));
        voice.setPadding(8, 12, 8, 12);
        voice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("voice", isChecked).apply();
        });
        page.addView(voice);

        page.addView(label("Reminders appear hourly between set times. Toggle voice or disable reminders in settings.", 13));

        Space s5 = new Space(this);
        page.addView(s5, new LinearLayout.LayoutParams(1, 12));

        Button test = button("🔔 Test Reminder");
        page.addView(test);
        test.setOnClickListener(v -> ReminderReceiver.showReminder(this));

        Button settings = button("⚙️ Settings");
        page.addView(settings);
        settings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        Button advanced = button("⚡ Advanced Settings");
        page.addView(advanced);
        advanced.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdvancedSettingsActivity.class);
            startActivity(intent);
        });

        Button stats = button("📊 Statistics");
        page.addView(stats);
        stats.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatsActivity.class);
            startActivity(intent);
        });

        scroll.addView(page);
        setContentView(scroll);
    }

    private void refresh() {
        int glasses = HydrationTracker.getGlassesToday(this);
        int goal = HydrationTracker.getDailyGoal(this);
        int streak = HydrationTracker.getCurrentStreak(this);
        int progress = HydrationTracker.getProgressPercentage(this);
        boolean goalReached = HydrationTracker.isDailyGoalReached(this);

        total.setText(glasses + " / " + goal + " glasses");
        if (goalReached) {
            total.setTextColor(Color.rgb(76, 175, 80));
        } else {
            total.setTextColor(blue);
        }

        dailyProgress.setProgress(progress);
        progressDisplay.setText(progress + "% complete");

        double weeklyAvg = HistoryTracker.getWeeklyAverage(this);
        weeklyStatsDisplay.setText(String.format("📊 Weekly Average: %.1f glasses/day", weeklyAvg));

        streakDisplay.setText("🔥 Streak: " + streak + " days");

        if (goalReached) {
            message.setText("✨ Goal reached today! Awesome! 🎉");
            message.setTextColor(Color.rgb(76, 175, 80));
        } else {
            int remaining = goal - glasses;
            message.setText("Drink " + remaining + " more " + (remaining == 1 ? "glass" : "glasses") + " to reach today's goal!");
            message.setTextColor(Color.rgb(255, 152, 0));
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel("water", "Water reminders", NotificationManager.IMPORTANCE_HIGH);
            c.setDescription("Friendly hydration reminders");
            c.enableVibration(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(c);
        }
    }

private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.VIBRATE}, 1);
        }
        // Exact alarms need a separate, user-granted "special app access" on Android 12+.
        // From Android 14 onward it's OFF by default, so send the user to the system
        // screen to turn it on. Without this, hourly reminders silently degrade to
        // inexact timing instead of crashing (handled in WaterReminderScheduler).
        if (!WaterReminderScheduler.canScheduleExactAlarms(this)) {
            WaterReminderScheduler.requestExactAlarmPermission(this);
        }
    }

    private void scheduleReminders() {
        WaterReminderScheduler.scheduleHourlyReminders(this);
    }
}
