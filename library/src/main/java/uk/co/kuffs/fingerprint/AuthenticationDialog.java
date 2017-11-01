package uk.co.kuffs.fingerprint;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import uk.co.kuffs.fingerprint.util.AnimationUtil;
import uk.co.kuffs.fingerprint.util.DrawableUtil;
import uk.co.kuffs.fingerprint.util.ViewUtil;

@RequiresApi(api = Build.VERSION_CODES.M)
public class AuthenticationDialog extends AppCompatDialogFragment {

    public static final String TAG = AuthenticationDialog.class.getSimpleName();

    public static final int STATE_ERROR = 1;
    public static final int STATE_OK = 0;

    private View rootView;
    private Api.Callback callBacks;

    private String alias;
    private String encryptedValue;
    private Api api;
    private TextSwitcher switcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);

        alias = getArguments().getString("Alias");
        encryptedValue = getArguments().getString("Value");

    }

    @Override
    public void onResume() {
        super.onResume();

        int px = (int) getResources().getDimension(R.dimen.dialog_width);

        if (getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(px, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Api.Callback)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        callBacks = (Api.Callback) activity;


    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fingerprint, null);

        switcher = rootView.findViewById(R.id.message);
        switcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getActivity());
                t.setGravity(Gravity.CENTER_HORIZONTAL);
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
                return t;
            }
        });

        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        switcher.setInAnimation(in);
        switcher.setOutAnimation(out);
        switcher.setCurrentText(getActivity().getString(R.string.touchSensor));

        setCancelable(true);


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(rootView);

        Dialog d = alertDialogBuilder.create();

        d.setCancelable(true);
        d.setCanceledOnTouchOutside(false);
        if (d.getWindow() != null)
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        d.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    revealDismiss();
                    return true;
                }
                return false;
            }
        });

        d.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                rootView.setVisibility(View.INVISIBLE);
                AnimationUtil.CircularReveal(rootView, 300, null);

                api = Api.getApi(getActivity(), alias);

                api.startListening(encryptedValue, new Api.DecryptedListener() {
                    @Override
                    public void onDecrypted(String value) {
                        callBacks.onAuthenticated(value);
                    }

                    @Override
                    public void onDecryptError(String error) {
                        callBacks.onError(error);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        callBacks.onAuthenticationFailed();
                    }

                    @Override
                    public void onKeyInvalidated() {
                        callBacks.onKeyInvalidated();
                    }
                }, AuthenticationDialog.this);

            }
        });

        return d;

    }

    public void showSnackbar(String message) {
        ViewUtil.getSnackBar(rootView, message).show();
    }

    public void showSnackbar(@StringRes int message) {
        ViewUtil.getSnackBar(rootView, message).show();
    }

    public void delayDismiss(int delay) {
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                revealDismiss();
            }
        }, delay);
    }

    public void revealDismiss() {

        if (rootView != null) {

            AnimationUtil.CircularHide(rootView, 300, new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    dismissAllowingStateLoss();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }

    void setMessage(String message, final String message2) {
        switcher.setText(message);
        if (message2 != null) {
            switcher.postDelayed(new Runnable() {
                @Override
                public void run() {
                    switcher.setText(message2);
                }
            }, 1000);
        }
    }

    void animate(int state) {
        final int speed = 300;

        Context c = getContext();

        if (state == STATE_ERROR) {
            if (c == null) {
                return;
            }

            final Drawable d1 = DrawableUtil.getTransitionDrawable(c, R.drawable.ic_fp_40px, R.drawable.ic_fingerprint_error, speed);
            final Drawable d2 = DrawableUtil.getTransitionDrawable(c, R.drawable.ic_fingerprint_error, R.drawable.ic_fp_40px, speed);

            final ImageView i = rootView.findViewById(R.id.imgFingerprint);
            i.setImageDrawable(d1);
            i.animate().scaleX(0.75f).scaleY(0.75f)
                    .setDuration(speed)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            i.setImageDrawable(d2);
                            i.animate().scaleX(1f).scaleY(1f).setDuration(speed).start();
                        }
                    }).start();
        } else {
            if (c == null) {
                revealDismiss();
                return;
            }

            final Drawable d1 = DrawableUtil.getTransitionDrawable(c, R.drawable.ic_fp_40px, R.drawable.ic_fingerprint_success, speed);
            final Drawable d2 = DrawableUtil.getTransitionDrawable(c, R.drawable.ic_fingerprint_success, R.drawable.ic_fp_40px, speed);

            final ImageView i = rootView.findViewById(R.id.imgFingerprint);
            i.setImageDrawable(d1);
            i.animate().scaleX(1.25f).scaleY(1.25f)
                    .setDuration(speed)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            i.setImageDrawable(d2);
                            i.animate().scaleX(1f).scaleY(1f).setDuration(speed).start();
                            delayDismiss(100);
                            //   callback.Authenticated();
                        }
                    }).start();

        }


    }

    public static void showDialog(FragmentActivity activity, String alias, String encryptedValue) {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        AuthenticationDialog d = new AuthenticationDialog();

        Bundle args = new Bundle();
        args.putString("Alias", alias);
        args.putString("Value", encryptedValue);
        d.setArguments(args);

        d.show(ft, TAG);
    }

}
