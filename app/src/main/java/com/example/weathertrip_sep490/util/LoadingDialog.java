package com.example.weathertrip_sep490.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weathertrip_sep490.R;

public final class LoadingDialog {

    private static Dialog currentDialog = null;

    private LoadingDialog() {
    }

    private static Activity findActivity(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return findActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    /**
     * Shows the loading dialog with the default message "Đang tải...".
     */
    public static void show(@NonNull Context context) {
        show(context, "Đang tải...");
    }

    /**
     * Shows the loading dialog with a custom message.
     */
    public static synchronized void show(@NonNull Context context, @NonNull String message) {
        Activity activity = findActivity(context);
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        // Dismiss any existing dialog first
        dismissInternal();

        try {
            Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_loading);
            
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            
            TextView tvMessage = dialog.findViewById(R.id.tvLoadingMessage);
            if (tvMessage != null) {
                tvMessage.setText(message);
            }
            
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            
            currentDialog = dialog;
        } catch (Exception ignored) {
        }
    }

    /**
     * Dismisses the current loading dialog if showing.
     */
    public static synchronized void dismiss() {
        dismissInternal();
    }

    private static void dismissInternal() {
        if (currentDialog != null) {
            try {
                if (currentDialog.isShowing()) {
                    Context context = currentDialog.getContext();
                    Activity activity = findActivity(context);
                    if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                        currentDialog.dismiss();
                    }
                }
            } catch (Exception ignored) {
            } finally {
                currentDialog = null;
            }
        }
    }
}
