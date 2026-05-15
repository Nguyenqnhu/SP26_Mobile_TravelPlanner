package com.example.weathertrip_sep490.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.weathertrip_sep490.util.LanguageManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivAvatar;
    private SharedPreferences sharedPrefs;
    private SwitchMaterial switchNotify, switchDark;
    private UserAPI userAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        userAPI = RetrofitClient.getInstance().getPreferenceAPI();

        initViews();
        setupClickListeners();
        setupBottomNav();
        loadSavedSettings();
        loadUserProfile();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_profile);
        if (bottomNav == null) return;
        bottomNav.setSelectedItemId(R.id.nav_user); // Tô đúng icon Tài khoản
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomepageActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_explore) {
                startActivity(new Intent(this, ExploreActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_user) {
                return true; // Đã ở Profile
            }
            if (id == R.id.nav_trip) {
                startActivity(new Intent(this, TripManageActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_coupon) {
                startActivity(new Intent(this, AdsFeedActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        switchNotify = findViewById(R.id.switchNotifications);
        switchDark = findViewById(R.id.switchDarkMode);

        setSettingItemText(R.id.itemPersonalInfo, "Thông tin cá nhân", R.drawable.ic_user);
        setSettingItemText(R.id.itemChangePassword, "Đổi mật khẩu", R.drawable.ic_lock_outline);
        setSettingItemText(R.id.itemUpdatePreference, "Cập nhật preference", R.drawable.ic_preferences);
        setSettingItemText(R.id.itemSavedPromotions, "Các ưu đãi đã lưu", R.drawable.ic_heart);
        setSettingItemText(R.id.itemLanguage, "Ngôn ngữ", R.drawable.ic_language);
        setSettingItemText(R.id.itemHelp, "Trợ giúp", R.drawable.ic_help_outline);

    }

    private void setupClickListeners() {

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Change Avatar
        findViewById(R.id.btnCamera).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        findViewById(R.id.itemPersonalInfo).setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        findViewById(R.id.itemChangePassword).setOnClickListener(v -> {
            startActivity(new Intent(this, ResetPasswordActivity.class));
        });

        findViewById(R.id.itemUpdatePreference).setOnClickListener(v -> {
            startActivity(new Intent(this, PreferencesActivity.class));
        });

        findViewById(R.id.itemSavedPromotions).setOnClickListener(v -> {
            startActivity(new Intent(this, SavedPromotionsActivity.class));
        });

        findViewById(R.id.itemLanguage).setOnClickListener(v -> {
            LanguageManager.showLanguageDialog(this, this::recreate);
        });

        findViewById(R.id.itemHelp).setOnClickListener(v -> {
            Toast.makeText(this, "Mở Help", Toast.LENGTH_SHORT).show();
        });


        //  Switch (DarkMode / Notifications)
        switchNotify.setOnCheckedChangeListener((button, isChecked) -> {
            sharedPrefs.edit().putBoolean("notifications", isChecked).apply();
        });

        switchDark.setOnCheckedChangeListener((button, isChecked) -> {
            sharedPrefs.edit().putBoolean("darkMode", isChecked).apply();
            applyTheme(isChecked);
        });

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());
    }

    private void setSettingItemText(int viewId, String text, int iconResId) {
        View root = findViewById(viewId);
        if (root == null) return;
        TextView label = root.findViewById(R.id.itemLabel);
        ImageView icon = root.findViewById(R.id.itemIcon);
        if (label != null) {
            label.setText(text);
        }
        if (icon != null) {
            icon.setImageResource(iconResId);
        }
    }

    private void applyTheme(boolean isDark) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void loadSavedSettings() {
        switchNotify.setChecked(sharedPrefs.getBoolean("notifications", true));
        switchDark.setChecked(sharedPrefs.getBoolean("darkMode", false));
    }

    private void loadUserProfile() {
        SharedPreferences loginPrefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
        String userId = loginPrefs.getString("current_user_id", null);
        String email = loginPrefs.getString("current_email",
                loginPrefs.getString("saved_email", null));
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }
        userAPI.getUserById(userId.trim()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    TextView tvName = findViewById(R.id.tvUserName);
                    TextView tvEmail = findViewById(R.id.tvUserEmail);
                    if (user.getName() != null && !user.getName().isEmpty()) {
                        tvName.setText(user.getName());
                    }
                    if (email != null && !email.isEmpty()) {
                        tvEmail.setText(email);
                    }
                    // AvatarUrl: hiện chưa load ảnh từ URL, có thể thêm sau với thư viện image loading
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // bỏ qua, dùng giá trị mặc định
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            ivAvatar.setImageURI(data.getData());
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn chắc chắn muốn thoát chứ?")
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    // Xóa token + thông tin user hiện tại
                    getSharedPreferences("TravelGoPrefs", MODE_PRIVATE)
                            .edit()
                            .remove("access_token")
                            .remove("refresh_token")
                            .remove("current_user_id")
                            .remove("current_email")
                            .apply();

                    // Quay về màn Welcome và clear back stack
                    Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}