package com.example.weathertrip_sep490.ui;

import com.example.weathertrip_sep490.util.AppToast;

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
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExplorePOIDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POI_ID = "extra_poi_id";
    public static final String EXTRA_POI_JSON = "extra_poi_json";

    private static final Gson gson = new Gson();

    private ImageView ivImage;
    private TextView tvName;
    private TextView tvCity;
    private TextView tvLocationName;
    private TextView tvAddress;
    private TextView tvCost;
    private TextView tvHours;
    private TextView tvIndoor;
    private TextView tvType;
    private TextView tvRecommendation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_poi_detail);

        ivImage = findViewById(R.id.ivPoiDetailImage);
        tvName = findViewById(R.id.tvPoiDetailName);
        tvCity = findViewById(R.id.tvPoiDetailCity);
        tvLocationName = findViewById(R.id.tvPoiDetailLocationName);
        tvAddress = findViewById(R.id.tvPoiDetailAddress);
        tvCost = findViewById(R.id.tvPoiDetailCost);
        tvHours = findViewById(R.id.tvPoiDetailHours);
        tvIndoor = findViewById(R.id.tvPoiDetailIndoor);
        tvType = findViewById(R.id.tvPoiDetailType);
        tvRecommendation = findViewById(R.id.tvPoiDetailRecommendation);

        ImageButton btnBack = findViewById(R.id.btnBackDetail);
        btnBack.setOnClickListener(v -> finish());

        String poiId = getIntent().getStringExtra(EXTRA_POI_ID);
        String poiJson = getIntent().getStringExtra(EXTRA_POI_JSON);
        if (poiJson != null && !poiJson.trim().isEmpty()) {
            try {
                POI poi = gson.fromJson(poiJson.trim(), POI.class);
                if (poi != null) {
                    bindPoi(poi);
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        if (poiId == null || poiId.trim().isEmpty()) {
            AppToast.show(this, "Không có POI id");
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
                    AppToast.show(ExplorePOIDetailActivity.this, "Không lấy được dữ liệu POI");
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
                    AppToast.show(ExplorePOIDetailActivity.this, "Không tìm thấy POI");
                    return;
                }
                bindPoi(found);
            }

            @Override
            public void onFailure(Call<List<POI>> call, Throwable t) {
                Log.e("POI_DETAIL", "loadPoiById failed", t);
                AppToast.show(ExplorePOIDetailActivity.this, "Lỗi kết nối API");
            }
        });
    }

    private void bindPoi(POI poi) {
        tvName.setText(safe(poi.getName()));
        tvAddress.setText(safeWithFallback(poi.getAddress(), "Chưa có địa chỉ"));
        tvLocationName.setText("Khu vực: " + safeWithFallback(poi.getLocationName(), "Chưa cập nhật"));
        tvCity.setText("Thành phố: " + safeWithFallback(poi.getCity(), "Chưa cập nhật"));
        tvType.setText(safeWithFallback(poi.getType(), "Unknown"));
        tvCost.setText(formatCost(poi.getApproxCost()));
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
                AppToast.show(this, "Chưa có GoogleMapLink");
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

    private String safeWithFallback(String s, String fallback) {
        String v = safe(s).trim();
        return v.isEmpty() ? fallback : v;
    }

    private String formatCost(String rawCost) {
        String cost = safe(rawCost).trim();
        if (cost.isEmpty()) return "Chưa cập nhật";
        if ("0".equals(cost)) return "Miễn phí";
        return cost + " VND";
    }
}

