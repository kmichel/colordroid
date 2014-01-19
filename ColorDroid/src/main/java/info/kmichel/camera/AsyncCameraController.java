package info.kmichel.camera;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

public class AsyncCameraController implements CameraController {
    private final Handler main_handler;
    private Handler camera_handler;
    private PreviewSizeListener preview_size_listener;

    public AsyncCameraController(final CameraListener camera_listener) {
        main_handler = new Handler(new MainThreadHandler(camera_listener));
    }

    public void setExpectedState(final CameraState camera_state) {
        if (camera_handler == null)
            start();
        camera_handler.sendMessage(camera_handler.obtainMessage(CameraThreadHandler.MESSAGE_SET_CAMERA_STATE, camera_state));
    }

    public void setPreviewSizeListener(final PreviewSizeListener listener) {
        preview_size_listener = listener;
        if (camera_handler == null)
            start();
        final Message message = camera_handler.obtainMessage(CameraThreadHandler.MESSAGE_SET_PREVIEW_SIZE_LISTENER);
        message.obj = new PreviewSizeListener() {
            @Override
            public void onPreviewSizeChange(final int width, final int height) {
                main_handler.sendMessage(main_handler.obtainMessage(MainThreadHandler.MESSAGE_PREVIEW_SIZE_CHANGE, width, height));
            }
        };
        camera_handler.sendMessage(message);
    }

    public void setPreviewDisplay(final SurfaceHolder holder) {
        if (camera_handler == null)
            start();
        camera_handler.sendMessage(camera_handler.obtainMessage(CameraThreadHandler.MESSAGE_SET_PREVIEW_DISPLAY, holder));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setPreviewTexture(final SurfaceTexture texture) {
        if (camera_handler == null)
            start();
        camera_handler.sendMessage(camera_handler.obtainMessage(CameraThreadHandler.MESSAGE_SET_PREVIEW_TEXTURE, texture));
    }

    public void setDisplayOrientation(final int rotationAngle) {
        if (camera_handler == null)
            start();
        camera_handler.sendMessage(camera_handler.obtainMessage(CameraThreadHandler.MESSAGE_SET_DISPLAY_ORIENTATION, rotationAngle, 0));
    }

    public void setLightEnabled(final boolean enabled) {
        if (camera_handler == null)
            start();
        camera_handler.sendMessage(camera_handler.obtainMessage(CameraThreadHandler.MESSAGE_SET_LIGHT_ENABLED, enabled ? 1 : 0, 0));
    }

    private void start() {
        final HandlerThread handler_thread = new HandlerThread("Camera Thread");
        handler_thread.start();
        final Looper looper = handler_thread.getLooper();
        if (looper == null)
            throw new IllegalStateException("Looper should not be null after thread start");
        camera_handler = new Handler(looper, new CameraThreadHandler(new CameraListener() {
            @Override
            public void onCameraStart(final boolean light_supported) {
                main_handler.sendMessage(main_handler.obtainMessage(MainThreadHandler.MESSAGE_CAMERA_START, light_supported ? 1 : 0, 0));
            }

            @Override
            public void onCameraStartRunning() {
                main_handler.sendMessage(main_handler.obtainMessage(MainThreadHandler.MESSAGE_CAMERA_START_RUNNING));
            }

            @Override
            public void onFirstImage() {
                main_handler.sendMessage(main_handler.obtainMessage(MainThreadHandler.MESSAGE_FIRST_IMAGE));
            }

            @Override
            public void onImageChange(final NV21Buffer buffer) {
                main_handler.sendMessage(main_handler.obtainMessage(MainThreadHandler.MESSAGE_IMAGE_CHANGE, buffer));
            }

            @Override
            public void onCameraStopRunning() {
                main_handler.sendMessage(main_handler.obtainMessage(MainThreadHandler.MESSAGE_CAMERA_STOP_RUNNING));
            }
        }));
    }

    private static class CameraThreadHandler implements Handler.Callback {
        static final int MESSAGE_SET_CAMERA_STATE = 1;
        static final int MESSAGE_SET_PREVIEW_SIZE_LISTENER = 2;
        static final int MESSAGE_SET_PREVIEW_DISPLAY = 3;
        static final int MESSAGE_SET_PREVIEW_TEXTURE = 4;
        static final int MESSAGE_SET_DISPLAY_ORIENTATION = 5;
        static final int MESSAGE_SET_LIGHT_ENABLED = 6;

        private final SimpleCameraController camera_controller;

        CameraThreadHandler(final CameraListener listener) {
            camera_controller = new SimpleCameraController(listener);
        }

        @Override
        public boolean handleMessage(final Message message) {
            switch (message.what) {
                case MESSAGE_SET_CAMERA_STATE:
                    camera_controller.setExpectedState((CameraState) message.obj);
                    return true;
                case MESSAGE_SET_PREVIEW_SIZE_LISTENER:
                    camera_controller.setPreviewSizeListener((PreviewSizeListener) message.obj);
                    return true;
                case MESSAGE_SET_PREVIEW_DISPLAY:
                    camera_controller.setPreviewDisplay((SurfaceHolder) message.obj);
                    return true;
                case MESSAGE_SET_PREVIEW_TEXTURE:
                    camera_controller.setPreviewTexture((SurfaceTexture) message.obj);
                    return true;
                case MESSAGE_SET_DISPLAY_ORIENTATION:
                    camera_controller.setDisplayOrientation(message.arg1);
                    return true;
                case MESSAGE_SET_LIGHT_ENABLED:
                    camera_controller.setLightEnabled(message.arg1 != 0);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class MainThreadHandler implements Handler.Callback {
        static final int MESSAGE_CAMERA_START = 1;
        static final int MESSAGE_CAMERA_START_RUNNING = 2;
        static final int MESSAGE_FIRST_IMAGE = 3;
        static final int MESSAGE_IMAGE_CHANGE = 4;
        static final int MESSAGE_CAMERA_STOP_RUNNING = 5;
        static final int MESSAGE_PREVIEW_SIZE_CHANGE = 6;

        private final CameraListener camera_listener;

        MainThreadHandler(final CameraListener camera_listener) {
            this.camera_listener = camera_listener;
        }

        @Override
        public boolean handleMessage(final Message message) {
            switch (message.what) {
                case MESSAGE_CAMERA_START:
                    camera_listener.onCameraStart(message.arg1 != 0);
                    return true;
                case MESSAGE_CAMERA_START_RUNNING:
                    camera_listener.onCameraStartRunning();
                    return true;
                case MESSAGE_FIRST_IMAGE:
                    camera_listener.onFirstImage();
                    return true;
                case MESSAGE_IMAGE_CHANGE:
                    camera_listener.onImageChange((NV21Buffer) message.obj);
                    return true;
                case MESSAGE_CAMERA_STOP_RUNNING:
                    camera_listener.onCameraStopRunning();
                    return true;
                case MESSAGE_PREVIEW_SIZE_CHANGE:
                    preview_size_listener.onPreviewSizeChange(message.arg1, message.arg2);
                default:
                    return false;
            }
        }
    }

}
