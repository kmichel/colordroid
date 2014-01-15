package info.kmichel.colordroid;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

public class VolumeChecker {
    private final AudioManager audio_manager;
    private final int stream_type;

    public VolumeChecker(final Activity activity, final int stream_type) {
        audio_manager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        this.stream_type = stream_type;
        activity.setVolumeControlStream(stream_type);
    }

    public boolean is_muted() {
        return audio_manager.getStreamVolume(stream_type) == 0;
    }

    public void check_volume() {
        if (is_muted())
            audio_manager.setStreamVolume(
                    stream_type,
                    audio_manager.getStreamMaxVolume(stream_type) / 2,
                    AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
    }
}
