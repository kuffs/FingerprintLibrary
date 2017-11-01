package uk.co.kuffs.fingerprint.util;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;

public class AnimationUtil {

    private static final String TAG = AnimationUtils.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void CircularReveal(Dialog d, int duration, Animator.AnimatorListener listener) {
        View v = d.getWindow().getDecorView();
        v.setVisibility(View.INVISIBLE);
        CircularReveal(v, 300, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void CircularHide(Dialog d, int duration, Animator.AnimatorListener listener) {
        View v = d.getWindow().getDecorView();
        v.setVisibility(View.INVISIBLE);
        CircularHide(v, 300, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void CircularReveal(View view, int duration, Animator.AnimatorListener listener) {
        // get the center for the clipping circle
        try {
            int cx = view.getMeasuredWidth() / 2;
            int cy = view.getMeasuredHeight() / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;

            // create the animator for this view (the start radius is zero)
            view.setVisibility(View.INVISIBLE);
            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
            anim.setInterpolator(new AccelerateInterpolator(1f));
            anim.setDuration(duration);

            // make the view visible and start the animation
            if (listener != null) anim.addListener(listener);

            view.setVisibility(View.VISIBLE);

            anim.start();
        } catch (Exception e) {
            view.setVisibility(View.VISIBLE);
            e.printStackTrace();
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void CircularHide(final View view, int duration, Animator.AnimatorListener listener) {
        try {
            int cx = view.getWidth() / 2;
            int cy = view.getHeight() / 2;

            float initialRadius = (float) Math.hypot(cx, cy);

            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
            anim.setInterpolator(new AccelerateInterpolator(1f));
            anim.setDuration(duration);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    view.setVisibility(View.GONE);
                }
            });

            if (listener != null) anim.addListener(listener);
            anim.start();
        } catch (Exception e) {
            view.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }


}