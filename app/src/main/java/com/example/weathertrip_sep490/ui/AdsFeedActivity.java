package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.AdsFeedAdapter;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.AdvertisementItem;
import com.example.weathertrip_sep490.model.SavedPromotionItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdsFeedActivity extends AppCompatActivity {

    private AdsFeedAdapter adapter;
    private final List<AdvertisementItem> allAds = new ArrayList<>();
    private TextView tvTitle;
    private final Set<String> savedPromotionIds = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_feed);

        android.view.View btnBack = findViewById(R.id.btnAdsFeedBack);
        btnBack.setOnClickListener(v -> {
            if (isTaskRoot()) {
                startActivity(new Intent(this, HomepageActivity.class));
            }
            finish();
        });
        tvTitle = findViewById(R.id.tvAdsFeedTitle);

        RecyclerView rv = findViewById(R.id.rvAdsFeed);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdsFeedAdapter(new AdsFeedAdapter.Listener() {
            @Override
            public void onSaveClicked(@NonNull AdvertisementItem item) {
                savePromotion(item);
            }

            @Override
            public void onItemClicked(@NonNull AdvertisementItem item) {
                AdsPostDetailBottomSheet.newInstance(item).show(getSupportFragmentManager(), "AdsPostDetailBottomSheet");
            }
        });
        rv.setAdapter(adapter);

        loadSavedPromotions();
        loadAdvertisements();
        setupBottomNav();
    }

    private void loadSavedPromotions() {
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getMySavedPromotions().enqueue(new Callback<List<SavedPromotionItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<SavedPromotionItem>> call, @NonNull Response<List<SavedPromotionItem>> response) {
                if (adapter == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    Set<String> ids = new HashSet<>();
                    for (SavedPromotionItem sp : response.body()) {
                        if (sp == null) continue;
                        String pid = sp.getPromotionId();
                        if (pid != null && !pid.trim().isEmpty()) ids.add(pid.trim());
                    }
                    savedPromotionIds.clear();
                    savedPromotionIds.addAll(ids);
                    adapter.setSavedPromotionIds(ids);
                    applyFilter();
                }
                // nếu 401 hoặc lỗi thì bỏ qua (không toast để khỏi khó chịu)
            }

            @Override
            public void onFailure(@NonNull Call<List<SavedPromotionItem>> call, @NonNull Throwable t) {
                // bỏ qua
            }
        });
    }

    private void loadAdvertisements() {
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getActiveAdvertisements().enqueue(new Callback<List<AdvertisementItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdvertisementItem>> call, @NonNull Response<List<AdvertisementItem>> response) {
                allAds.clear();
                if (response.isSuccessful() && response.body() != null) {
                    allAds.addAll(response.body());
                }
                applyFilter();
                if (allAds.isEmpty()) {
                    com.example.weathertrip_sep490.util.AppToast.showInfo(AdsFeedActivity.this, "Chưa có quảng cáo nào");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AdvertisementItem>> call, @NonNull Throwable t) {
                com.example.weathertrip_sep490.util.AppToast.showError(AdsFeedActivity.this, "Lỗi tải quảng cáo: " + (t.getMessage() != null ? t.getMessage() : "unknown"));
            }
        });
    }

    private void applyFilter() {
        adapter.updateData(allAds);
        if (tvTitle != null) {
            tvTitle.setText("Các ưu đãi TravelGo (" + allAds.size() + ")");
        }
    }

    private void savePromotion(@NonNull AdvertisementItem item) {
        if (item.getPromotion() == null || item.getPromotion().getPromotionId() == null || item.getPromotion().getPromotionId().trim().isEmpty()) {
            com.example.weathertrip_sep490.util.AppToast.showError(this, "Bài viết này chưa có mã giảm để lưu");
            return;
        }
        String promotionId = item.getPromotion().getPromotionId().trim();
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.savePromotion(promotionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    com.example.weathertrip_sep490.util.AppToast.showSuccess(AdsFeedActivity.this, "Mã giảm lưu thành công !");
                    savedPromotionIds.add(promotionId);
                    if (adapter != null) adapter.setPromotionSaved(promotionId, true);
                    applyFilter();
                    return;
                }
                if (response.code() == 409) {
                    com.example.weathertrip_sep490.util.AppToast.showInfo(AdsFeedActivity.this, "Mã giảm đã được lưu trước đó");
                    savedPromotionIds.add(promotionId);
                    if (adapter != null) adapter.setPromotionSaved(promotionId, true);
                    applyFilter();
                    return;
                }
                com.example.weathertrip_sep490.util.AppToast.showError(AdsFeedActivity.this, "Không thể lưu mã giảm (" + response.code() + ")");
                if (adapter != null) adapter.setPromotionSaved(promotionId, false);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                com.example.weathertrip_sep490.util.AppToast.showError(AdsFeedActivity.this, "Lỗi mạng khi lưu mã giảm");
                if (adapter != null) adapter.setPromotionSaved(promotionId, false);
            }
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav == null) return;
        bottomNav.setSelectedItemId(R.id.nav_coupon);
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
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_trip) {
                startActivity(new Intent(this, TripManageActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_coupon) {
                return true;
            }
            return false;
        });
    }
}
