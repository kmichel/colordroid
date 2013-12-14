package info.kmichel;

import android.os.Handler;

public class Pacer {

    private final int delay_milliseconds;
    private final PacerListener listener;
    private final Handler delay_handler;
    private final Runnable delay_runnable;
    private boolean delay_expired;
    private boolean is_active;
    private String last_text_sent;
    private String pending_text;

    public Pacer(final int delay_milliseconds, final PacerListener listener) {
        this.delay_milliseconds = delay_milliseconds;
        this.listener = listener;
        // TODO: reuse an existing handler ?
        delay_handler = new Handler();
        delay_runnable = new Runnable() {
            @Override
            public void run() {
                delay_expired = true;
                checkPendingText();
            }
        };
        delay_expired = true;
        is_active = true;
        last_text_sent = null;
        pending_text = null;
    }

    public void setText(final String text) {
        pending_text = text;
        checkPendingText();
    }

    public void cancelPendingText() {
        pending_text = null;
    }

    public void pause() {
        is_active = false;
    }

    public void resume() {
        is_active = true;
        checkPendingText();
    }

    private void checkPendingText() {
        if (is_active && delay_expired && pending_text != null && !pending_text.equals(last_text_sent)) {
            listener.onTextChange(pending_text);
            last_text_sent = pending_text;
            delay_expired = false;
            delay_handler.postDelayed(delay_runnable, delay_milliseconds);
        }
    }
}
