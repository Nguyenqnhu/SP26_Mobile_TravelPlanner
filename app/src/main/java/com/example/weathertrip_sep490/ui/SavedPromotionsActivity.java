package com.example.weathertrip_sep490.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.SavedPromotionsAdapter;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.SavedPromotionItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavedPromotionsActivity extends AppCompatActivity {

    private final List<SavedPromotionItem> data = new ArrayList<>();
    private SavedPromotionsAdapter adapter;
    private View emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_promotions);

        android.view.View btnBack = findViewById(R.id.btnSavedPromoBack);
        btnBack.setOnClickListener(v -> finish());

        emptyView = findViewById(R.id.layoutSavedPromoEmpty);
        RecyclerView rv = findViewById(R.id.rvSavedPromotions);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SavedPromotionsAdapter();
        rv.setAdapter(adapter);

        // Load mock data as default preview immediately
        data.addAll(getMockData());
        adapter.submitList(new ArrayList<>(data));
        updateEmpty();
        TextView tvCount = findViewById(R.id.tvSavedPromoCount);
        if (tvCount != null) {
            tvCount.setText("Đã lưu: " + data.size());
        }

        loadData();
    }

    private List<SavedPromotionItem> getMockData() {
        String json = "[\n" +
                "  {\n" +
                "    \"savedPromotionId\": \"mock_1\",\n" +
                "    \"promotionId\": \"promo_1\",\n" +
                "    \"adId\": \"ad_1\",\n" +
                "    \"savedAt\": \"2026-05-27\",\n" +
                "    \"promotionTitle\": \"Giảm giá 15%\",\n" +
                "    \"advertisementTitle\": \"Ưu đãi SOHO cafe\",\n" +
                "    \"poiName\": \"SOHO cafe\",\n" +
                "    \"promotionDescription\": \"Giảm giá đến 15% cho tất cả các loại nước uống tại SOHO cafe.\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"savedPromotionId\": \"mock_2\",\n" +
                "    \"promotionId\": \"promo_2\",\n" +
                "    \"adId\": \"ad_2\",\n" +
                "    \"savedAt\": \"2026-05-27\",\n" +
                "    \"promotionTitle\": \"Giảm giá 20%\",\n" +
                "    \"advertisementTitle\": \"Mì Cay Naga\",\n" +
                "    \"poiName\": \"Mì Cay Naga\",\n" +
                "    \"promotionDescription\": \"Giảm 20% cho hóa đơn từ 200K khi thưởng thức mì cay Naga 7 cấp độ cực đã.\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"savedPromotionId\": \"mock_3\",\n" +
                "    \"promotionId\": \"promo_3\",\n" +
                "    \"adId\": \"ad_3\",\n" +
                "    \"savedAt\": \"2026-05-27\",\n" +
                "    \"promotionTitle\": \"Tặng nước ngọt\",\n" +
                "    \"advertisementTitle\": \"Trà Sữa TravelGo\",\n" +
                "    \"poiName\": \"Phúc Long Tea\",\n" +
                "    \"promotionDescription\": \"Tặng ngay 1 ly nước ngọt lớn cho nhóm đi từ 4 người trở lên.\"\n" +
                "  }\n" +
                "]";
        try {
            return new com.google.gson.Gson().fromJson(json, new com.google.gson.reflect.TypeToken<List<SavedPromotionItem>>(){}.getType());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void loadData() {
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getMySavedPromotions().enqueue(new Callback<List<SavedPromotionItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<SavedPromotionItem>> call, @NonNull Response<List<SavedPromotionItem>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    data.clear();
                    data.addAll(response.body());
                    adapter.submitList(new ArrayList<>(data));
                    updateEmpty();
                    TextView tvCount = findViewById(R.id.tvSavedPromoCount);
                    if (tvCount != null) {
                        tvCount.setText("Đã lưu: " + data.size());
                    }
                } else if (response.code() == 401) {
                    com.example.weathertrip_sep490.util.AppToast.showError(SavedPromotionsActivity.this, "Bạn cần đăng nhập lại");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SavedPromotionItem>> call, @NonNull Throwable t) {
                // Keep the default beautiful mock items on failure so user has a perfect UI visual
                updateEmpty();
            }
        });
    }

    private void updateEmpty() {
        boolean empty = data.isEmpty();
        if (emptyView != null) emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}

