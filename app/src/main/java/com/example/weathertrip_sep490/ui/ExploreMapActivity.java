package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.view.View;
import com.google.android.material.card.MaterialCardView;
import com.example.weathertrip_sep490.adapter.SearchSuggestionAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.adapter.PoiRecentAdapter;
import com.example.weathertrip_sep490.model.POI;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_POIS_JSON = "pois_json";

    private static final String MAPVIEW_BUNDLE_KEY = "ExploreMapViewBundle";
    private static final Gson gson = new Gson();

    private MapView mapView;
    private GoogleMap googleMap;
    private boolean mapReady = false;

    private final List<POI> pois = new ArrayList<>();
    private final List<POI> filteredPois = new ArrayList<>();

    private TextView tvMapCount;
    private ImageButton btnBack;
    private RecyclerView rvSuggestions;

    private EditText etSearchMap;
    private ChipGroup chipGroupPoiFilters;

    private MaterialCardView cardSearchSuggestions;
    private RecyclerView rvSearchAutoComplete;
    private SearchSuggestionAdapter searchSuggestionAdapter;
    private boolean isSelectingFromSuggestions = false;

    private String activeCategory = "ALL";
    private String searchQuery = "";

    private PoiRecentAdapter suggestionsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_map);

        tvMapCount = findViewById(R.id.tvMapPlaceCount);
        btnBack = findViewById(R.id.btnBackMap);
        rvSuggestions = findViewById(R.id.rvPoiSuggestions);
        etSearchMap = findViewById(R.id.etSearchMap);
        chipGroupPoiFilters = findViewById(R.id.chipGroupPoiFilters);
        cardSearchSuggestions = findViewById(R.id.cardSearchSuggestions);
        rvSearchAutoComplete = findViewById(R.id.rvSearchAutoComplete);
        setupSearchSuggestions();

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        FloatingActionButton btnZoomIn = findViewById(R.id.btnZoomIn);
        FloatingActionButton btnZoomOut = findViewById(R.id.btnZoomOut);
        if (btnZoomIn != null) {
            btnZoomIn.setOnClickListener(v -> {
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.zoomIn());
                }
            });
        }
        if (btnZoomOut != null) {
            btnZoomOut.setOnClickListener(v -> {
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.zoomOut());
                }
            });
        }

        // Parse POIs from intent (can be empty if caller didn't pass data)
        String poisJson = getIntent().getStringExtra(EXTRA_POIS_JSON);
        if (poisJson != null && !poisJson.trim().isEmpty()) {
            Type listType = new TypeToken<List<POI>>() {}.getType();
            try {
                List<POI> parsed = gson.fromJson(poisJson, listType);
                if (parsed != null) {
                    if (parsed.size() > 10) {
                        pois.addAll(parsed.subList(0, 10));
                    } else {
                        pois.addAll(parsed);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        filteredPois.clear();
        filteredPois.addAll(pois);

        setupFiltersAndSearch();
        setupSuggestions();
        initMapView(savedInstanceState);

        // Nếu caller không truyền POIs (hoặc trả rỗng), tự gọi API lấy POI thật
        if (pois.isEmpty()) {
            loadPoisFromApi();
        } else {
            // Nếu đã có POIs sẵn thì refresh lại UI lần đầu
            recomputeFilteredPoisAndRefreshUi();
        }
    }

    private void loadPoisFromApi() {
        UserAPI api = RetrofitClient.getInstance().getPOIAPI();
        api.getRecommendedPOIs("vi").enqueue(new Callback<List<POI>>() {
            @Override
            public void onResponse(Call<List<POI>> call, Response<List<POI>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ExploreMapActivity.this, "Không lấy được POI", Toast.LENGTH_SHORT).show();
                    return;
                }
                pois.clear();
                List<POI> body = response.body();
                if (body != null) {
                    if (body.size() > 10) {
                        pois.addAll(body.subList(0, 10));
                    } else {
                        pois.addAll(body);
                    }
                }
                recomputeFilteredPoisAndRefreshUi();
            }

            @Override
            public void onFailure(Call<List<POI>> call, Throwable t) {
                Toast.makeText(ExploreMapActivity.this, "Lỗi kết nối API POI", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFiltersAndSearch() {
        // Search
        if (etSearchMap != null) {
            etSearchMap.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    searchQuery = s == null ? "" : s.toString();
                    if (!isSelectingFromSuggestions) {
                        updateSearchSuggestionsPopup(searchQuery);
                    }
                    recomputeFilteredPoisAndRefreshUi();
                }
            });
        }

        // Category filter
        if (chipGroupPoiFilters != null) {
            chipGroupPoiFilters.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == -1) return;
                Chip chip = group.findViewById(checkedId);
                if (chip == null) return;
                Object tag = chip.getTag();
                activeCategory = tag == null ? "ALL" : tag.toString();
                recomputeFilteredPoisAndRefreshUi();
            });
        }
    }

    private void setupSearchSuggestions() {
        if (rvSearchAutoComplete == null) return;
        rvSearchAutoComplete.setLayoutManager(new LinearLayoutManager(this));
        searchSuggestionAdapter = new SearchSuggestionAdapter(poi -> {
            if (poi == null) return;
            isSelectingFromSuggestions = true;
            etSearchMap.setText(poi.getName());
            searchQuery = poi.getName();
            isSelectingFromSuggestions = false;
            
            if (cardSearchSuggestions != null) {
                cardSearchSuggestions.setVisibility(View.GONE);
            }
            
            recomputeFilteredPoisAndRefreshUi();
            
            if (googleMap != null) {
                LatLng pos = new LatLng(poi.getLatitude(), poi.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f));
            }
        });
        rvSearchAutoComplete.setAdapter(searchSuggestionAdapter);
    }

    private void updateSearchSuggestionsPopup(String query) {
        if (cardSearchSuggestions == null || searchSuggestionAdapter == null) return;
        
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) {
            cardSearchSuggestions.setVisibility(View.GONE);
            return;
        }
        
        List<POI> suggestions = new ArrayList<>();
        for (POI poi : pois) {
            if (poi == null) continue;
            String name = poi.getName() == null ? "" : poi.getName().toLowerCase();
            String city = poi.getCity() == null ? "" : poi.getCity().toLowerCase();
            String type = poi.getType() == null ? "" : poi.getType().toLowerCase();
            if (name.contains(q) || city.contains(q) || type.contains(q)) {
                suggestions.add(poi);
            }
        }
        
        if (suggestions.isEmpty()) {
            cardSearchSuggestions.setVisibility(View.GONE);
        } else {
            searchSuggestionAdapter.updateData(suggestions);
            cardSearchSuggestions.setVisibility(View.VISIBLE);
        }
    }

    private void setupSuggestions() {
        if (rvSuggestions == null) return;
        rvSuggestions.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        suggestionsAdapter = new PoiRecentAdapter(poi -> {
            if (poi != null && googleMap != null) {
                LatLng pos = new LatLng(poi.getLatitude(), poi.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
            }
        });
        rvSuggestions.setAdapter(suggestionsAdapter);
        recomputeFilteredPoisAndRefreshUi();
    }

    private void recomputeFilteredPoisAndRefreshUi() {
        filteredPois.clear();
        String q = searchQuery == null ? "" : searchQuery.trim().toLowerCase();

        for (POI poi : pois) {
            if (poi == null) continue;
            if (!matchesCategory(poi)) continue;
            if (!q.isEmpty() && !matchesQuery(poi, q)) continue;
            filteredPois.add(poi);
        }

        if (tvMapCount != null) {
            tvMapCount.setText(filteredPois.size() + " địa điểm");
        }

        refreshSuggestionList();
        applyPoiMarkers();
    }

    private boolean matchesCategory(POI poi) {
        if (activeCategory == null || activeCategory.trim().isEmpty()) return true;
        if ("ALL".equalsIgnoreCase(activeCategory)) return true;

        String t = poi.getType() == null ? "" : poi.getType().trim();
        String cat = activeCategory.trim();

        if ("Cafe".equalsIgnoreCase(cat)) {
            return "Cafe".equalsIgnoreCase(t);
        }
        if ("Restaurant".equalsIgnoreCase(cat)) {
            return "Restaurant".equalsIgnoreCase(t);
        }
        if ("Nightlife".equalsIgnoreCase(cat)) {
            // Gom vài loại "giải trí về đêm"
            return "Nightlife".equalsIgnoreCase(t)
                    || "Bar".equalsIgnoreCase(t)
                    || "NightMarket".equalsIgnoreCase(t);
        }
        if ("CULTURE".equalsIgnoreCase(cat)) {
            return "CulturalSite".equalsIgnoreCase(t)
                    || "Museum".equalsIgnoreCase(t)
                    || "HistoricalSite".equalsIgnoreCase(t)
                    || "Temple".equalsIgnoreCase(t)
                    || "Church".equalsIgnoreCase(t);
        }
        if ("NATURE".equalsIgnoreCase(cat)) {
            return "Nature".equalsIgnoreCase(t)
                    || "Park".equalsIgnoreCase(t)
                    || "Waterfall".equalsIgnoreCase(t)
                    || "Beach".equalsIgnoreCase(t)
                    || "Resort".equalsIgnoreCase(t);
        }

        // Fallback: match theo đúng chuỗi
        return t.equalsIgnoreCase(cat);
    }

    private boolean matchesQuery(POI poi, String q) {
        if (q == null || q.trim().isEmpty()) return true;
        String name = poi.getName() == null ? "" : poi.getName().toLowerCase();
        String city = poi.getCity() == null ? "" : poi.getCity().toLowerCase();
        String address = poi.getAddress() == null ? "" : poi.getAddress().toLowerCase();
        String type = poi.getType() == null ? "" : poi.getType().toLowerCase();
        return name.contains(q) || city.contains(q) || address.contains(q) || type.contains(q);
    }

    private void refreshSuggestionList() {
        if (suggestionsAdapter == null) return;
        if (filteredPois.isEmpty()) {
            suggestionsAdapter.updateData(new ArrayList<>());
            return;
        }

        // Giới hạn số item hiển thị để giống UI demo
        List<POI> limited = filteredPois.size() > 5 ? filteredPois.subList(0, 5) : filteredPois;
        suggestionsAdapter.updateData(new ArrayList<>(limited));
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

    private void initMapView(@Nullable Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapExploreFull);
        Bundle mapBundle = null;
        if (savedInstanceState != null) {
            mapBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
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
        if (filteredPois.isEmpty()) {
            Toast.makeText(this, "Chưa có địa điểm để hiển thị", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyPoiMarkers() {
        if (!mapReady || googleMap == null) return;
        googleMap.clear();
        if (filteredPois.isEmpty()) return;

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

        // Nếu nhiều POI có cùng tọa độ (overlap) thì sẽ hiện như 1 pin.
        // Để hiển thị đủ từng POI, mình tách vị trí nhẹ (jitter) theo số lượng POI trùng tọa độ.
        final double earthRadiusMeters = 6378137.0;
        final double jitterRadiusMeters = 45.0; // bán kính tách pin nhỏ, đủ nhìn rõ

        // Group theo lat/lng làm tròn 5 chữ số thập phân (~1.1m)
        java.util.Map<String, List<POI>> clusters = new java.util.HashMap<>();
        for (POI poi : filteredPois) {
            if (poi == null) continue;
            double lat = poi.getLatitude();
            double lng = poi.getLongitude();
            if (lat == 0.0d && lng == 0.0d) continue;
            long latKey = Math.round(lat * 100000d);
            long lngKey = Math.round(lng * 100000d);
            String key = latKey + "_" + lngKey;
            List<POI> list = clusters.get(key);
            if (list == null) {
                list = new ArrayList<>();
                clusters.put(key, list);
            }
            list.add(poi);
        }

        LatLng first = null;
        int added = 0;
        for (java.util.Map.Entry<String, List<POI>> entry : clusters.entrySet()) {
            List<POI> group = entry.getValue();
            if (group == null || group.isEmpty()) continue;

            // Lấy tọa độ base theo phần tử đầu tiên
            POI basePoi = group.get(0);
            double baseLat = basePoi.getLatitude();
            double baseLng = basePoi.getLongitude();
            if (baseLat == 0.0d && baseLng == 0.0d) continue;

            int n = group.size();
            for (int i = 0; i < n; i++) {
                POI poi = group.get(i);
                if (poi == null) continue;

                double lat = baseLat;
                double lng = baseLng;

                if (n > 1) {
                    // Offset theo vòng tròn quanh vị trí base
                    double angle = (2.0 * Math.PI * i) / n;
                    double latRad = Math.toRadians(baseLat);
                    double dx = jitterRadiusMeters * Math.cos(angle);
                    double dy = jitterRadiusMeters * Math.sin(angle);

                    // offset lat: dx along meridian
                    double offsetLatRad = dx / earthRadiusMeters;
                    // offset lng: dy along parallel, scale theo cos(lat)
                    double offsetLngRad = dy / (earthRadiusMeters * Math.cos(latRad));

                    lat = baseLat + Math.toDegrees(offsetLatRad);
                    lng = baseLng + Math.toDegrees(offsetLngRad);
                }

                LatLng pos = new LatLng(lat, lng);
                if (first == null) first = pos;

                Marker m = googleMap.addMarker(
                        new MarkerOptions()
                                .position(pos)
                                .title(safe(poi.getName())));
                if (m != null) {
                    m.setTag(poi);
                    bounds.include(pos);
                    added++;
                }
            }
        }

        if (added == 0) return;
        try {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80));
        } catch (Exception ignored) {
            if (first != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(first, 12f));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
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
}

