package info.kmichel.camera;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleCameraController implements CameraController {
    private static final String TAG = "SimpleCameraController";

    private final CameraListener listener;
    private Camera camera;
    private CameraState camera_state;
    private CameraState expected_state;
    private boolean light_enabled;
    private SurfaceHolder surface_holder;
    private SurfaceTexture surface_texture;
    private PreviewSizeListener preview_size_listener;
    private int final_orientation;
    private boolean waiting_for_first_frame;
    private NV21Buffer nv21_buffer;

    public SimpleCameraController(final CameraListener listener) {
        camera_state = CameraState.CAMERA_STOPPED;
        expected_state = CameraState.CAMERA_STOPPED;
        this.listener = listener;
    }

    public void setExpectedState(final CameraState expected_state) {
        if (this.expected_state != expected_state) {
            Log.d(TAG, "Asking transition: " + this.expected_state + " -> " + expected_state);
            this.expected_state = expected_state;
            update();
        }
    }

    public void setDisplayOrientation(final int display_orientation) {
        final int camera_orientation;
        final boolean camera_is_facing_front;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            final Camera.CameraInfo camera_info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(getBestCameraId(), camera_info);
            camera_orientation = camera_info.orientation;
            camera_is_facing_front = (camera_info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            // TODO: test the application on a Froyo device
            camera_orientation = 90;
            camera_is_facing_front = false;
        }
        if (camera_is_facing_front)
            final_orientation = (360 - (camera_orientation + display_orientation) % 360) % 360;
        else
            final_orientation = (camera_orientation - display_orientation + 360) % 360;

        if (final_orientation % 90 != 0) {
            Log.e(TAG, "Ignoring invalid orientation: " + final_orientation);
            return;
        }

        if (camera_state != CameraState.CAMERA_STOPPED) {
            signalPreviewSize();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                camera.setDisplayOrientation(final_orientation);
            else if (camera_state == CameraState.CAMERA_RUNNING) {
                stopRunningCamera();
                update();
            }
        }
    }

    public void setPreviewSizeListener(final PreviewSizeListener preview_size_listener) {
        this.preview_size_listener = preview_size_listener;
        if (camera_state != CameraState.CAMERA_STOPPED)
            signalPreviewSize();
    }

    public void setPreviewDisplay(final SurfaceHolder holder) {
        surface_holder = holder;
        surface_texture = null;
        if (camera_state == CameraState.CAMERA_RUNNING)
            stopRunningCamera();
        else
            update();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setPreviewTexture(final SurfaceTexture texture) {
        surface_texture = texture;
        surface_holder = null;
        if (camera_state == CameraState.CAMERA_RUNNING)
            stopRunningCamera();
        else
            update();
    }

    public void setLightEnabled(final boolean enabled) {
        light_enabled = enabled;
        updateLight();
    }

    private static Camera openCamera() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                final int camera_id = getBestCameraId();
                if (camera_id != -1)
                    return Camera.open(camera_id);
            } else
                return Camera.open();
        } catch (final RuntimeException e) {
            Log.e(TAG, "Can\'t open camera", e);
        }
        return null;
    }

    private void startCamera() {
        camera = openCamera();
        if (camera != null) {
            final Camera.Parameters camera_parameters = camera.getParameters();
            setupCameraParameters(camera_parameters);
            try {
                camera.setParameters(camera_parameters);
            } catch (final RuntimeException e) {
                Log.e(TAG, "Failed setting camera parameters", e);
                return;
            }
            debugCamera(camera.getParameters());
            signalPreviewSize();
            if (listener != null)
                listener.onCameraStart(isLightSupported());
            setCameraState(CameraState.CAMERA_STARTED);
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static int getBestCameraId() {
        final int camera_count = Camera.getNumberOfCameras();
        final Camera.CameraInfo camera_info = new Camera.CameraInfo();
        for (int camera_id = 0; camera_id < camera_count; ++camera_id) {
            Camera.getCameraInfo(camera_id, camera_info);
            if (camera_info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                return camera_id;
        }
        // If we found no back facing camera, fall back on front facing one
        // or return -1 if we don't have camera
        return camera_count - 1;
    }

    private void stopCamera() {
        camera.release();
        camera = null;
        setCameraState(CameraState.CAMERA_STOPPED);
    }

    private void startRunningCamera() {
        if (surface_holder == null && surface_texture == null)
            return;
        final Camera.Parameters parameters = camera.getParameters();
        final Camera.Size size = parameters.getPreviewSize();
        if (size != null) {
            nv21_buffer = new NV21Buffer(size.width, size.height);
            camera.addCallbackBuffer(nv21_buffer.data);
        } else {
            Log.e(TAG, "No defined preview size, can't start running camera");
            return;
        }
        camera.setDisplayOrientation(final_orientation);
        camera.setPreviewCallbackWithBuffer(preview_callback);
        try {
            if (surface_texture != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    camera.setPreviewTexture(surface_texture);
            } else
                camera.setPreviewDisplay(surface_holder);
        } catch (final IOException e) {
            Log.e(TAG, "Error setting preview texture or display", e);
            return;
        }
        waiting_for_first_frame = true;
        camera.startPreview();
        if (listener != null)
            listener.onCameraStartRunning();
        setCameraState(CameraState.CAMERA_RUNNING);
    }

    private void signalPreviewSize() {
        final Camera.Parameters camera_parameters = camera.getParameters();
        final Camera.Size preview_size = camera_parameters.getPreviewSize();
        if (preview_size != null && preview_size_listener != null) {
            if (final_orientation == 90 || final_orientation == 270)
                //noinspection SuspiciousNameCombination
                preview_size_listener.onPreviewSizeChange(preview_size.height, preview_size.width);
            else
                preview_size_listener.onPreviewSizeChange(preview_size.width, preview_size.height);
        }
    }

    private void stopRunningCamera() {
        if (listener != null)
            listener.onCameraStopRunning();
        camera.stopPreview();
        camera.setPreviewCallbackWithBuffer(null);
        nv21_buffer = null;
        setCameraState(CameraState.CAMERA_STARTED);
    }

    private void setCameraState(final CameraState camera_state) {
        Log.d(TAG, "Transitioning: " + this.camera_state + " -> " + camera_state);
        this.camera_state = camera_state;
        update();
    }

    private void update() {
        if (camera_state == expected_state)
            return;
        Log.d(TAG, "Trying transition: " + camera_state + " -> " + expected_state);
        switch (camera_state) {
            case CAMERA_STOPPED:
                startCamera();
                break;
            case CAMERA_STARTED:
                switch (expected_state) {
                    case CAMERA_STOPPED:
                        stopCamera();
                        break;
                    case CAMERA_RUNNING:
                        startRunningCamera();
                        break;
                }
                break;
            case CAMERA_RUNNING:
                stopRunningCamera();
        }
        updateLight();
    }

    private boolean isLightSupported() {
        if (camera != null) {
            final Camera.Parameters parameters = camera.getParameters();
            return contains(parameters.getSupportedFlashModes(), Camera.Parameters.FLASH_MODE_TORCH);
        }
        return false;
    }

    private void updateLight() {
        if (!isLightSupported())
            return;
        if (camera_state != CameraState.CAMERA_STOPPED) {
            try {
                final Camera.Parameters parameters = camera.getParameters();
                final boolean light_actually_enabled = Camera.Parameters.FLASH_MODE_TORCH.equals(parameters.getFlashMode());
                if (light_enabled && !light_actually_enabled) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);
                }
                if (!light_enabled && light_actually_enabled) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                }
            } catch (final RuntimeException e) {
                Log.e(TAG, "Failed setting camera parameters", e);
            }
        }
    }

    private static void setupCameraParameters(final Camera.Parameters parameters) {
        final List<Camera.Size> preview_sizes = parameters.getSupportedPreviewSizes();
        if (preview_sizes != null) {
            final Camera.Size largest_preview_size = getLargestSize(preview_sizes);
            if (largest_preview_size != null)
                parameters.setPreviewSize(largest_preview_size.width, largest_preview_size.height);
        }

        parameters.setPreviewFormat(ImageFormat.NV21);

        if (contains(parameters.getSupportedWhiteBalance(), Camera.Parameters.WHITE_BALANCE_AUTO))
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            if (contains(parameters.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
            if (contains(parameters.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            final List<Camera.Area> important_areas = new ArrayList<Camera.Area>();
            important_areas.add(new Camera.Area(new Rect(450, 450, 550, 550), 1000));
            if (parameters.getMaxNumFocusAreas() > 0)
                parameters.setFocusAreas(important_areas);
            if (parameters.getMaxNumMeteringAreas() > 0)
                parameters.setMeteringAreas(important_areas);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            final List<int[]> fps_ranges = parameters.getSupportedPreviewFpsRange();
            if (fps_ranges != null) {
                int[] min_range = null;
                for (final int[] range : fps_ranges)
                    if (min_range == null || range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] > min_range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX])
                        min_range = range;
                if (min_range != null)
                    parameters.setPreviewFpsRange(
                            min_range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                            min_range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
            }
        }

        // TODO: test capabilities before settings those parameters
        parameters.set("skinToneEnhancement", "disable");
        parameters.set("denoise", "denoise-off");
        parameters.set("mce", "disable");
        parameters.set("selectable-zone-af", "spot-metering");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            if (parameters.isVideoStabilizationSupported())
                parameters.setVideoStabilization(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            parameters.setRecordingHint(true);
    }

    private static void debugCamera(final Camera.Parameters parameters) {
        final String parameters_string = parameters.flatten();
        final String[] pairs = parameters_string.split(";");
        final List<String> pairs2 = new ArrayList<String>();
        Collections.addAll(pairs2, pairs);
        Collections.sort(pairs2);
        for (final String parameter : pairs2)
            Log.v(TAG, "Param: " + parameter);
    }

    private static Camera.Size getLargestSize(final List<Camera.Size> sizes) {
        int max_area = 0;
        Camera.Size max_size = null;
        for (final Camera.Size size : sizes)
            if (size.width * size.height > max_area) {
                max_area = size.width * size.height;
                max_size = size;
            }
        return max_size;
    }

    // TODO: implement it with parent class
    private final Camera.PreviewCallback preview_callback = new Camera.PreviewCallback() {
        long previous_frame_end_time = System.nanoTime();
        int tick;

        @Override
        public void onPreviewFrame(final byte[] bytes, final Camera camera) {
            final long frame_start_time = System.nanoTime();
            if (waiting_for_first_frame) {
                if (listener != null)
                    listener.onFirstImage();
                waiting_for_first_frame = false;
            }
            if (listener != null && nv21_buffer.data == bytes)
                listener.onImageChange(nv21_buffer);
            camera.addCallbackBuffer(bytes);

            final long frame_end_time = System.nanoTime();
            final long frame_length = frame_end_time - frame_start_time;
            final long repeat_interval = frame_end_time - previous_frame_end_time;
            if (tick % 100 == 0)
                Log.v(TAG, "Self time: " + (frame_length / 1000000) + " ms , Repeat time: " + (repeat_interval / 1000000) + "ms");
            previous_frame_end_time = frame_end_time;
            ++tick;
        }
    };

    private static boolean contains(final List<String> list, final String value) {
        return list != null && list.contains(value);
    }
}
