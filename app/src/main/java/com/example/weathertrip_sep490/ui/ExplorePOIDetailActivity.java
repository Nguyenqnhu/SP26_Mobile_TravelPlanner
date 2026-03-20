package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RecentPoiStorage;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.POI;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExplorePOIDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POI_ID = "extra_poi_id";

    private ImageView ivImage;
    private TextView tvName;
    private TextView tvCity;
    private TextView tvAddress;
    private TextView tvCost;
    private TextView tvHours;
    private TextView tvIndoor;
    private TextView tvRecommendation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_poi_detail);

        ivImage = findViewById(R.id.ivPoiDetailImage);
        tvName = findViewById(R.id.tvPoiDetailName);
        tvCity = findViewById(R.id.tvPoiDetailCity);
        tvAddress = findViewById(R.id.tvPoiDetailAddress);
        tvCost = findViewById(R.id.tvPoiDetailCost);
        tvHours = findViewById(R.id.tvPoiDetailHours);
        tvIndoor = findViewById(R.id.tvPoiDetailIndoor);
        tvRecommendation = findViewById(R.id.tvPoiDetailRecommendation);

        ImageButton btnBack = findViewById(R.id.btnBackDetail);
        btnBack.setOnClickListener(v -> finish());

        String poiId = getIntent().getStringExtra(EXTRA_POI_ID);
        if (poiId == null || poiId.trim().isEmpty()) {
            Toast.makeText(this, "Không có POI id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPoiByIdFromRecommended(poiId.trim());
    }

    private void loadPoiByIdFromRecommended(String poiId) {
        UserAPI api = RetrofitClient.getInstance().getPOIAPI();
        api.getRecommendedPOIs("vi").enqueue(new Callback<List<POI>>() {
            @Override
            public void onResponse(Call<List<POI>> call, Response<List<POI>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ExplorePOIDetailActivity.this, "Không lấy được dữ liệu POI", Toast.LENGTH_SHORT).show();
                    return;
                }

                POI found = null;
                for (POI p : response.body()) {
                    if (p == null || p.getId() == null) continue;
                    if (poiId.equalsIgnoreCase(p.getId())) {
                        found = p;
                        break;
                    }
                }

                if (found == null) {
                    Toast.makeText(ExplorePOIDetailActivity.this, "Không tìm thấy POI", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindPoi(found);
            }

            @Override
            public void onFailure(Call<List<POI>> call, Throwable t) {
                Log.e("POI_DETAIL", "loadPoiById failed", t);
                Toast.makeText(ExplorePOIDetailActivity.this, "Lỗi kết nối API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindPoi(POI poi) {
        tvName.setText(safe(poi.getName()));
        tvCity.setText(safe(poi.getCity()));
        tvAddress.setText(safe(poi.getAddress()));
        tvCost.setText(poi.getApproxCost() == null || poi.getApproxCost().isEmpty() ? "Chưa cập nhật" : poi.getApproxCost());
        tvHours.setText(buildOpenHoursText(poi));
        tvIndoor.setText(poi.isIndoor() ? "Trong nhà" : "Ngoài trời");

        String rec = poi.getVisitRecommendation();
        tvRecommendation.setText(rec == null || rec.trim().isEmpty() ? "Chưa có gợi ý" : rec);

        RecentPoiStorage.addRecent(this, poi);

        String url = poi.getPoiImgUrl();
        if (url == null || url.trim().isEmpty()) {
            ivImage.setImageResource(R.drawable.bg_image_placeholder);
        } else {
            Glide.with(ivImage)
                    .load(url.trim())
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .centerCrop()
                    .into(ivImage);
        }

        findViewById(R.id.btnOpenMap).setOnClickListener(v -> {
            String link = poi.getGoogleMapLink();
            if (link == null || link.trim().isEmpty()) {
                Toast.makeText(this, "Chưa có GoogleMapLink", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link.trim())));
        });
    }

    private String buildOpenHoursText(POI poi) {
        if (poi == null) return "Chưa có giờ mở cửa";
        if (poi.isIs24Hours()) return "Mở cửa 24/7";

        String open = safe(poi.getOpenHour()).trim();
        String close = safe(poi.getCloseHour()).trim();
        if (open.length() >= 5) open = open.substring(0, 5);
        if (close.length() >= 5) close = close.substring(0, 5);
        if (open.isEmpty() || close.isEmpty()) return "Chưa có giờ mở cửa";
        return open + "-" + close;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}

