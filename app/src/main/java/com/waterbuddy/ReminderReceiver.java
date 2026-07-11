package com.waterbuddy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import java.util.Random;

/**
 * Handles water reminder notifications with voice and actions
 */
public class ReminderReceiver extends BroadcastReceiver {

    static final String CHANNEL = "water";
    private static final Random random = new Random();

    private static final String[] REMINDERS = {
            "Drink water, cutie! It is sip sip time!",
            "Hydration time! Stay healthy and hydrated!",
            "Time for a water break! 💧",
            "Your body is calling for water!",
            "Keep calm and drink water! 🌊",
            "Cheers! Time to refill your hydration!"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        SharedPreferences preferences =
                context.getSharedPreferences("water", Context.MODE_PRIVATE);

        if ("DRANK".equals(action)) {
            HydrationTracker.addGlass(context);

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);
            manager.cancel(7);
            return;
        }

        if ("VOICE".equals(action)) {
            boolean voiceOn = !preferences.getBoolean("voice", true);
            preferences.edit().putBoolean("voice", voiceOn).apply();
            showReminder(context);
            return;
        }

        if ("REMIND".equals(action)) {
            showReminder(context);
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
        SharedPreferences preferences =
                context.getSharedPreferences("water", Context.MODE_PRIVATE);

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
                                voiceOn ? "🔊 Voice: On" : "🔇 Voice: Off",
                                actionIntent(context, "VOICE", 12)
                        ).build()
                )
                .build();

        NotificationManager manager =
                context.getSystemService(NotificationManager.class);
        manager.notify(7, notification);

        if (voiceOn) {
            speakReminder(context, reminderText);
        }
    }

    private static void speakReminder(Context context, String text) {
        final TextToSpeech[] speech = new TextToSpeech[1];

        speech[0] = new TextToSpeech(context, status -> {
            TextToSpeech tts = speech[0];

            if (status == TextToSpeech.SUCCESS && tts != null) {
                tts.setPitch(1.65f);
                tts.setSpeechRate(0.9f);
                tts.speak(
                        text,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "water-reminder"
                );
            }
        });
    }
}
