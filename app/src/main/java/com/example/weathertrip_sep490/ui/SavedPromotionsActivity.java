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

        ImageView btnBack = findViewById(R.id.btnSavedPromoBack);
        btnBack.setOnClickListener(v -> finish());

        emptyView = findViewById(R.id.layoutSavedPromoEmpty);
        RecyclerView rv = findViewById(R.id.rvSavedPromotions);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SavedPromotionsAdapter();
        rv.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getMySavedPromotions().enqueue(new Callback<List<SavedPromotionItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<SavedPromotionItem>> call, @NonNull Response<List<SavedPromotionItem>> response) {
                data.clear();
                if (response.isSuccessful() && response.body() != null) {
                    data.addAll(response.body());
                } else if (response.code() == 401) {
                    Toast.makeText(SavedPromotionsActivity.this, "Bạn cần đăng nhập lại", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SavedPromotionsActivity.this, "Không thể tải danh sách (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
                adapter.submitList(new ArrayList<>(data));
                updateEmpty();
                TextView tvCount = findViewById(R.id.tvSavedPromoCount);
                if (tvCount != null) {
                    tvCount.setText("Đã lưu: " + data.size());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SavedPromotionItem>> call, @NonNull Throwable t) {
                Toast.makeText(SavedPromotionsActivity.this, "Lỗi mạng khi tải ưu đãi đã lưu", Toast.LENGTH_SHORT).show();
                updateEmpty();
            }
        });
    }

    private void updateEmpty() {
        boolean empty = data.isEmpty();
        if (emptyView != null) emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}

