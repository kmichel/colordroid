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
        final double[] angles = {.375, .625, 0};
        final double[] x_anchors = {0.25, 0.75, 0.5};
        final double[] y_anchors = {0.25, 0.25, 1};
        final boolean[] use_baseline = {false, false, true};

        final double square_size = 32 * density;
        final double anchor_circle_radius =
                Math.sqrt(view_radius * view_radius + view_radius * view_radius)
                        - Math.sqrt(square_size * square_size + square_size * square_size);

        final int children_count = getChildCount();
        for (int i = 0; i < children_count; ++i) {
            final View child = getChildAt(i);
            if (child == null)
                continue;
            if (child.getVisibility() == GONE)
                continue;
            final double angle_radian = 2 * Math.PI * (angles[i] - 0.25);
            final double x_base = left + circle_center_x + Math.cos(angle_radian) * anchor_circle_radius;
            final double y_base = top + circle_center_y + Math.sin(angle_radian) * anchor_circle_radius;

            final int child_width = child.getMeasuredWidth();
            final int child_height = child.getMeasuredHeight();
            final int virtual_child_height = (use_baseline[i] && child.getBaseline() != -1) ? child.getBaseline() : child_height;
            final int child_left = (int) Math.round(x_base - child_width * x_anchors[i]);
            final int child_top = (int) Math.round(y_base - virtual_child_height * y_anchors[i]);

            child.layout(child_left, child_top, child_left + child_width, child_top + child_height);
        }
    }

    @Override
    protected void onSizeChanged(final int width, final int height, final int old_width, final int old_height) {
        super.onSizeChanged(width, height, old_width, old_height);
        final int minSide = Math.min(width, height);
        circle_center_x = width * 0.5f;
        circle_center_y = height * 0.5f;
        visor_radius = minSide * 0.1f;
        view_radius = minSide * 0.5f - 16.0f * density;

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
