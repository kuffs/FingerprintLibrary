package uk.co.kuffs.fingerprint.util;


import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import uk.co.kuffs.fingerprint.R;

public class ViewUtil {

    //region [ SnackBar ]

    private static Snackbar _lastSnackBar;

    public static Snackbar getSnackBar(View v, int message) {
        return getSnackBar(v, v.getContext().getString(message));
    }

    public static Snackbar getSnackBar(View v, String message) {
        _lastSnackBar = Snackbar.make(v, message, 3000);
        View vi = _lastSnackBar.getView();
        TextView textView = (TextView) vi.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(3);
        vi.setBackgroundColor(ColorUtil.resolveColor(v.getContext(), R.attr.colorPrimary, Color.BLACK));
        _lastSnackBar.setActionTextColor(ColorUtil.resolveColor(v.getContext(), R.attr.colorPrimaryLight, Color.WHITE));
        return _lastSnackBar;
    }

    public static Snackbar getDarkSnackBar(View v, String message) {
        _lastSnackBar = Snackbar.make(v, message, 3000);
        View vi = _lastSnackBar.getView();
        TextView textView = (TextView) vi.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(3);
        vi.setBackgroundColor(ColorUtil.resolveColor(v.getContext(), R.attr.colorAccent, Color.BLACK));
        _lastSnackBar.setActionTextColor(ColorUtil.resolveColor(v.getContext(), R.attr.colorPrimaryLight, Color.WHITE));
        return _lastSnackBar;
    }

    public static void DismissLastSnackbar() {
        if (_lastSnackBar != null) {
            _lastSnackBar.dismiss();
        }
    }

    //endregion [ SnackBar ]

}
