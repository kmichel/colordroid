package info.kmichel.colordroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import info.kmichel.MovementDetector;
import info.kmichel.Pacer;
import info.kmichel.PacerListener;
import info.kmichel.Speaker;
import info.kmichel.SpeakerListener;
import info.kmichel.StrictModeHelper;
import info.kmichel.StrictModeSeverity;
import info.kmichel.camera.CameraController;
import info.kmichel.camera.CameraListener;
import info.kmichel.camera.NV21Buffer;
import info.kmichel.camera.SurfaceCameraView;
import info.kmichel.camera.TextureCameraView;

public class MeasureActivity extends Activity implements
        CameraListener,
        MovementDetector.MovementListener,
        SpeakerListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private ColorDetector color_detector;
    private MovementDetector movement_detector;
    private VolumeChecker volume_checker;
    private Speaker speaker;
    private Pacer pacer;
    private CameraController camera_controller;

    private static int getRotationAngle(final int rotation_code) {
        switch (rotation_code) {
            default:
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new StrictModeHelper(this).setSeverity(StrictModeSeverity.KILL_ON_VIOLATE);
        setContentView(R.layout.activity_measure);

        color_detector = new ColorDetector();
        color_detector.loadMunsellData(this);
        movement_detector = new MovementDetector(this, 200, this);
        volume_checker = new VolumeChecker(this, TextToSpeech.Engine.DEFAULT_STREAM);
        speaker = new Speaker(this, this);
        camera_controller = new CameraController(this);
        camera_controller.setDisplayOrientation(getRotationAngle(getWindowManager().getDefaultDisplay().getRotation()));

        final TextView text_view = (TextView) findViewById(R.id.colorName);
        pacer = new Pacer(500, new PacerListener() {
            public void onTextChange(final String text) {
                text_view.setText(text);
                speaker.speak(text);
            }
        });

        final ViewGroup camera_preview = (ViewGroup) findViewById(R.id.cameraPreview);
        final FrameLayout.LayoutParams layout_params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            camera_preview.addView(new TextureCameraView(this, camera_controller), 0, layout_params);
        else
            camera_preview.addView(new SurfaceCameraView(this, camera_controller), 0, layout_params);

        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        onSharedPreferenceChanged(preferences, "enable_light_toggle");
        bindToggleToPreference((CompoundButton) findViewById(R.id.speechButton), preferences, "enable_speech");
        bindToggleToPreference((CompoundButton) findViewById(R.id.lightButton), preferences, "enable_light");
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onConfigurationChanged(final Configuration configuration) {
        super.onConfigurationChanged(configuration);
        camera_controller.setDisplayOrientation(getRotationAngle(getWindowManager().getDefaultDisplay().getRotation()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (movement_detector != null)
            movement_detector.start();
        camera_controller.setExpectedState(CameraController.CameraState.CAMERA_RUNNING);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (movement_detector != null)
            movement_detector.stop();
        if (pacer != null)
            pacer.cancelPendingText();
        camera_controller.setExpectedState(CameraController.CameraState.CAMERA_STOPPED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speaker.shutdown();
        getPreferences(MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void onLightEnabledChange(final boolean enabled) {
        final Checkable light_button = (Checkable) findViewById(R.id.lightButton);
        if (light_button != null)
            light_button.setChecked(enabled);
        camera_controller.setLightEnabled(enabled);
    }

    private void onLightToggleEnabledChange(final boolean show) {
        final View light_button = findViewById(R.id.lightButton);
        if (light_button != null)
            light_button.setEnabled(show);
    }

    private void onSpeechEnabledChange(final boolean enabled) {
        final Checkable speech_button = (Checkable) findViewById(R.id.speechButton);
        if (speech_button != null)
            speech_button.setChecked(enabled);
        speaker.setEnabled(enabled);
        if (enabled)
            volume_checker.check_volume();
    }

    @Override
    public void onCameraStart() {
        final boolean light_supported = camera_controller.isLightSupported();
        final View light_button = findViewById(R.id.lightButton);
        if (light_button != null)
            light_button.setEnabled(light_supported);
        setBooleanPreference("enable_light_toggle", light_supported);
    }

    @Override
    public void onCameraStartRunning() {
    }

    @Override
    public void onFirstImage() {
        final Window window = getWindow();
        if (window != null)
            window.setBackgroundDrawable(null);
    }

    @Override
    public void onCameraStopRunning() {
        final Window window = getWindow();
        if (window != null)
            window.setBackgroundDrawableResource(android.R.color.black);
    }

    @Override
    public void onImageChange(final NV21Buffer image) {
        pacer.setText(color_detector.detect_color(image));
    }

    @Override
    public void onMoveStart() {
        pacer.pause();
    }

    @Override
    public void onMoveStop() {
        pacer.resume();
    }

    @Override
    public void onSpeakerFail() {
        Toast.makeText(this, "Speech initialization failed", Toast.LENGTH_LONG).show();
        findViewById(R.id.speechButton).setEnabled(false);
    }

    public void openSettings(final View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            startActivity(new Intent(this, SettingsActivity.class));
        else
            startActivity(new Intent(this, DeprecatedSettingsActivity.class));
    }

    private void bindToggleToPreference(final CompoundButton button, final SharedPreferences preferences, final String key) {
        if (button != null)
            button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton checked_button, final boolean checked) {
                    setBooleanPreference(key, checked);
                }
            });
        new HandlePreferenceEvent().execute(key, "init");
    }

    private class HandlePreferenceEvent extends AsyncTask<String, Void, Boolean> {
        private String key;
        private String event;

        @Override
        protected Boolean doInBackground(final String... strings) {
            key = strings[0];
            event = strings[1];
            return getPreferences(MODE_PRIVATE).getBoolean(key, false);
        }

        @Override
        protected void onPostExecute(final Boolean value) {
            if ("enable_light".equals(key))
                onLightEnabledChange(value);
            if ("enable_light_toggle".equals(key))
                onLightToggleEnabledChange(value);
            if ("enable_speech".equals(key)) {
                if (event.equals("init") && value && volume_checker.is_muted())
                    setBooleanPreference("enable_speech", false);
                else
                    onSpeechEnabledChange(value);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
        new HandlePreferenceEvent().execute(key, "change");
    }

    private void setBooleanPreference(final String key, final boolean value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putBoolean(key, value);
                editor.commit();
            }
        }).start();
    }

}
