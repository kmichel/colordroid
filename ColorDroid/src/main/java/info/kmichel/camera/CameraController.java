package info.kmichel.camera;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.SurfaceHolder;

public interface CameraController {
    void setExpectedState(final CameraState camera_state);

    void setPreviewSizeListener(final PreviewSizeListener listener);

    void setPreviewDisplay(final SurfaceHolder holder);

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void setPreviewTexture(final SurfaceTexture texture);

    void setDisplayOrientation(final int rotationAngle);

    void setLightEnabled(final boolean enabled);
}
