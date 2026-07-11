package com.waterbuddy;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;

/**
 * Advanced settings with quiet hours, snooze, vibration, etc.
 */
public class AdvancedSettingsActivity extends Activity {
    private static final int blue = Color.rgb(48, 151, 222);
    private static final int darkBlue = Color.rgb(30, 74, 101);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            buildUI();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading settings", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void buildUI() {
        try {
            ScrollView scroll = new ScrollView(this);
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(24, 24, 24, 24);
            layout.setBackgroundColor(Color.rgb(234, 248, 255));

            TextView title = createLabel("⚡ Advanced Settings", 26);
            title.setTextColor(blue);
            layout.addView(title);

            layout.addView(createLabel("🌙 Quiet Hours", 20));
            layout.addView(createLabel("Don't remind me during these hours", 14));

            Switch quietMode = new Switch(this);
            quietMode.setText("Enable Quiet Hours");
            quietMode.setTextSize(16);
            quietMode.setChecked(ReminderPreferences.isQuietModeEnabled(this));
            quietMode.setPadding(0, 12, 0, 12);
            layout.addView(quietMode);

            LinearLayout quietStartLayout = new LinearLayout(this);
            quietStartLayout.setOrientation(LinearLayout.HORIZONTAL);
            quietStartLayout.setPadding(0, 8, 0, 8);
            TextView quietStartLabel = createLabel("Start (Quiet from):", 16);
            quietStartLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            EditText quietStart = new EditText(this);
            quietStart.setText(String.valueOf(ReminderPreferences.getQuietStartHour(this)));
            quietStart.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            quietStart.setLayoutParams(new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT));
            quietStartLayout.addView(quietStartLabel);
            quietStartLayout.addView(quietStart);
            layout.addView(quietStartLayout);

            LinearLayout quietEndLayout = new LinearLayout(this);
            quietEndLayout.setOrientation(LinearLayout.HORIZONTAL);
            quietEndLayout.setPadding(0, 8, 0, 8);
            TextView quietEndLabel = createLabel("End (Quiet until):", 16);
            quietEndLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            EditText quietEnd = new EditText(this);
            quietEnd.setText(String.valueOf(ReminderPreferences.getQuietEndHour(this)));
            quietEnd.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            quietEnd.setLayoutParams(new LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT));
            quietEndLayout.addView(quietEndLabel);
            quietEndLayout.addView(quietEnd);
            layout.addView(quietEndLayout);

            Button saveQuiet = createButton("Save Quiet Hours");
            saveQuiet.setOnClickListener(v -> {
                try {
                    String startStr = quietStart.getText().toString().trim();
                    String endStr = quietEnd.getText().toString().trim();
                    
                    if (startStr.isEmpty() || endStr.isEmpty()) {
                        Toast.makeText(this, "Please enter both hours", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    int start = Integer.parseInt(startStr);
                    int end = Integer.parseInt(endStr);
                    if (start >= 0 && start < 24 && end >= 0 && end < 24) {
                        ReminderPreferences.setQuietMode(this, quietMode.isChecked(), start, end);
                        Toast.makeText(this, "Quiet hours set!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please use hours 0-23", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            });
            layout.addView(saveQuiet);

            layout.addView(new Space(this));

            layout.addView(createLabel("⏱️ Snooze Duration", 20));
            LinearLayout snoozeLayout = new LinearLayout(this);
            snoozeLayout.setOrientation(LinearLayout.VERTICAL);
            snoozeLayout.setPadding(0, 8, 0, 16);

            String[] snoozeOptions = {"5 minutes", "10 minutes", "15 minutes", "20 minutes", "30 minutes"};
            int[] snoozeValues = {5, 10, 15, 20, 30};
            int currentSnooze = ReminderPreferences.getSnoozeMinutes(this);

            for (int i = 0; i < snoozeOptions.length; i++) {
                RadioButton rb = new RadioButton(this);
                rb.setText(snoozeOptions[i]);
                rb.setTextSize(16);
                rb.setTextColor(darkBlue);
                rb.setChecked(snoozeValues[i] == currentSnooze);
                final int snoozeMinutes = snoozeValues[i];
                rb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        ReminderPreferences.setSnoozeMinutes(AdvancedSettingsActivity.this, snoozeMinutes);
                    }
                });
                snoozeLayout.addView(rb);
            }
            layout.addView(snoozeLayout);

            layout.addView(new Space(this));

            layout.addView(createLabel("📳 Vibration", 20));
            Switch vibration = new Switch(this);
            vibration.setText("Enable vibration on reminders");
            vibration.setTextSize(16);
            vibration.setChecked(ReminderPreferences.isVibrationEnabled(this));
            vibration.setPadding(0, 12, 0, 12);
            vibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ReminderPreferences.setVibrationEnabled(AdvancedSettingsActivity.this, isChecked);
            });
            layout.addView(vibration);

            layout.addView(new Space(this));

            Button backButton = createButton("← Back");
            backButton.setOnClickListener(v -> finish());
            layout.addView(backButton);

            scroll.addView(layout);
            setContentView(scroll);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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