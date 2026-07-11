package com.waterbuddy;

import android.content.Context;
import android.speech.tts.TextToSpeech;

/**
 * Manages Text-to-Speech announcements for water reminders
 */
public class VoiceManager {
    private static TextToSpeech tts;
    private static boolean isInitialized = false;

    /**
     * Initialize TTS engine
     */
    public static void init(Context context, Runnable onReady) {
        if (isInitialized) {
            onReady.run();
            return;
        }

        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setPitch(1.65f);
                tts.setSpeechRate(0.85f);
                isInitialized = true;
                onReady.run();
            }
        });
    }

    /**
     * Speak reminder message
     */
    public static void speak(String text) {
        if (isInitialized && tts != null) {
            tts.stop();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "water-reminder");
        }
    }

    /**
     * Stop current speech
     */
    public static void stop() {
        if (tts != null) {
            tts.stop();
        }
    }

    /**
     * Shutdown TTS
     */
    public static void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            isInitialized = false;
        }
    }
}
