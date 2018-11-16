package com.sogukj.pe.baselibrary.utils;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sogukj.pe.baselibrary.R;


/**
 * Created by admin on 2018/11/14.
 */

public class ToastUtils {
    private static Toast toast;

    private static void showCustomToast(@DrawableRes Integer resId, @NonNull CharSequence text, @NonNull Context context) {
        View inflate = View.inflate(context, R.layout.layout_custom_toast, null);
        ImageView icon = inflate.findViewById(R.id.toast_icon);
        TextView content = inflate.findViewById(R.id.toast_tv);
        if (resId != null) {
            icon.setImageResource(resId);
        } else {
            icon.setVisibility(View.GONE);
        }
        content.setText(text);
        if (toast == null) {
            toast = new Toast(context);
        }
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, -50);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(inflate);
        toast.show();
    }

    public static void showSuccessToast(@NonNull CharSequence text, @NonNull Context context) {
        showCustomToast(R.drawable.icon_toast_success, text, context);
    }

    public static void showSuccessToast(@StringRes int resId, @NonNull Context context) {
        showCustomToast(R.drawable.icon_toast_success, context.getResources().getText(resId), context);
    }

    public static void showErrorToast(@NonNull CharSequence text, @NonNull Context context) {
        showCustomToast(R.drawable.icon_toast_fail, text, context);
    }

    public static void showErrorToast(@StringRes int resId, @NonNull Context context) {
        showCustomToast(R.drawable.icon_toast_fail, context.getResources().getText(resId), context);
    }

    public static void showWarnToast(@NonNull CharSequence text, @NonNull Context context) {
        showCustomToast(R.drawable.icon_toast_common, text, context);
    }

    public static void showWarnToast(@StringRes int resId, @NonNull Context context) {
        showCustomToast(R.drawable.icon_toast_common, context.getResources().getText(resId), context);
    }

}
