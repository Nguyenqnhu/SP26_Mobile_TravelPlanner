package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.util.AppToast;
import com.example.weathertrip_sep490.model.Preference;
import com.example.weathertrip_sep490.model.UserPreferenceItem;
import com.example.weathertrip_sep490.util.ViewAnimationUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreferencesActivity extends AppCompatActivity {

    private static final String TAG = "PreferencesActivity";
    private static final String PREFS_NAME = "TravelGoPrefs";
    private static final String KEY_SELECTED_PREFERENCES = "selected_preferences";
    private static final String KEY_PENDING_PREFERENCES_SYNC = "pending_preferences_sync";
    private static final String KEY_PENDING_PREFERENCES_IDS = "pending_preferences_ids";

    private ChipGroup cgPreferences;
    private TextView tvSkip;
    private EditText etSearch;
    private final List<String> selectedIds = new ArrayList<>(); // Lưu ID những mục người dùng chọn
    private final List<Preference> allPreferences = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Nếu Login gửi token qua Intent thì lưu ngay vào SharedPreferences (tránh chưa kịp đọc sau)
        String tokenFromIntent = getIntent() != null ? getIntent().getStringExtra("access_token") : null;
        if (tokenFromIntent != null && !tokenFromIntent.trim().isEmpty()) {
            getSharedPreferences("TravelGoPrefs", MODE_PRIVATE).edit()
                    .putString("access_token", tokenFromIntent.trim()).apply();
        }

        cgPreferences = findViewById(R.id.cgPreferences);
        tvSkip = findViewById(R.id.tvSkip);
        etSearch = findViewById(R.id.etSearch);

        ViewAnimationUtil.setTouchScaleAnimation(findViewById(R.id.btnContinue));
        ViewAnimationUtil.setTouchScaleAnimation(tvSkip);

        preloadSelectedPreferencesFromLocal();
        fetchPreferencesFromServer();
        fetchSelectedPreferencesFromServer();

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            if (selectedIds.isEmpty()) {
                AppToast.showError(this, "Vui lòng chọn ít nhất 1 sở thích");
            } else {
                saveUserPreferencesAndContinue();
            }
        });

        tvSkip.setOnClickListener(v -> navigateNext());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterAndRender(s.toString());
            }
        });
    }

    private void fetchPreferencesFromServer() {
        UserAPI api = RetrofitClient.getInstance().getPreferenceAPI();
        api.getAllPreferences().enqueue(new Callback<List<Preference>>() {
            @Override
            public void onResponse(Call<List<Preference>> call, Response<List<Preference>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allPreferences.clear();
                    allPreferences.addAll(response.body());
                    filterAndRender(etSearch.getText().toString());
                } else {
                    AppToast.showError(PreferencesActivity.this, "Không tải được danh sách sở thích");
                }
            }

            @Override
            public void onFailure(Call<List<Preference>> call, Throwable t) {
                AppToast.showError(PreferencesActivity.this, "Lỗi kết nối server");
            }
        });
    }

    private void fetchSelectedPreferencesFromServer() {
        UserAPI api = RetrofitClient.getInstance().getPreferenceAPI();
        api.getUserPreferences().enqueue(new Callback<List<UserPreferenceItem>>() {
            @Override
            public void onResponse(Call<List<UserPreferenceItem>> call, Response<List<UserPreferenceItem>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                selectedIds.clear();
                for (UserPreferenceItem item : response.body()) {
                    if (item != null && item.getPreferenceId() != null && !item.getPreferenceId().trim().isEmpty()) {
                        selectedIds.add(item.getPreferenceId().trim());
                    }
                }
                persistSelectedPreferencesLocal();
                filterAndRender(etSearch.getText().toString());
            }

            @Override
            public void onFailure(Call<List<UserPreferenceItem>> call, Throwable t) {
                // Giữ dữ liệu local nếu gọi API lỗi.
            }
        });
    }

    private void preloadSelectedPreferencesFromLocal() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String localSelected = prefs.getString(KEY_SELECTED_PREFERENCES, "");
        if (localSelected == null || localSelected.trim().isEmpty()) {
            return;
        }
        selectedIds.clear();
        String[] parts = localSelected.split(",");
        for (String part : parts) {
            String id = part == null ? "" : part.trim();
            if (!id.isEmpty() && !selectedIds.contains(id)) {
                selectedIds.add(id);
            }
        }
    }

    private void persistSelectedPreferencesLocal() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String joinedIds = TextUtils.join(",", selectedIds);
        prefs.edit().putString(KEY_SELECTED_PREFERENCES, joinedIds).apply();
    }

    private void filterAndRender(String query) {
        String q = query == null ? "" : query.toLowerCase().trim();
        List<Preference> filtered = new ArrayList<>();
        for (Preference p : allPreferences) {
            if (p.getName() != null && p.getName().toLowerCase().contains(q)) {
                filtered.add(p);
            }
        }
        renderChips(filtered);
    }

    private void renderChips(List<Preference> list) {
        cgPreferences.removeAllViews();
        for (Preference pref : list) {
            Chip chip = new Chip(this);
            chip.setText(pref.getName());
            chip.setCheckable(true);
            chip.setChecked(selectedIds.contains(pref.getId()));
            stylePreferenceChip(chip);

            chip.setOnCheckedChangeListener((v, isChecked) -> {
                if (isChecked) {
                    if (!selectedIds.contains(pref.getId())) {
                        selectedIds.add(pref.getId());
                    }
                } else {
                    selectedIds.remove(pref.getId());
                }
            });

            cgPreferences.addView(chip);
        }
    }

    /**
     * Chip giống mockup: nền trắng; chọn — viền + chữ xanh; chưa chọn — viền xám + chữ slate.
     * Màu dùng từ colors.xml, không thêm drawable mới.
     */
    private void stylePreferenceChip(Chip chip) {
        chip.setCloseIconVisible(false);
        chip.setEnsureMinTouchTargetSize(false);
        float d = getResources().getDisplayMetrics().density;
        chip.setChipCornerRadius(24f * d);
        chip.setChipMinHeight((int) (40 * d));
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        chip.setChipStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics()));

        int greenStroke = ContextCompat.getColor(this, R.color.emerald_600);
        int greenText = ContextCompat.getColor(this, R.color.text_title);
        int greyStroke = Color.parseColor("#E5E7EB");
        int greyText = ContextCompat.getColor(this, R.color.slate_500);

        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
        chip.setChipStrokeColor(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{greenStroke, greyStroke}));
        chip.setTextColor(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{greenText, greyText}));
    }

    private void saveUserPreferencesAndContinue() {
        // Lưu local để dùng offline
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String joinedIds = TextUtils.join(",", selectedIds);
        persistSelectedPreferencesLocal();

        // Token: ưu tiên từ Intent (vừa đăng nhập), không có thì đọc SharedPreferences
        String accessToken = (getIntent() != null ? getIntent().getStringExtra("access_token") : null);
        if (accessToken == null || accessToken.trim().isEmpty()) {
            accessToken = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString("access_token", null);
        }
        if (accessToken == null || accessToken.trim().isEmpty()) {
            AppToast.showError(this, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.");
            navigateNext();
            return;
        }

        UserAPI api = RetrofitClient.getInstance().getPreferenceAPI();
        String authHeader = accessToken.trim().startsWith("Bearer ")
                ? accessToken.trim()
                : "Bearer " + accessToken.trim();
        Call<Void> call = api.updateUserPreferences(
                authHeader,
                new ArrayList<>(selectedIds)
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    prefs.edit()
                            .putBoolean(KEY_PENDING_PREFERENCES_SYNC, false)
                            .remove(KEY_PENDING_PREFERENCES_IDS)
                            .apply();
                    AppToast.showSuccess(PreferencesActivity.this, "Đã cập nhật sở thích thành công!");
                    navigateNext();
                } else {
                    int code = response.code();
                    String err = null;
                    try {
                        if (response.errorBody() != null) err = response.errorBody().string();
                    } catch (Exception ignored) { }
                    Log.e(TAG, "updateUserPreferences failed. code=" + code
                            + " selectedIds=" + selectedIds
                            + " errorBody=" + (err != null ? err : "<null>"));
                    // Đánh dấu chờ đồng bộ lại khi server ổn
                    prefs.edit()
                            .putBoolean(KEY_PENDING_PREFERENCES_SYNC, true)
                            .putString(KEY_PENDING_PREFERENCES_IDS, joinedIds)
                            .apply();

                    String detailLower = err != null ? err.toLowerCase() : "";
                    boolean looksLikeServerMisconfig = code >= 500
                            || detailLower.contains("cloudinary")
                            || detailLower.contains("developerexceptionpagemiddleware")
                            || detailLower.contains("system.reflection");
                    AppToast.showError(PreferencesActivity.this,
                            looksLikeServerMisconfig
                                    ? "Server đang lỗi (" + code + "). Sở thích đã lưu trên máy và sẽ đồng bộ sau."
                                    : ("Không lưu được lên server (" + code + "). " + (err != null && !err.trim().isEmpty() ? err : "Vui lòng thử lại sau.")));
                    navigateNext();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                AppToast.showError(PreferencesActivity.this,
                        "Lỗi kết nối: " + (t.getMessage() != null ? t.getMessage() : "Unknown"));
                navigateNext();
            }
        });
    }

    private void navigateNext() {
       //navigate
        Intent intent = new Intent(PreferencesActivity.this, HomepageActivity.class);
        startActivity(intent);
        finish();
    }
}