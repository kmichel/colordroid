package info.kmichel.camera;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SurfaceCameraView extends SurfaceView implements SurfaceHolder.Callback {
    final CameraController camera_controller;
    private SurfaceHolder holder;
    private PreviewViewScaler scaler;

    public SurfaceCameraView(final Context context, final CameraController camera_controller) {
        super(context);
        this.camera_controller = camera_controller;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        holder = getHolder();
        if (holder != null)
            holder.addCallback(this);
        scaler = new PreviewViewScaler(this);
        camera_controller.setPreviewSizeListener(scaler);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (holder != null)
            holder.removeCallback(this);
    }

    @Override
    protected void onMeasure(final int width_measure_spec, final int height_measure_spec) {
        if (scaler != null) {
            final int[] size = scaler.measure(width_measure_spec, height_measure_spec);
            setMeasuredDimension(size[0], size[1]);
        } else {
            setMeasuredDimension(MeasureSpec.getSize(width_measure_spec), MeasureSpec.getSize(height_measure_spec));
        }
    }

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        // We do nothing because it's specified that surfaceChanged will
        // always be called at least once after surfaceCreated
    }

    @Override
    public void surfaceChanged(final SurfaceHolder surfaceHolder, final int format, final int width, final int height) {
        camera_controller.setPreviewDisplay(surfaceHolder);
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {
        camera_controller.setPreviewDisplay(null);
    }
}
