package info.kmichel.colordroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class VisorView extends ViewGroup {

    private final Paint paint;
    private float visor_radius;
    private float view_radius;
    private float circle_center_x;
    private float circle_center_y;
    private final float density;
    private RadialGradient gradient;

    public VisorView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);

        final Resources resources = getResources();
        if (resources != null)
            density = resources.getDisplayMetrics().density;
        else
            density = 1;
        setWillNotDraw(false);
        // TODO: test perfs with setLayerType
    }

    private static int negociateSize(final int measure_spec, final int suggested_minimum) {
        switch (MeasureSpec.getMode(measure_spec)) {
            case MeasureSpec.EXACTLY:
                return MeasureSpec.getSize(measure_spec);
            case MeasureSpec.AT_MOST:
                return Math.min(MeasureSpec.getSize(measure_spec), suggested_minimum);
            case MeasureSpec.UNSPECIFIED:
                return suggested_minimum;
            default:
                return 0;
        }
    }

    private static int negociateChildSize(final int size, final int parent_size) {
        switch (size) {
            case LayoutParams.MATCH_PARENT:
                return MeasureSpec.makeMeasureSpec(parent_size, MeasureSpec.EXACTLY);
            case LayoutParams.WRAP_CONTENT:
                return MeasureSpec.makeMeasureSpec(parent_size, MeasureSpec.AT_MOST);
            default:
                return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        }
    }

    @Override
    protected void onMeasure(final int width_measure_spec, final int height_measure_spec) {
        final int width = negociateSize(width_measure_spec, getSuggestedMinimumWidth());
        final int height = negociateSize(height_measure_spec, getSuggestedMinimumHeight());
        setMeasuredDimension(width, height);

        final int children_count = getChildCount();
        for (int i = 0; i < children_count; ++i) {
            final View child = getChildAt(i);
            if (child == null)
                continue;
            if (child.getVisibility() == GONE)
                continue;

            final LayoutParams child_layout_params = child.getLayoutParams();
            if (child_layout_params == null)
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            else
                child.measure(negociateChildSize(child_layout_params.width, width), negociateChildSize(child_layout_params.height, height));
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        final double view_margin = 16 * density;
        final double[] angles = {.375, .625, 0};
        final double[] x_anchors = {0.25, 0.75, 0.5};
        final double[] y_anchors = {0.25, 0.25, 1};
        final double[] radial_align = {1.0, 1.0, 0.0};
        final boolean[] use_baseline = {false, false, true};

        final int available_width = getMeasuredWidth();
        final int available_height = getMeasuredHeight();

        final int children_count = getChildCount();
        final double[] distances = new double[children_count];
        final double[] x_slopes = new double[children_count];
        final double[] y_slopes = new double[children_count];
        final double[] x_offsets = new double[children_count];
        final double[] y_offsets = new double[children_count];
        double min_distance = Math.min(available_width, available_height) * 0.5;

        for (int i = 0; i < children_count; ++i) {
            final View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE)
                continue;

            final int child_width = child.getMeasuredWidth();
            final int child_height = child.getMeasuredHeight();
            final int anchoring_child_height = use_baseline[i] && child.getBaseline() != -1 ? child.getBaseline() : child_height;

            final double child_left_extra = child_width * x_anchors[i];
            final double child_top_extra = anchoring_child_height * y_anchors[i];
            final double child_right_extra = child_width - child_left_extra;
            final double child_bottom_extra = child_height - child_top_extra;

            final double min_left = child_left_extra - circle_center_x;
            final double min_top = child_top_extra - circle_center_y;
            final double max_right = circle_center_x - child_right_extra;
            final double max_bottom = circle_center_y - child_bottom_extra;

            final double angle_radian = 2 * Math.PI * (angles[i] - 0.25);
            final double x_slope = Math.cos(angle_radian);
            final double y_slope = Math.sin(angle_radian);

            final double max_x_distance = (x_slope >= 0 ? max_right : min_left) / x_slope;
            final double max_y_distance = (y_slope >= 0 ? max_bottom : min_top) / y_slope;

            final double max_distance = Math.min(max_x_distance, max_y_distance);
            min_distance = Math.min(min_distance, max_distance);

            distances[i] = max_distance;
            x_slopes[i] = x_slope;
            y_slopes[i] = y_slope;
            x_offsets[i] = child_left_extra;
            y_offsets[i] = child_top_extra;
        }

        for (int i = 0; i < children_count; ++i) {
            final View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE)
                continue;

            final double distance = min_distance + (distances[i] - min_distance) * radial_align[i];
            final int child_left = (int) Math.round(circle_center_x + distance * x_slopes[i] - x_offsets[i]);
            final int child_top = (int) Math.round(circle_center_y + distance * y_slopes[i] - y_offsets[i]);
            child.layout(child_left, child_top, child_left + child.getMeasuredWidth(), child_top + child.getMeasuredHeight());
        }

        view_radius = (float) (min_distance - view_margin);
        visor_radius = view_radius * 0.2f;
    }

    @Override
    protected void onSizeChanged(final int width, final int height, final int old_width, final int old_height) {
        super.onSizeChanged(width, height, old_width, old_height);
        final int minSide = Math.min(width, height);
        circle_center_x = width * 0.5f;
        circle_center_y = height * 0.5f;

        gradient = new RadialGradient(circle_center_x, circle_center_y, minSide,
                new int[]{Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK}, null, Shader.TileMode.CLAMP);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        paint.setARGB(255, 255, 255, 255);
        paint.setShader(gradient);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setARGB(255, 255, 255, 255);
        paint.setStrokeWidth(1 * density);
        canvas.drawCircle(circle_center_x, circle_center_y, view_radius - 0.5f * density, paint);

        paint.setARGB(64, 255, 255, 255);
        paint.setStrokeWidth(6 * density);
        canvas.drawCircle(circle_center_x, circle_center_y, view_radius - 6 * density, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(48, 255, 255, 255);
        canvas.drawCircle(circle_center_x, circle_center_y, visor_radius, paint);

        paint.setARGB(16, 0, 0, 0);
        canvas.drawCircle(circle_center_x, circle_center_y, 1.5f * density, paint);

        paint.setARGB(255, 255, 255, 255);
        canvas.drawCircle(circle_center_x, circle_center_y, 1.0f * density, paint);


    }
}
