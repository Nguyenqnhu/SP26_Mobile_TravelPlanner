package com.example.weathertrip_sep490.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.weathertrip_sep490.R;

public final class AppToast {

    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_INFO = 3;

    private AppToast() {
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

    public static void show(@NonNull Context context, @NonNull String message) {
        int type = TYPE_INFO;
        String msgLower = message.toLowerCase();
        if (msgLower.contains("thành công") || msgLower.contains("đã tạo") || msgLower.contains("đã copy") || msgLower.contains("đã mời") || msgLower.contains("đã tham gia")) {
            type = TYPE_SUCCESS;
        } else if (msgLower.contains("lỗi") || msgLower.contains("thất bại") || msgLower.contains("không") || msgLower.contains("chưa") || msgLower.contains("thiếu") || msgLower.contains("sai")) {
            type = TYPE_ERROR;
        }
        show(context, message, type);
    }

    public static void showSuccess(@NonNull Context context, @NonNull String message) {
        show(context, message, TYPE_SUCCESS);
    }

    public static void showError(@NonNull Context context, @NonNull String message) {
        show(context, message, TYPE_ERROR);
    }

    public static void showInfo(@NonNull Context context, @NonNull String message) {
        show(context, message, TYPE_INFO);
    }

    public static void show(@NonNull Context context, @NonNull String message, int type) {
        Activity activity = findActivity(context);
        if (activity != null) {
            showInternal(activity, message, type);
        } else {
            Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private static void showInternal(@NonNull Activity activity, @NonNull String message, int type) {
        if (activity.isFinishing() || activity.isDestroyed()) return;

        ViewGroup root = activity.findViewById(android.R.id.content);
        if (root == null) return;

        View toastView = LayoutInflater.from(activity).inflate(R.layout.view_app_toast, root, false);
        TextView titleView = toastView.findViewById(R.id.appToastTitle);
        TextView messageView = toastView.findViewById(R.id.appToastMessage);
        ImageView iconView = toastView.findViewById(R.id.appToastIcon);

        messageView.setText(message);
        applyStyle(toastView, titleView, iconView, type);

        removeExistingToast(root);
        root.addView(toastView);

        toastView.setAlpha(0f);
        toastView.setTranslationY(-24f);
        toastView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(260L)
                .start();

        toastView.postDelayed(() -> hideToast(root, toastView), 2000);
    }

    private static void hideToast(@NonNull ViewGroup root, @NonNull View toastView) {
        if (toastView.getParent() != root) return;
        toastView.animate()
                .alpha(0f)
                .translationY(-16f)
                .setDuration(220L)
                .withEndAction(() -> root.removeView(toastView))
                .start();
    }

    private static void removeExistingToast(@NonNull ViewGroup root) {
        View existing = root.findViewWithTag("app_toast_view");
        if (existing != null) {
            root.removeView(existing);
        }
    }

    private static void applyStyle(@NonNull View toastView, @NonNull TextView titleView, @NonNull ImageView iconView, int type) {
        toastView.setTag("app_toast_view");
        int iconRes;
        int accentColor;
        int titleText;
        switch (type) {
            case TYPE_SUCCESS:
                iconRes = android.R.drawable.checkbox_on_background;
                accentColor = Color.parseColor("#34D399");
                titleText = Color.parseColor("#D1FAE5");
                titleView.setText("Thành công");
                break;
            case TYPE_ERROR:
                iconRes = android.R.drawable.ic_delete;
                accentColor = Color.parseColor("#F87171");
                titleText = Color.parseColor("#FEE2E2");
                titleView.setText("Thông báo lỗi");
                break;
            case TYPE_INFO:
            default:
                iconRes = android.R.drawable.ic_dialog_info;
                accentColor = Color.parseColor("#22D3EE");
                titleText = Color.parseColor("#E0F2FE");
                titleView.setText("Thông tin");
                break;
        }

        titleView.setTextColor(titleText);
        iconView.setImageResource(iconRes);
        iconView.setColorFilter(accentColor);
    }
}
