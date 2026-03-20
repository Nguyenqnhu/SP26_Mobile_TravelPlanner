package com.example.weathertrip_sep490.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.weathertrip_sep490.model.POI;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class RecentPoiStorage {
    private static final String PREFS_NAME = "TravelGoPrefs";
    private static final String KEY_RECENT_POIS = "recent_pois_v1";
    private static final int MAX_ITEMS = 10;

    private static final Gson gson = new Gson();
    private static final Type listType = new TypeToken<List<POI>>() {}.getType();

    private RecentPoiStorage() {}

    public static List<POI> getRecent(Context context) {
        if (context == null) return Collections.emptyList();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_RECENT_POIS, null);
        if (raw == null || raw.trim().isEmpty()) return Collections.emptyList();
        try {
            List<POI> list = gson.fromJson(raw, listType);
            if (list == null) return Collections.emptyList();
            return list;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public static void addRecent(Context context, POI poi) {
        if (context == null || poi == null) return;
        String id = poi.getId();
        if (id == null || id.trim().isEmpty()) return;

        List<POI> current = new ArrayList<>(getRecent(context));

        for (Iterator<POI> it = current.iterator(); it.hasNext();) {
            POI p = it.next();
            if (p == null || p.getId() == null) continue;
            if (id.equalsIgnoreCase(p.getId())) {
                it.remove();
                break;
            }
        }

        current.add(0, poi);
        if (current.size() > MAX_ITEMS) {
            current = new ArrayList<>(current.subList(0, MAX_ITEMS));
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_RECENT_POIS, gson.toJson(current, listType)).apply();
    }
}

