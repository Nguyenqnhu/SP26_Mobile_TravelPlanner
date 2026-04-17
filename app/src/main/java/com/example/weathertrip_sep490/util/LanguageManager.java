package com.example.weathertrip_sep490.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public final class LanguageManager {

    private static final String LANG_VI = "vi";
    private static final String LANG_EN = "en";

    private LanguageManager() {}

    public static void applyLanguage(String languageTag) {
        String safeTag = (languageTag == null || languageTag.trim().isEmpty()) ? LANG_VI : languageTag.trim();
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(safeTag);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    public static String getCurrentLanguageTag(Context context) {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales == null || locales.isEmpty()) {
            return LANG_VI;
        }
        String tag = locales.get(0).toLanguageTag();
        return tag == null || tag.isEmpty() ? LANG_VI : tag;
    }

    public static boolean isVietnamese(Context context) {
        return LANG_VI.equals(getCurrentLanguageTag(context));
    }

    public static void showLanguageDialog(Activity activity, Runnable onChanged) {
        String[] labels = new String[] {"Tiếng Việt", "English"};
        String current = getCurrentLanguageTag(activity);
        int selected = LANG_EN.equals(current) ? 1 : 0;
        new androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle("Ngôn ngữ")
                .setSingleChoiceItems(labels, selected, (dialog, which) -> {
                    String tag = which == 1 ? LANG_EN : LANG_VI;
                    applyLanguage(tag);
                    dialog.dismiss();
                    if (onChanged != null) onChanged.run();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
