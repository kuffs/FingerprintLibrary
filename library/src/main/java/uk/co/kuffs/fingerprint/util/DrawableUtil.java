package uk.co.kuffs.fingerprint.util;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

public class DrawableUtil {

    public static TransitionDrawable getTransitionDrawable(Context c, @DrawableRes int d1, @DrawableRes int d2) {
        return getTransitionDrawable(c, d1, d2, 1000);
    }

    public static TransitionDrawable getTransitionDrawable(Drawable d1, Drawable d2) {
        return getTransitionDrawable(d1, d2, 1000);
    }

    public static TransitionDrawable getTransitionDrawable(Context c, int d1, int d2, int speed) {
        return getTransitionDrawable(ContextCompat.getDrawable(c, d1), ContextCompat.getDrawable(c, d2), speed);
    }

    public static TransitionDrawable getTransitionDrawable(Drawable d1, Drawable d2, int speed) {
        TransitionDrawable drawable = new TransitionDrawable(new Drawable[]{d1, d2});
        drawable.setCrossFadeEnabled(true);
        drawable.startTransition(speed);
        return drawable;
    }

}
