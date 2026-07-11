package com.waterbuddy;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;

/**
 * Statistics and achievements screen
 */
public class StatsActivity extends Activity {
    private static final int blue = Color.rgb(48, 151, 222);
    private static final int darkBlue = Color.rgb(30, 74, 101);
    private static final int lightBlue = Color.rgb(200, 230, 250);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        layout.setBackgroundColor(Color.rgb(234, 248, 255));

        TextView title = createLabel("📊 Statistics & Achievements", 26);
        title.setTextColor(blue);
        layout.addView(title);

        LinearLayout todayCard = createStatsCard("Today's Progress", "💧");
        int todayGlasses = HydrationTracker.getGlassesToday(this);
        int dailyGoal = HydrationTracker.getDailyGoal(this);
        double litersToday = HydrationTracker.getLitersToday(this);
        int progress = HydrationTracker.getProgressPercentage(this);

        todayCard.addView(createLabel(todayGlasses + " / " + dailyGoal + " glasses", 24));
        todayCard.addView(createLabel(String.format("%.2f / %.2f liters", litersToday, dailyGoal * HydrationTracker.getGlassSizeMl(this) / 1000.0), 14));

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setProgress(progress);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20));
        todayCard.addView(progressBar);

        TextView progressText = createLabel(progress + "% complete", 14);
        progressText.setGravity(android.view.Gravity.CENTER);
        todayCard.addView(progressText);

        layout.addView(todayCard);

        layout.addView(new Space(this));

        LinearLayout streakCard = createStatsCard("Drinking Streak", "🔥");
        int streak = HydrationTracker.getCurrentStreak(this);
        TextView streakText = createLabel(streak + " days", 32);
        streakText.setTextColor(blue);
        streakText.setGravity(android.view.Gravity.CENTER);
        streakCard.addView(streakText);
        streakCard.addView(createLabel("Keep it going! 💪", 14));
        layout.addView(streakCard);

        layout.addView(new Space(this));

        LinearLayout statsCard = createStatsCard("All-Time Statistics", "⭐");
        int totalGlasses = HydrationTracker.getTotalGlassesAllTime(this);
        double totalLiters = (totalGlasses * HydrationTracker.getGlassSizeMl(this)) / 1000.0;
        statsCard.addView(createLabel("Total Glasses: " + totalGlasses, 16));
        statsCard.addView(createLabel(String.format("Total Liters: %.1f", totalLiters), 16));
        layout.addView(statsCard);

        layout.addView(new Space(this));

        String badge = HydrationTracker.getAchievementBadge(this);
        if (!badge.isEmpty()) {
            LinearLayout badgeCard = createStatsCard("Achievement Unlocked", "🏆");
            TextView badgeText = createLabel(badge, 18);
            badgeText.setGravity(android.view.Gravity.CENTER);
            badgeCard.addView(badgeText);
            layout.addView(badgeCard);
            layout.addView(new Space(this));
        }

        layout.addView(createLabel("💡 Hydration Tips", 20));
        String[] tips = {
                "• Drink water when you wake up to rehydrate",
                "• Drink a glass of water before each meal",
                "• Keep a water bottle with you always",
                "• Drink more water on hot or active days",
                "• Set reminders to stay consistent"
        };
        for (String tip : tips) {
            layout.addView(createLabel(tip, 13));
        }

        layout.addView(new Space(this));

        Button backButton = createButton("← Back");
        backButton.setOnClickListener(v -> finish());
        layout.addView(backButton);

        scroll.addView(layout);
        setContentView(scroll);
    }

    private LinearLayout createStatsCard(String title, String emoji) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 16, 16, 16);
        card.setBackgroundColor(lightBlue);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        card.setLayoutParams(params);

        TextView titleText = createLabel(emoji + " " + title, 18);
        titleText.setTextColor(blue);
        card.addView(titleText);

        return card;
    }

    private TextView createLabel(String text, int size) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(darkBlue);
        tv.setPadding(8, 8, 8, 8);
        return tv;
    }

    private Button createButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(16);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(blue);
        btn.setAllCaps(false);
        btn.setPadding(12, 16, 12, 16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        btn.setLayoutParams(params);
        return btn;
    }
}
