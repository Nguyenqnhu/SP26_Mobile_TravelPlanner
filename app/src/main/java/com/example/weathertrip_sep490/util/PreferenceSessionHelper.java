package com.example.weathertrip_sep490.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Lưu trạng thái preference theo từng user để tránh nhầm giữa các tài khoản trên cùng thiết bị.
 */
public final class PreferenceSessionHelper {

    public static final String PREFS_NAME = "TravelGoPrefs";
    private static final String KEY_SELECTED_PREFERENCES = "selected_preferences";
    private static final String KEY_HAS_COMPLETED_PREFERENCES_ONCE = "has_completed_preferences_once";

    private PreferenceSessionHelper() {
    }

    @NonNull
    public static String resolveUserId(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userId = prefs.getString("current_user_id", null);
        if (userId != null && !userId.trim().isEmpty()) {
            return userId.trim();
        }
        String email = prefs.getString("current_email", null);
        if (email == null || email.trim().isEmpty()) {
            email = prefs.getString("saved_email", null);
        }
        if (email != null && !email.trim().isEmpty()) {
            return "email_" + email.trim().toLowerCase();
        }
        return "unknown";
    }

    public static void migrateLegacyKeysIfNeeded(@NonNull Context context, @NonNull String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.contains(scopedKey(KEY_SELECTED_PREFERENCES, userId))
                || prefs.contains(scopedKey(KEY_HAS_COMPLETED_PREFERENCES_ONCE, userId))) {
            return;
        }
        String legacySelected = prefs.getString(KEY_SELECTED_PREFERENCES, "");
        boolean legacyCompleted = prefs.getBoolean(KEY_HAS_COMPLETED_PREFERENCES_ONCE, false);
        if (TextUtils.isEmpty(legacySelected) && !legacyCompleted) {
            return;
        }
        prefs.edit()
                .putString(scopedKey(KEY_SELECTED_PREFERENCES, userId),
                        legacySelected != null ? legacySelected : "")
                .putBoolean(scopedKey(KEY_HAS_COMPLETED_PREFERENCES_ONCE, userId), legacyCompleted)
                .apply();
    }

    public static boolean hasLocalPreferences(@NonNull Context context, @NonNull String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String selected = prefs.getString(scopedKey(KEY_SELECTED_PREFERENCES, userId), "");
        boolean completed = prefs.getBoolean(scopedKey(KEY_HAS_COMPLETED_PREFERENCES_ONCE, userId), false);
        return (!TextUtils.isEmpty(selected)) || completed;
    }

    public static void saveSelectedIds(@NonNull Context context, @NonNull String userId,
                                       @Nullable List<String> preferenceIds) {
        List<String> ids = new ArrayList<>();
        if (preferenceIds != null) {
            for (String id : preferenceIds) {
                if (id != null && !id.trim().isEmpty() && !ids.contains(id.trim())) {
                    ids.add(id.trim());
                }
            }
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(scopedKey(KEY_SELECTED_PREFERENCES, userId), TextUtils.join(",", ids))
                .apply();
    }

    @NonNull
    public static String getSelectedIdsCsv(@NonNull Context context, @NonNull String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String value = prefs.getString(scopedKey(KEY_SELECTED_PREFERENCES, userId), "");
        return value != null ? value : "";
    }

    public static void markFlowCompleted(@NonNull Context context, @NonNull String userId) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(scopedKey(KEY_HAS_COMPLETED_PREFERENCES_ONCE, userId), true)
                .apply();
    }

    @NonNull
    private static String scopedKey(@NonNull String baseKey, @NonNull String userId) {
        return baseKey + "_" + userId;
    }
}
