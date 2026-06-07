package com.example.weathertrip_sep490.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Hiệu ứng khi chạm (hover/press): scale nhỏ lại rồi bật lại.
 */
public final class ViewAnimationUtil {

    private static final float SCALE_PRESSED = 0.96f;
    private static final long DURATION_PRESS = 80L;
    private static final long DURATION_RELEASE = 120L;

    private ViewAnimationUtil() {}

    /**
     * Gắn hiệu ứng scale khi nhấn/thả cho view (button, TextView clickable...).
     */
    public static void setTouchScaleAnimation(View view) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animateScale(v, SCALE_PRESSED, DURATION_PRESS);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animateScale(v, 1f, DURATION_RELEASE);
                    break;
            }
            return false; // không consume event để click vẫn chạy
        });
    }

    private static void animateScale(View view, float scale, long duration) {
        view.animate().cancel();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, scale);
        scaleX.setDuration(duration);
        scaleY.setDuration(duration);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.start();
    }
}
