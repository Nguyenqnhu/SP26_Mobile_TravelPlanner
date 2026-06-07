package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weathertrip_sep490.util.AppToast;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

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

public class ExploreActivity extends AppCompatActivity implements OnMapReadyCallback {

    private RecyclerView rvPlaceFilters, rvActivityFilters, rvFeaturedPlaces, rvExplorePlaces;
    private TextView tvPlaceCount;
    private TextView btnToggleMap;

    private final List<POI> poiList = new ArrayList<>();
    private FeaturedPOIAdapter featuredPOIAdapter;
    private ExplorePOIAdapter explorePOIAdapter;

    private static final String MAPVIEW_BUNDLE_KEY = "ExploreMapViewBundle";
    private MapView mapView;
    private GoogleMap googleMap;
    private boolean mapReady = false;
    private boolean mapMode = false;
    private final List<Marker> poiMarkers = new ArrayList<>();
    private static final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);


        rvFeaturedPlaces = findViewById(R.id.rvFeaturedPlaces);
        rvExplorePlaces = findViewById(R.id.rvExplorePlaces);
        tvPlaceCount = findViewById(R.id.tvPlaceCount);
        btnToggleMap = findViewById(R.id.btnExploreToggleMap);


        setupBottomNav();
        setupRecyclerViews();
        initMapView(savedInstanceState);
        setupToggleMap();

        // Lấy POIs thật từ BE
        loadRecommendedPOIs();
    }

    private void setupToggleMap() {
        if (btnToggleMap == null) return;
        mapMode = false;
        btnToggleMap.setText("Bản đồ");
        btnToggleMap.setOnClickListener(v -> openExploreMap());
        applyExploreModeUi();
    }

    private void openExploreMap() {
        Intent intent = new Intent(this, ExploreMapActivity.class);
        intent.putExtra("pois_json", gson.toJson(poiList));
        startActivity(intent);
    }

    private void applyExploreModeUi() {
        if (mapView != null) {
            mapView.setVisibility(View.GONE);
        }
        if (rvFeaturedPlaces != null) {
            rvFeaturedPlaces.setVisibility(View.VISIBLE);
        }
        if (rvExplorePlaces != null) {
            rvExplorePlaces.setVisibility(View.VISIBLE);
        }
    }

    private void initMapView(Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapExplorePois);
        if (mapView == null) return;
        Bundle mapBundle = null;
        if (savedInstanceState != null) {
            mapBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        mapReady = true;
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof POI) {
                openPoiDetailMock((POI) tag);
                return true;
            }
            return false;
        });

        applyPoiMarkers();
    }

    private void applyPoiMarkers() {
        if (!mapReady || googleMap == null) return;
        googleMap.clear();
        poiMarkers.clear();
        if (poiList.isEmpty()) return;

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        // Tách pin nếu nhiều POI trùng tọa độ (đỡ bị chồng thành 1 pin)
        final double earthRadiusMeters = 6378137.0;
        final double jitterRadiusMeters = 40.0;

        java.util.Map<String, List<POI>> clusters = new java.util.HashMap<>();
        for (POI p : poiList) {
            if (p == null) continue;
            double lat = p.getLatitude();
            double lng = p.getLongitude();
            if (lat == 0.0d && lng == 0.0d) continue;
            long latKey = Math.round(lat * 100000d);
            long lngKey = Math.round(lng * 100000d);
            String key = latKey + "_" + lngKey;
            List<POI> list = clusters.get(key);
            if (list == null) {
                list = new ArrayList<>();
                clusters.put(key, list);
            }
            list.add(p);
        }

        int added = 0;
        LatLng first = null;
        for (java.util.Map.Entry<String, List<POI>> entry : clusters.entrySet()) {
            List<POI> group = entry.getValue();
            if (group == null || group.isEmpty()) continue;

            POI basePoi = group.get(0);
            double baseLat = basePoi.getLatitude();
            double baseLng = basePoi.getLongitude();
            if (baseLat == 0.0d && baseLng == 0.0d) continue;

            int n = group.size();
            for (int i = 0; i < n; i++) {
                POI p = group.get(i);
                if (p == null) continue;

                double lat = baseLat;
                double lng = baseLng;
                if (n > 1) {
                    double angle = (2.0 * Math.PI * i) / n;
                    double latRad = Math.toRadians(baseLat);
                    double dx = jitterRadiusMeters * Math.cos(angle);
                    double dy = jitterRadiusMeters * Math.sin(angle);

                    double offsetLatRad = dx / earthRadiusMeters;
                    double offsetLngRad = dy / (earthRadiusMeters * Math.cos(latRad));

                    lat = baseLat + Math.toDegrees(offsetLatRad);
                    lng = baseLng + Math.toDegrees(offsetLngRad);
                }

                LatLng pos = new LatLng(lat, lng);
                if (first == null) first = pos;

                Marker m = googleMap.addMarker(
                        new MarkerOptions().position(pos).title(safe(p.getName())));
                if (m != null) {
                    m.setTag(p);
                    poiMarkers.add(m);
                    bounds.include(pos);
                    added++;
                }
            }
        }

        if (added == 0) return;
        try {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80));
        } catch (Exception ignored) {
            if (first != null) googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(first, 12f));
        }
    }

    private void openPoiDetailMock(POI poi) {
        if (poi == null) return;
        Intent intent = new Intent(this, ExplorePOIDetailActivity.class);
        intent.putExtra(ExplorePOIDetailActivity.EXTRA_POI_ID, safe(poi.getId()));
        intent.putExtra(ExplorePOIDetailActivity.EXTRA_POI_JSON, gson.toJson(poi));
        startActivity(intent);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView == null) return;
        Bundle mapBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapBundle == null) {
            mapBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapBundle);
        }
        mapView.onSaveInstanceState(mapBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
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

//     Connect API
    private void loadRecommendedPOIs() {
        UserAPI apiService = RetrofitClient.getInstance().getPOIAPI();

        apiService.getRecommendedPOIs("vi").enqueue(new Callback<List<POI>>() {
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

                    featuredPOIAdapter.notifyDataSetChanged();
                    explorePOIAdapter.notifyDataSetChanged();
                } else {
                    AppToast.showError(ExploreActivity.this, "Không lấy được dữ liệu địa điểm");
                    Log.e("API_POI", "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<POI>> call, Throwable t) {
                AppToast.showError(ExploreActivity.this, "Lỗi kết nối API");
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
}