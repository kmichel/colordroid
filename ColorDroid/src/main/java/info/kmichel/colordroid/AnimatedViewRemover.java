package info.kmichel.colordroid;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

class AnimatedViewRemover implements Animation.AnimationListener {
    private final ViewGroup parent;
    private final View child;

    AnimatedViewRemover(final ViewGroup parent, final View child) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    public void onAnimationStart(final Animation animation) {

    }

    @Override
    public void onAnimationEnd(final Animation animation) {
        parent.post(new Runnable() {
            @Override
            public void run() {
                parent.removeView(child);
            }
        });
    }

    @Override
    public void onAnimationRepeat(final Animation animation) {

    }
}
