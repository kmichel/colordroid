package info.kmichel.colordroid;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ButtonHighlighter {
    public static final int HIGHLIGHT_COUNT = 3;
    public static final int HIGHLIGHT_INTERVAL_MS = 100;

    private final Context context;
    private final RelativeLayout view;
    private final int animation_id;

    public ButtonHighlighter(final Context context, final RelativeLayout view, final int animation_id) {
        this.context = context;
        this.view = view;
        this.animation_id = animation_id;
    }

    public void highlightButton(final CompoundButton button, final Drawable highlight_drawable) {
        final RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(
                button.getWidth() - button.getPaddingLeft() - button.getPaddingRight(),
                button.getHeight() - button.getPaddingTop() - button.getPaddingBottom());
        layout_params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layout_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layout_params.setMargins(button.getLeft() + button.getPaddingLeft(), button.getTop() + button.getPaddingTop(), 0, 0);

        // This allows using a StateDrawable aka. <selector> and have different highlights for checked/unchecked states
        final boolean enabled = button.isChecked();

        final Runnable start_highlight = new Runnable() {
            @Override
            public void run() {
                final ImageView highlight_view = new ImageView(context);
                highlight_view.setEnabled(enabled);
                highlight_view.setImageDrawable(highlight_drawable);
                view.addView(highlight_view, layout_params);
                final Animation highlight_animation = AnimationUtils.loadAnimation(context, animation_id);
                if (highlight_animation != null) {
                    highlight_animation.setAnimationListener(new AnimatedViewRemover(view, highlight_view));
                    highlight_view.startAnimation(highlight_animation);
                }
            }
        };

        for (long i = 0; i < HIGHLIGHT_COUNT; ++i)
            view.postDelayed(start_highlight, i * HIGHLIGHT_INTERVAL_MS);
    }
}
