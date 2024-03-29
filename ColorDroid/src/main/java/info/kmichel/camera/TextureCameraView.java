package info.kmichel.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.ViewParent;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TextureCameraView extends TextureView implements TextureView.SurfaceTextureListener {
    private static final String TAG = "TextureCameraView";
    // Having a scaleX or scaleY different from 1 avoids nearest-neighbor filtering,
    // thus we scale the view with a random value and then compensate during layout.
    private static final int TEXTURE_FILTERING_HACK_SCALE = 2;

    final CameraController camera_controller;
    private PreviewViewScaler scaler;

    public TextureCameraView(final Context context, final CameraController camera_controller) {
        super(context);
        this.camera_controller = camera_controller;
        setSurfaceTextureListener(this);
        setScaleX(TEXTURE_FILTERING_HACK_SCALE);
        setScaleY(TEXTURE_FILTERING_HACK_SCALE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isHardwareAccelerated()) {
            scaler = new PreviewViewScaler(this);
            camera_controller.setPreviewSizeListener(scaler);
        } else {
            Log.d(TAG, "Window is not hardware accelerated, falling back to SurfaceCameraView");
            final ViewParent view_parent = getParent();
            if (view_parent instanceof ViewGroup) {
                final ViewGroup view_group = (ViewGroup) view_parent;
                final int index = view_group.indexOfChild(this);
                view_group.removeViewInLayout(this);
                final SurfaceCameraView surface_camera_view = new SurfaceCameraView(getContext(), camera_controller);
                final ViewGroup.LayoutParams layout_params = getLayoutParams();
                if (layout_params != null)
                    view_group.addView(surface_camera_view, index, layout_params);
                else
                    view_group.addView(surface_camera_view, index);
            } else {
                throw new IllegalStateException("TextureCameraView must have a non-null ViewGroup as parent");
            }
        }
    }

    @Override
    protected void onMeasure(final int width_measure_spec, final int height_measure_spec) {
        if (scaler != null) {
            final int[] size = scaler.measure(width_measure_spec, height_measure_spec);
            setMeasuredDimension(size[0] / TEXTURE_FILTERING_HACK_SCALE, size[1] / TEXTURE_FILTERING_HACK_SCALE);
        } else {
            setMeasuredDimension(
                    MeasureSpec.getSize(width_measure_spec) / TEXTURE_FILTERING_HACK_SCALE,
                    MeasureSpec.getSize(height_measure_spec) / TEXTURE_FILTERING_HACK_SCALE);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface_texture, final int width, final int height) {
        camera_controller.setPreviewTexture(surface_texture);
    }

    @Override
    public void onSurfaceTextureSizeChanged(final SurfaceTexture surface_texture, final int width, final int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface_texture) {
        camera_controller.setPreviewTexture(null);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(final SurfaceTexture surface_texture) {
    }
}
