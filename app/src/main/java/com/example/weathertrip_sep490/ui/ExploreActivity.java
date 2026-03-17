package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.ExplorePOIAdapter;
import com.example.weathertrip_sep490.adapter.FeaturedPOIAdapter;
import com.example.weathertrip_sep490.adapter.FilterChipAdapter;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.POI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.net.InetAddress;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreActivity extends AppCompatActivity {

    private RecyclerView rvPlaceFilters, rvActivityFilters, rvFeaturedPlaces, rvExplorePlaces;
    private TextView tvPlaceCount;

    private final List<POI> poiList = new ArrayList<>();
    private FeaturedPOIAdapter featuredPOIAdapter;
    private ExplorePOIAdapter explorePOIAdapter;

    // Stack ảnh nổi bật
    private ImageView imgStackFront, imgStackBack1, imgStackBack2;
    private final List<String> stackImageUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        rvPlaceFilters = findViewById(R.id.rvPlaceFilters);
        rvActivityFilters = findViewById(R.id.rvActivityFilters);
        rvFeaturedPlaces = findViewById(R.id.rvFeaturedPlaces);
        rvExplorePlaces = findViewById(R.id.rvExplorePlaces);
        tvPlaceCount = findViewById(R.id.tvPlaceCount);

        imgStackFront = findViewById(R.id.imgStackFront);
        imgStackBack1 = findViewById(R.id.imgStackBack1);
        imgStackBack2 = findViewById(R.id.imgStackBack2);

        setupImageStack();
        setupBottomNav();
        setupPlaceFilters();
        setupActivityFilters();
        setupRecyclerViews();
        loadRecommendedPOIs();
    }

    private void setupPlaceFilters() {
        List<String> placeFilters = Arrays.asList(
                "Nổi bật", "Gần bạn", "Trong nhà", "Ngoài trời"
        );

        FilterChipAdapter adapter = new FilterChipAdapter(placeFilters, (position, value) -> {
            // lọc sau
        });

        rvPlaceFilters.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        rvPlaceFilters.setAdapter(adapter);
    }

    private void setupActivityFilters() {
        List<String> activityFilters = Arrays.asList(
                "Tham quan", "Ẩm thực", "Café", "Bảo tàng", "Mua sắm"
        );

        FilterChipAdapter adapter = new FilterChipAdapter(activityFilters, (position, value) -> {
            // lọc sau
        });

        rvActivityFilters.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        rvActivityFilters.setAdapter(adapter);
    }

    private void setupRecyclerViews() {
        featuredPOIAdapter = new FeaturedPOIAdapter(poiList);
        explorePOIAdapter = new ExplorePOIAdapter(poiList);

        rvFeaturedPlaces.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        rvFeaturedPlaces.setAdapter(featuredPOIAdapter);

        rvExplorePlaces.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );
        rvExplorePlaces.setAdapter(explorePOIAdapter);
    }

    private void loadRecommendedPOIs() {
        UserAPI apiService = RetrofitClient.getInstance().getPOIAPI();

        apiService.getRecommendedPOIs().enqueue(new Callback<List<POI>>() {
            @Override
            public void onResponse(Call<List<POI>> call, Response<List<POI>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    poiList.clear();
                    poiList.addAll(response.body());

                    tvPlaceCount.setText(poiList.size() + " địa điểm");

                    int withImage = 0;
                    for (int i = 0; i < poiList.size(); i++) {
                        POI p = poiList.get(i);
                        if (p == null) continue;
                        String url = p.getPoiImgUrl();
                        if (url != null && !url.trim().isEmpty()) {
                            withImage++;
                            if (withImage <= 5) {
                                Log.d("POI_IMG", "POI[" + i + "] imgUrl=" + url);
                            }
                        }
                    }
                    Log.d("POI_IMG", "Total POIs=" + poiList.size() + ", withImage=" + withImage);

                    // Debug mạng: thử fetch 1 ảnh đầu tiên để xem lỗi thật (DNS/SSL/timeout...)
                    runInternetDiagnostics();
                    for (POI p : poiList) {
                        if (p == null) continue;
                        String testUrl = p.getPoiImgUrl();
                        if (testUrl == null || testUrl.trim().isEmpty()) continue;
                        debugFetchImageUrl(testUrl.trim());
                        break;
                    }

                    updateImageStackFromPois();
                    featuredPOIAdapter.notifyDataSetChanged();
                    explorePOIAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ExploreActivity.this, "Không lấy được dữ liệu địa điểm", Toast.LENGTH_SHORT).show();
                    Log.e("API_POI", "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<POI>> call, Throwable t) {
                Toast.makeText(ExploreActivity.this, "Lỗi kết nối API", Toast.LENGTH_SHORT).show();
                Log.e("API_POI", "Failure: " + t.getMessage());
            }
        });
    }

    private void debugFetchImageUrl(String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                Log.e("IMG_NET", "OkHttp fetch FAILED url=" + url + " err=" + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                ResponseBody body = response.body();
                long len = body != null ? body.contentLength() : -1;
                Log.d("IMG_NET", "OkHttp fetch OK url=" + url + " code=" + response.code() + " len=" + len);
                if (body != null) body.close();
            }
        });
    }

    private void runInternetDiagnostics() {
        // 1) DNS resolve
        new Thread(() -> {
            try {
                InetAddress addr = InetAddress.getByName("res.cloudinary.com");
                Log.d("IMG_NET", "DNS OK res.cloudinary.com -> " + addr.getHostAddress());
            } catch (Exception e) {
                Log.e("IMG_NET", "DNS FAIL res.cloudinary.com err=" + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
        }).start();

        // 2) Quick internet check (Google generate_204)
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url("https://www.google.com/generate_204")
                .get()
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                Log.e("IMG_NET", "Internet check FAILED err=" + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                Log.d("IMG_NET", "Internet check OK code=" + response.code());
                ResponseBody body = response.body();
                if (body != null) body.close();
            }
        });
    }

    // Thiết lập stack ảnh: khi bấm ảnh trước sẽ xoay về cuối danh sách
    private void setupImageStack() {
        if (imgStackFront == null || imgStackBack1 == null || imgStackBack2 == null) {
            return;
        }

        imgStackFront.setOnClickListener(v -> {
            if (stackImageUrls.isEmpty()) return;
            // Đưa ảnh trước (index 0) xuống cuối
            String first = stackImageUrls.remove(0);
            stackImageUrls.add(first);
            applyStackImages();
        });
    }

    private void applyStackImages() {
        if (imgStackFront == null || imgStackBack1 == null || imgStackBack2 == null) return;
        if (stackImageUrls.isEmpty()) return;

        int size = stackImageUrls.size();
        String frontUrl = stackImageUrls.get(0);
        String back1Url = stackImageUrls.get(size > 1 ? 1 : 0);
        String back2Url = stackImageUrls.get(size > 2 ? 2 : (size > 1 ? 1 : 0));

        loadStackImage(imgStackFront, frontUrl);
        loadStackImage(imgStackBack1, back1Url);
        loadStackImage(imgStackBack2, back2Url);
    }

    private void updateImageStackFromPois() {
        if (imgStackFront == null || imgStackBack1 == null || imgStackBack2 == null) return;

        stackImageUrls.clear();
        for (POI poi : poiList) {
            if (poi == null) continue;
            String url = poi.getPoiImgUrl();
            if (url == null) continue;
            url = url.trim();
            if (url.isEmpty()) continue;
            stackImageUrls.add(url);
            if (stackImageUrls.size() >= 3) break;
        }
        applyStackImages();
    }

    private void loadStackImage(ImageView imageView, String url) {
        if (url == null || url.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.bg_image_placeholder);
            return;
        }
        Glide.with(imageView)
                .load(url.trim())
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .centerCrop()
                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        Log.e("GLIDE_IMG", "Stack load failed url=" + url, e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);
    }

    private void setupBottomNav() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_explore);
        if (bottomNav == null) return;
        bottomNav.setSelectedItemId(R.id.nav_explore); // Tab Khám phá
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_explore) return true; // Đã ở Khám phá

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomepageActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_user) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_trip) {
                Toast.makeText(this, "Lịch trình", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}