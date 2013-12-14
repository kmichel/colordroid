package info.kmichel;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class Speaker {

    private final TextToSpeech text_to_speech;

    private boolean is_initialized;
    private boolean is_enabled;
    private String pending_text;

    public Speaker(final Context context, final SpeakerListener listener) {
        text_to_speech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            public void onInit(final int status) {
                if (status == TextToSpeech.SUCCESS) {
                    text_to_speech.setLanguage(Locale.US);
                    is_initialized = true;
                    if (pending_text != null) {
                        speak(pending_text);
                        pending_text = null;
                    }
                } else {
                    if (listener != null)
                        listener.onSpeakerFail();
                }
            }
        });
        is_initialized = false;
        is_enabled = false;
        pending_text = null;

    }

    public void setEnabled(final boolean enabled) {
        is_enabled = enabled;
    }

    public boolean isEnabled() {
        return is_enabled;
    }

    public void speak(final String text) {
        if (is_initialized) {
            if (is_enabled)
                text_to_speech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        } else
            pending_text = text;
    }

    public void shutdown() {
        text_to_speech.shutdown();
    }
}
