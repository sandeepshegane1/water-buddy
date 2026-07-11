package com.waterbuddy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;

public class ReminderReceiver extends BroadcastReceiver {

    static final String CHANNEL = "water";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        SharedPreferences preferences =
                context.getSharedPreferences("water", Context.MODE_PRIVATE);

        if ("DRANK".equals(action)) {
            preferences.edit()
                    .putInt("glasses", preferences.getInt("glasses", 0) + 1)
                    .apply();

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

        showReminder(context);
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

        Notification notification = new Notification.Builder(context, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Drink water, cutie!")
                .setContentText("It is sip sip time!")
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(
                        new Notification.Action.Builder(
                                null,
                                "I drank water",
                                actionIntent(context, "DRANK", 11)
                        ).build()
                )
                .addAction(
                        new Notification.Action.Builder(
                                null,
                                voiceOn ? "Voice: On" : "Voice: Off",
                                actionIntent(context, "VOICE", 12)
                        ).build()
                )
                .build();

        NotificationManager manager =
                context.getSystemService(NotificationManager.class);
        manager.notify(7, notification);

        if (voiceOn) {
            final TextToSpeech[] speech = new TextToSpeech[1];

            speech[0] = new TextToSpeech(context, status -> {
                TextToSpeech tts = speech[0];

                if (status == TextToSpeech.SUCCESS && tts != null) {
                    tts.setPitch(1.65f);
                    tts.setSpeechRate(0.9f);
                    tts.speak(
                            "Drink water, cutie! It is sip sip time!",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "water-reminder"
                    );
                }
            });
        }
    }
}
