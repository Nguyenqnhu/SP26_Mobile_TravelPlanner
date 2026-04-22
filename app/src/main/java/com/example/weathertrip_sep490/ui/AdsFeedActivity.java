package com.example.weathertrip_sep490.ui;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdsFeedActivity extends AppCompatActivity {

    private AdsFeedAdapter adapter;
    private final List<AdvertisementItem> allAds = new ArrayList<>();
    private int selectedFilter = 0;
    private MaterialButton chipAll;
    private MaterialButton chipPromo;
    private MaterialButton chipSaved;
    private TextView tvTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_feed);

        ImageView btnBack = findViewById(R.id.btnAdsFeedBack);
        btnBack.setOnClickListener(v -> finish());
        tvTitle = findViewById(R.id.tvAdsFeedTitle);
        chipAll = findViewById(R.id.chipAdsAll);
        chipPromo = findViewById(R.id.chipAdsPromo);
        chipSaved = findViewById(R.id.chipAdsSaved);

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

        chipAll.setOnClickListener(v -> selectFilter(0));
        chipPromo.setOnClickListener(v -> selectFilter(1));
        chipSaved.setOnClickListener(v -> selectFilter(2));
        selectFilter(0);

        loadSavedPromotions();
        loadAdvertisements();
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
                    adapter.setSavedPromotionIds(ids);
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
                    Toast.makeText(AdsFeedActivity.this, "Chưa có quảng cáo nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AdvertisementItem>> call, @NonNull Throwable t) {
                Toast.makeText(AdsFeedActivity.this, "Lỗi tải quảng cáo: " + (t.getMessage() != null ? t.getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectFilter(int index) {
        selectedFilter = index;
        styleChip(chipAll, index == 0);
        styleChip(chipPromo, index == 1);
        styleChip(chipSaved, index == 2);
        applyFilter();
    }

    private void styleChip(@NonNull MaterialButton chip, boolean selected) {
        int stroke = ContextCompat.getColor(this, R.color.green_primary);
        int fillBase = ContextCompat.getColor(this, R.color.emerald_100);
        int fill = androidx.core.graphics.ColorUtils.setAlphaComponent(fillBase, 170);

        chip.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selected ? fill : ContextCompat.getColor(this, R.color.white)));
        chip.setTextColor(stroke);
        chip.setStrokeWidth(2);
        chip.setStrokeColor(android.content.res.ColorStateList.valueOf(stroke));
    }

    private void applyFilter() {
        List<AdvertisementItem> filtered = new ArrayList<>();
        for (AdvertisementItem item : allAds) {
            if (selectedFilter == 0) {
                filtered.add(item);
            } else if (selectedFilter == 1) {
                if (item.getPromotion() != null) filtered.add(item);
            } else {
                String promoTitle = item.getPromotion() != null ? item.getPromotion().getTitle() : null;
                if (promoTitle != null && promoTitle.toLowerCase(Locale.ROOT).contains("giảm")) {
                    filtered.add(item);
                }
            }
        }
        adapter.updateData(filtered);
        if (tvTitle != null) {
            tvTitle.setText("Các ưu đãi TravelPlanner (" + filtered.size() + ")");
        }
    }

    private void savePromotion(@NonNull AdvertisementItem item) {
        if (item.getPromotion() == null || item.getPromotion().getPromotionId() == null || item.getPromotion().getPromotionId().trim().isEmpty()) {
            Toast.makeText(this, "Bài viết này chưa có mã giảm để lưu", Toast.LENGTH_SHORT).show();
            return;
        }
        String promotionId = item.getPromotion().getPromotionId().trim();
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.savePromotion(promotionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdsFeedActivity.this, "Mã giảm lưu thành công !", Toast.LENGTH_SHORT).show();
                    if (adapter != null) adapter.setPromotionSaved(promotionId, true);
                    return;
                }
                if (response.code() == 409) {
                    Toast.makeText(AdsFeedActivity.this, "Mã giảm đã được lưu trước đó", Toast.LENGTH_SHORT).show();
                    if (adapter != null) adapter.setPromotionSaved(promotionId, true);
                    return;
                }
                Toast.makeText(AdsFeedActivity.this, "Không thể lưu mã giảm (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                if (adapter != null) adapter.setPromotionSaved(promotionId, false);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(AdsFeedActivity.this, "Lỗi mạng khi lưu mã giảm", Toast.LENGTH_SHORT).show();
                if (adapter != null) adapter.setPromotionSaved(promotionId, false);
            }
        });
    }
}
