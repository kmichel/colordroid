package info.kmichel.camera;

import android.os.Build;
import android.view.View;

public class PreviewViewScaler implements PreviewSizeListener {

    public final View view;
    private ScalingMode scaling_mode;
    private int preview_width;
    private int preview_height;

    PreviewViewScaler(final View view) {
        this.view = view;
        scaling_mode = ScalingMode.COVER_VIEW;
    }

    public void setScalingMode(final ScalingMode scaling_mode) {
        this.scaling_mode = scaling_mode;
        requestLayoutIfAllowed();
    }

    public int[] measure(final int width_measure_spec, final int height_measure_spec) {
        // TODO: handle padding and wrap_content
        final int view_width = View.MeasureSpec.getSize(width_measure_spec);
        final int view_height = View.MeasureSpec.getSize(height_measure_spec);
        if (scaling_mode == ScalingMode.ANAMORPHIC
                || view_width == 0 || view_height == 0
                || preview_width == 0 || preview_height == 0)
            return new int[]{view_width, view_height};
        else {
            final float camera_ratio = preview_width / (float) preview_height;
            final float view_ratio = view_width / (float) view_height;
            if ((scaling_mode == ScalingMode.CONTAIN_IN_VIEW) ^ (camera_ratio < view_ratio))
                return new int[]{view_width, Math.round(view_width / camera_ratio)};
            else
                return new int[]{Math.round(view_height * camera_ratio), view_height};
        }
    }

    @Override
    public void onPreviewSizeChange(final int width, final int height) {
        preview_width = width;
        preview_height = height;
        requestLayoutIfAllowed();
    }

    private void requestLayoutIfAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            if (view.isInLayout())
                return;
        view.requestLayout();
    }

}
