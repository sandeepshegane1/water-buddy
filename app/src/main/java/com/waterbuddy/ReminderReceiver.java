package com.waterbuddy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import java.util.Random;

/**
 * Handles water reminder notifications with voice, vibration, and snooze functionality
 */
public class ReminderReceiver extends BroadcastReceiver {

    static final String CHANNEL = "water";
    private static final Random random = new Random();
    private static TextToSpeech tts;

    private static final String[] REMINDERS = {
            "Drink water, cutie! It is sip sip time!",
            "Hydration time! Stay healthy and hydrated!",
            "Time for a water break! 💧",
            "Your body is calling for water!",
            "Keep calm and drink water! 🌊",
            "Cheers! Time to refill your hydration!",
            "Splash! Let's hydrate!",
            "Sip, sip, hooray! Time to drink!"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        SharedPreferences preferences = context.getSharedPreferences("water", Context.MODE_PRIVATE);

        try {
            if ("DRANK".equals(action)) {
                HydrationTracker.addGlass(context);
                HistoryTracker.recordGlasses(context, HydrationTracker.getToday(), HydrationTracker.getGlassesToday(context));

                NotificationManager manager = context.getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.cancel(7);
                }
                return;
            }

            if ("VOICE".equals(action)) {
                boolean voiceOn = !preferences.getBoolean("voice", true);
                preferences.edit().putBoolean("voice", voiceOn).apply();
                showReminder(context);
                return;
            }

            if ("SNOOZE".equals(action)) {
                int snoozeMinutes = ReminderPreferences.getSnoozeMinutes(context);
                scheduleSnoozeReminder(context, snoozeMinutes);
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.cancel(7);
                }
                return;
            }

            if ("REMIND".equals(action)) {
                if (!ReminderPreferences.isInQuietHours(context)) {
                    showReminder(context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static PendingIntent actionIntent(Context context, String action, int id) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(action);

        return PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    static void showReminder(Context context) {
        try {
            SharedPreferences preferences = context.getSharedPreferences("water", Context.MODE_PRIVATE);
            boolean voiceOn = preferences.getBoolean("voice", true);
            String reminderText = REMINDERS[random.nextInt(REMINDERS.length)];

            Notification notification = new Notification.Builder(context, CHANNEL)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("💧 Water Reminder")
                    .setContentText(reminderText)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .addAction(
                            new Notification.Action.Builder(
                                    null,
                                    "I drank water ✓",
                                    actionIntent(context, "DRANK", 11)
                            ).build()
                    )
                    .addAction(
                            new Notification.Action.Builder(
                                    null,
                                    "Snooze 15 min",
                                    actionIntent(context, "SNOOZE", 10)
                            ).build()
                    )
                    .addAction(
                            new Notification.Action.Builder(
                                    null,
                                    voiceOn ? "🔊 Voice: On" : "🔇 Voice: Off",
                                    actionIntent(context, "VOICE", 12)
                            ).build()
                    )
                    .build();

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.notify(7, notification);
            }

            // Vibrate
            if (ReminderPreferences.isVibrationEnabled(context)) {
                vibrateDevice(context);
            }

            // Speak
            if (voiceOn) {
                speakReminder(context, reminderText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void vibrateDevice(Context context) {
        try {
            Vibrator vibrator = context.getSystemService(Vibrator.class);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(300);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void speakReminder(Context context, String text) {
        try {
            if (tts == null) {
                tts = new TextToSpeech(context, status -> {
                    if (status == TextToSpeech.SUCCESS) {
                        if (tts != null) {
                            tts.setPitch(1.65f);
                            tts.setSpeechRate(0.85f);
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                });
            } else {
                if (tts != null) {
                    tts.stop();
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scheduleSnoozeReminder(Context context, int minutes) {
        try {
            android.app.AlarmManager alarmManager = context.getSystemService(android.app.AlarmManager.class);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.setAction("REMIND");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    9999,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.add(java.util.Calendar.MINUTE, minutes);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
