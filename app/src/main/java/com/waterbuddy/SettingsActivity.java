package com.waterbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;

/**
 * Settings screen for customizing reminders and hydration preferences
 */
public class SettingsActivity extends Activity {
    private static final int blue = Color.rgb(48, 151, 222);
    private static final int darkBlue = Color.rgb(30, 74, 101);

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

        TextView title = createLabel("⚙️ Settings", 26);
        title.setTextColor(blue);
        layout.addView(title);

        layout.addView(createLabel("Reminder Hours", 20));
        layout.addView(createLabel("Set active reminder hours (24-hour format)", 14));

        LinearLayout startLayout = new LinearLayout(this);
        startLayout.setOrientation(LinearLayout.HORIZONTAL);
        startLayout.setPadding(0, 8, 0, 8);
        TextView startLabel = createLabel("Start Hour:", 16);
        startLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        EditText startHour = new EditText(this);
        startHour.setText(String.valueOf(WaterReminderScheduler.getStartHour(this)));
        startHour.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        startHour.setLayoutParams(new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT));
        startLayout.addView(startLabel);
        startLayout.addView(startHour);
        layout.addView(startLayout);

        LinearLayout endLayout = new LinearLayout(this);
        endLayout.setOrientation(LinearLayout.HORIZONTAL);
        endLayout.setPadding(0, 8, 0, 8);
        TextView endLabel = createLabel("End Hour:", 16);
        endLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        EditText endHour = new EditText(this);
        endHour.setText(String.valueOf(WaterReminderScheduler.getEndHour(this)));
        endHour.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        endHour.setLayoutParams(new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT));
        endLayout.addView(endLabel);
        endLayout.addView(endHour);
        layout.addView(endLayout);

        Button saveHours = createButton("Save Reminder Hours");
        saveHours.setOnClickListener(v -> {
            try {
                int start = Integer.parseInt(startHour.getText().toString());
                int end = Integer.parseInt(endHour.getText().toString());
                if (start >= 0 && start < 24 && end > start && end <= 24) {
                    WaterReminderScheduler.setReminderHours(this, start, end);
                    Toast.makeText(this, "Reminders set: " + start + ":00 - " + end + ":00", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Invalid hours! Use 0-24", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(saveHours);

        layout.addView(new Space(this));

        layout.addView(createLabel("Daily Hydration Goal", 20));
        LinearLayout goalLayout = new LinearLayout(this);
        goalLayout.setOrientation(LinearLayout.HORIZONTAL);
        goalLayout.setPadding(0, 8, 0, 8);
        TextView goalLabel = createLabel("Glasses per day:", 16);
        goalLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        EditText goalInput = new EditText(this);
        goalInput.setText(String.valueOf(HydrationTracker.getDailyGoal(this)));
        goalInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        goalInput.setLayoutParams(new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT));
        goalLayout.addView(goalLabel);
        goalLayout.addView(goalInput);
        layout.addView(goalLayout);

        Button saveGoal = createButton("Save Daily Goal");
        saveGoal.setOnClickListener(v -> {
            try {
                int goal = Integer.parseInt(goalInput.getText().toString());
                if (goal > 0 && goal <= 20) {
                    HydrationTracker.setDailyGoal(this, goal);
                    Toast.makeText(this, "Daily goal set to " + goal + " glasses", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please enter 1-20 glasses", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(saveGoal);

        layout.addView(new Space(this));

        layout.addView(createLabel("Glass Size", 20));
        LinearLayout sizeLayout = new LinearLayout(this);
        sizeLayout.setOrientation(LinearLayout.VERTICAL);
        sizeLayout.setPadding(0, 8, 0, 16);

        String[] sizes = {"250ml (Standard)", "500ml (Large)", "330ml (Can)", "200ml (Small)"};
        int[] sizeValues = {250, 500, 330, 200};
        int currentSize = HydrationTracker.getGlassSizeMl(this);

        for (int i = 0; i < sizes.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(sizes[i]);
            rb.setTextSize(16);
            rb.setTextColor(darkBlue);
            rb.setChecked(sizeValues[i] == currentSize);
            final int size = sizeValues[i];
            rb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    HydrationTracker.setGlassSizeMl(SettingsActivity.this, size);
                }
            });
            sizeLayout.addView(rb);
        }
        layout.addView(sizeLayout);

        layout.addView(new Space(this));

        layout.addView(createLabel("Reminders", 20));
        Switch enableReminders = new Switch(this);
        enableReminders.setText("Enable hourly reminders");
        enableReminders.setTextSize(16);
        enableReminders.setChecked(WaterReminderScheduler.areRemindersEnabled(this));
        enableReminders.setPadding(0, 12, 0, 12);
        enableReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            WaterReminderScheduler.setRemindersEnabled(SettingsActivity.this, isChecked);
            Toast.makeText(this, isChecked ? "Reminders enabled" : "Reminders disabled", Toast.LENGTH_SHORT).show();
        });
        layout.addView(enableReminders);

        layout.addView(new Space(this));

        Button backButton = createButton("← Back");
        backButton.setOnClickListener(v -> finish());
        layout.addView(backButton);

        scroll.addView(layout);
        setContentView(scroll);
    }

    private TextView createLabel(String text, int size) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(darkBlue);
        tv.setPadding(0, 12, 0, 12);
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
