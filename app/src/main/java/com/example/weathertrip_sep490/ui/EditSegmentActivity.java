package com.example.weathertrip_sep490.ui;

import com.example.weathertrip_sep490.util.AppToast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.EditSegmentAdapter;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.model.LocationOption;
import com.example.weathertrip_sep490.model.PlannerGenerateResponse;
import com.example.weathertrip_sep490.model.PlannerTripResponse;
import com.example.weathertrip_sep490.model.TripSegmentResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditSegmentActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String tripId;
    private RecyclerView rvSegments;
    private ProgressBar pbLoading;
    private TextView tvEmptyState;
    private MaterialButton btnGenerateAi;
    
    private EditSegmentAdapter adapter;
    private PlannerTripResponse plannerResponse;
    private boolean isDataChanged = false;

    // Google Maps fields
    private com.google.android.gms.maps.MapView mapView;
    private GoogleMap googleMap;
    private boolean mapReady = false;
    private final List<Marker> mapMarkers = new ArrayList<>();
    private final List<com.google.android.gms.maps.model.Polyline> mapPolylines = new ArrayList<>();

    // Review Card fields
    private TextView tvReviewTripTitle;
    private TextView tvReviewStartLocation;
    private TextView tvReviewStartDistrict;
    private TextView tvReviewSegmentLocation;
    private TextView tvReviewSegmentDistrict;
    private TextView tvReviewSegmentDates;

    // Intent extras passed along for redirection
    private String extraCity;
    private String extraTripTitle;
    private String extraDates;
    private String extraStartPoint;
    private String extraDestination;
    private String extraRoute;
    private boolean isCreationFlow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_segment);

        tripId = getIntent().getStringExtra("planner_trip_id");
        if (tripId == null || tripId.trim().isEmpty()) {
            AppToast.show(this, "Không tìm thấy thông tin chuyến đi");
            finish();
            return;
        }

        // Retrieve redirection parameters
        extraCity = getIntent().getStringExtra(TripDetailActivity.EXTRA_CITY);
        extraTripTitle = getIntent().getStringExtra(TripDetailActivity.EXTRA_TRIP_TITLE);
        extraDates = getIntent().getStringExtra(TripDetailActivity.EXTRA_DATES);
        extraStartPoint = getIntent().getStringExtra(TripDetailActivity.EXTRA_START_POINT);
        extraDestination = getIntent().getStringExtra(TripDetailActivity.EXTRA_DESTINATION);
        extraRoute = getIntent().getStringExtra(TripDetailActivity.EXTRA_ROUTE);
        isCreationFlow = getIntent().getBooleanExtra("is_creation_flow", false);

        // Initialize UI components
        rvSegments = findViewById(R.id.rvSegments);
        pbLoading = findViewById(R.id.pbLoading);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnGenerateAi = findViewById(R.id.btnGenerateAi);
        View btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        // Initialize Review Card components
        tvReviewTripTitle = findViewById(R.id.tvReviewTripTitle);
        tvReviewStartLocation = findViewById(R.id.tvReviewStartLocation);
        tvReviewStartDistrict = findViewById(R.id.tvReviewStartDistrict);
        tvReviewSegmentLocation = findViewById(R.id.tvReviewSegmentLocation);
        tvReviewSegmentDistrict = findViewById(R.id.tvReviewSegmentDistrict);
        tvReviewSegmentDates = findViewById(R.id.tvReviewSegmentDates);

        // Initialize and setup MapView
        mapView = findViewById(R.id.mapEditSegment);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Setup RecyclerView
        rvSegments.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new EditSegmentAdapter(this, tripId, (segmentId, newLocationName, newDistrictName, newDates) -> {
            isDataChanged = true;
            // Also update general trip preferences if the updated segment is the main stopover segment
            updateGeneralPreferencesIfMain(segmentId, newLocationName, newDistrictName, newDates);
        });
        rvSegments.setAdapter(adapter);

        btnGenerateAi.setOnClickListener(v -> generateAiItinerary());

        // Load data
        loadData();
    }

    private void loadData() {
        pbLoading.setVisibility(View.VISIBLE);
        rvSegments.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
        btnGenerateAi.setEnabled(false);

        // First load locations
        RetrofitClient.getInstance().getUserAPI().getAllLocations().enqueue(new Callback<List<LocationOption>>() {
            @Override
            public void onResponse(@NonNull Call<List<LocationOption>> call, @NonNull Response<List<LocationOption>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setLocations(response.body());
                    // Then load segments
                    loadPlannerSegments();
                } else {
                    pbLoading.setVisibility(View.GONE);
                    AppToast.show(EditSegmentActivity.this, "Không tải được danh sách địa điểm");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LocationOption>> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                AppToast.show(EditSegmentActivity.this, "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void loadPlannerSegments() {
        RetrofitClient.getInstance().getUserAPI().getPlanner(tripId).enqueue(new Callback<PlannerTripResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlannerTripResponse> call, @NonNull Response<PlannerTripResponse> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getSegments() != null) {
                    plannerResponse = response.body();
                    List<TripSegmentResponse> segList = plannerResponse.getSegments();
                    java.util.Collections.sort(segList, (s1, s2) -> Integer.compare(s1.getOrderIndex(), s2.getOrderIndex()));
                    initializeSegmentPrefs(plannerResponse);
                    populateReviewCard();
                    applyMapMarkers();
                    if (segList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        btnGenerateAi.setEnabled(false);
                    } else {
                        rvSegments.setVisibility(View.VISIBLE);
                        adapter.updateData(segList);
                        btnGenerateAi.setEnabled(true);
                    }
                } else {
                    AppToast.show(EditSegmentActivity.this, "Không tải được thông tin chặng đi");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlannerTripResponse> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                AppToast.show(EditSegmentActivity.this, "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void generateAiItinerary() {
        if (plannerResponse == null || plannerResponse.getSegments() == null) {
            AppToast.show(this, "Chưa tải xong thông tin chặng đi");
            return;
        }

        // Validate that each segment has an edited district in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", Context.MODE_PRIVATE);
        for (TripSegmentResponse seg : plannerResponse.getSegments()) {
            String key = "trip_segment_district_" + tripId + "_" + seg.getSegmentId();
            String dist = prefs.getString(key, null);
            if (dist == null || dist.trim().isEmpty() || dist.equalsIgnoreCase("Chọn quận/huyện") || dist.equalsIgnoreCase("Chặng dừng")) {
                AppToast.show(this, "Vui lòng chọn đầy đủ quận/huyện cho tất cả các chặng dừng chân trước khi tạo lịch trình AI");
                return;
            }
        }

        btnGenerateAi.setEnabled(false);
        btnGenerateAi.setText("Đang tạo lịch trình AI...");
        pbLoading.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance().getUserAPI().generatePlanner(tripId).enqueue(new Callback<PlannerGenerateResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlannerGenerateResponse> call, @NonNull Response<PlannerGenerateResponse> response) {
                pbLoading.setVisibility(View.GONE);
                btnGenerateAi.setEnabled(true);
                btnGenerateAi.setText("Tạo lịch trình AI");

                if (!response.isSuccessful()) {
                    AppToast.show(EditSegmentActivity.this, "Tạo lịch trình AI thất bại");
                    return;
                }

                AppToast.show(EditSegmentActivity.this, "AI đã tạo lịch trình thành công!");

                if (isCreationFlow) {
                    // Redirect directly to TripDetailActivity
                    Intent intent = new Intent(EditSegmentActivity.this, TripDetailActivity.class);
                    intent.putExtra("planner_trip_id", tripId);
                    intent.putExtra(TripDetailActivity.EXTRA_CITY, extraCity);
                    intent.putExtra(TripDetailActivity.EXTRA_TRIP_TITLE, extraTripTitle);
                    intent.putExtra(TripDetailActivity.EXTRA_DATES, extraDates);
                    intent.putExtra(TripDetailActivity.EXTRA_START_POINT, extraStartPoint);
                    intent.putExtra(TripDetailActivity.EXTRA_DESTINATION, extraDestination);
                    intent.putExtra(TripDetailActivity.EXTRA_ROUTE, extraRoute);
                    startActivity(intent);
                    finish();
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlannerGenerateResponse> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                btnGenerateAi.setEnabled(true);
                btnGenerateAi.setText("Tạo lịch trình AI");
                AppToast.show(EditSegmentActivity.this, "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void updateGeneralPreferencesIfMain(String segmentId, String newLocationName, String newDistrictName, String newDates) {
        if (plannerResponse == null || plannerResponse.getSegments() == null) return;
        List<TripSegmentResponse> segments = plannerResponse.getSegments();
        SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int index = -1;
        for (int i = 0; i < segments.size(); i++) {
            if (segmentId.equals(segments.get(i).getSegmentId())) {
                index = i;
                break;
            }
        }

        if (index == 0) {
            editor.putString("trip_start_location_" + tripId, newLocationName);
            editor.putString("trip_start_district_" + tripId, newDistrictName);
        }

        if (index == segments.size() - 1) {
            editor.putString("trip_end_location_" + tripId, newLocationName);
            editor.putString("trip_end_district_" + tripId, newDistrictName);
        }

        TripSegmentResponse mainSeg = getStopSegmentToEdit(plannerResponse);
        if (mainSeg != null && segmentId.equals(mainSeg.getSegmentId())) {
            editor.putString("trip_segment_location_" + tripId, newLocationName)
                  .putString("trip_segment_district_" + tripId, newDistrictName)
                  .putString("trip_segment_dates_" + tripId, newDates);
        }
        editor.apply();

        // Dynamically update map and review card on the EditSegmentActivity UI immediately
        populateReviewCard();
        applyMapMarkers();
    }

    private void populateReviewCard() {
        SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);

        String localTitle = prefs.getString("trip_local_title_" + tripId, extraTripTitle);
        if (localTitle == null || localTitle.trim().isEmpty()) {
            localTitle = extraTripTitle != null ? extraTripTitle : "--";
        }
        tvReviewTripTitle.setText(localTitle);

        String localStartLoc = prefs.getString("trip_start_location_" + tripId, extraStartPoint);
        if (localStartLoc == null || localStartLoc.trim().isEmpty()) {
            localStartLoc = extraStartPoint != null ? extraStartPoint : "--";
        }
        tvReviewStartLocation.setText(localStartLoc);

        String localStartDist = prefs.getString("trip_start_district_" + tripId, "--");
        tvReviewStartDistrict.setText(localStartDist);

        String localSegLoc = prefs.getString("trip_segment_location_" + tripId, null);
        if (localSegLoc == null || localSegLoc.trim().isEmpty()) {
            localSegLoc = extraDestination != null ? extraDestination : "--";
        }
        tvReviewSegmentLocation.setText(localSegLoc);

        String localSegDist = prefs.getString("trip_segment_district_" + tripId, null);
        if (localSegDist == null || localSegDist.trim().isEmpty()) {
            localSegDist = prefs.getString("trip_end_district_" + tripId, "--");
        }
        tvReviewSegmentDistrict.setText(localSegDist);

        String localSegDates = prefs.getString("trip_segment_dates_" + tripId, null);
        if (localSegDates == null || localSegDates.trim().isEmpty()) {
            if (plannerResponse != null && plannerResponse.getSegments() != null) {
                TripSegmentResponse seg = getStopSegmentToEdit(plannerResponse);
                if (seg != null && seg.getStartDate() != null && seg.getEndDate() != null) {
                    try {
                        java.text.DateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        localSegDates = df.format(seg.getStartDate()) + " - " + df.format(seg.getEndDate());
                    } catch (Exception ignored) {}
                }
            }
            if (localSegDates == null || localSegDates.trim().isEmpty()) {
                localSegDates = extraDates != null ? extraDates : "--";
            }
        }
        tvReviewSegmentDates.setText(localSegDates);
    }

    private void applyMapMarkers() {
        if (!mapReady || googleMap == null) return;
        for (Marker m : mapMarkers) {
            m.remove();
        }
        mapMarkers.clear();

        for (com.google.android.gms.maps.model.Polyline p : mapPolylines) {
            p.remove();
        }
        mapPolylines.clear();

        SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
        String polylineJson = prefs.getString("route_polyline_" + tripId, null);
        if (polylineJson != null && !polylineJson.trim().isEmpty()) {
            try {
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<com.example.weathertrip_sep490.model.RoutePolylinePointDto>>(){}.getType();
                List<com.example.weathertrip_sep490.model.RoutePolylinePointDto> polyPoints = new com.google.gson.Gson().fromJson(polylineJson, listType);
                if (polyPoints != null && !polyPoints.isEmpty()) {
                    List<LatLng> points = new ArrayList<>();
                    for (com.example.weathertrip_sep490.model.RoutePolylinePointDto p : polyPoints) {
                        points.add(new LatLng(p.getLatitude(), p.getLongitude()));
                    }
                    if (!points.isEmpty()) {
                        com.google.android.gms.maps.model.Polyline poly = googleMap.addPolyline(new com.google.android.gms.maps.model.PolylineOptions()
                                .addAll(points)
                                .color(androidx.core.content.ContextCompat.getColor(this, R.color.green_primary))
                                .width(14f));
                        mapPolylines.add(poly);

                        LatLng startLatLng = points.get(0);
                        LatLng endLatLng = points.get(points.size() - 1);

                        Marker startMarker = googleMap.addMarker(new MarkerOptions()
                                .position(startLatLng)
                                .title("Điểm xuất phát")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        if (startMarker != null) mapMarkers.add(startMarker);

                        Marker endMarker = googleMap.addMarker(new MarkerOptions()
                                .position(endLatLng)
                                .title("Điểm đến")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        if (endMarker != null) mapMarkers.add(endMarker);

                        // Draw intermediate stop markers
                        String nodesJson = prefs.getString("route_nodes_" + tripId, null);
                        if (nodesJson != null && !nodesJson.trim().isEmpty()) {
                            try {
                                java.lang.reflect.Type stringListType = new com.google.gson.reflect.TypeToken<List<String>>(){}.getType();
                                List<String> routeNodes = new com.google.gson.Gson().fromJson(nodesJson, stringListType);
                                if (routeNodes != null && routeNodes.size() > 2) {
                                    for (int i = 1; i < routeNodes.size() - 1; i++) {
                                        String nodeName = routeNodes.get(i);
                                        if (points.size() > 2) {
                                            int idx = (int) (((double) i / (routeNodes.size() - 1)) * (points.size() - 1));
                                            if (idx > 0 && idx < points.size() - 1) {
                                                LatLng intermediatePos = points.get(idx);
                                                Marker interMarker = googleMap.addMarker(new MarkerOptions()
                                                        .position(intermediatePos)
                                                        .title(nodeName)
                                                        .snippet("Điểm dừng chân")
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                                if (interMarker != null) mapMarkers.add(interMarker);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ignored) {}
                        }

                        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                        for (LatLng latLng : points) {
                            bounds.include(latLng);
                        }
                        try {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80));
                        } catch (Exception ignored) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.build().getCenter(), 10f));
                        }
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(16.0, 107.5), 5.5f));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        mapReady = true;
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        applyMapMarkers();
    }

    private void initializeSegmentPrefs(PlannerTripResponse response) {
        if (response == null || response.getSegments() == null) return;
        List<TripSegmentResponse> segList = response.getSegments();
        SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
        String nodesJson = prefs.getString("route_nodes_" + tripId, null);
        List<String> routeNodes = null;
        if (nodesJson != null && !nodesJson.trim().isEmpty()) {
            try {
                java.lang.reflect.Type stringListType = new com.google.gson.reflect.TypeToken<List<String>>(){}.getType();
                routeNodes = new com.google.gson.Gson().fromJson(nodesJson, stringListType);
            } catch (Exception ignored) {}
        }

        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < segList.size(); i++) {
            TripSegmentResponse seg = segList.get(i);
            if (seg == null) continue;
            String segId = seg.getSegmentId();
            String locKey = "trip_segment_location_" + tripId + "_" + segId;

            String nodeName = null;
            if (routeNodes != null && i < routeNodes.size()) {
                nodeName = routeNodes.get(i);
            }
            if (nodeName == null || nodeName.trim().isEmpty()) {
                nodeName = (extraDestination != null && !extraDestination.trim().isEmpty()) ? extraDestination : "Chặng dừng";
            }

            if (isCreationFlow || !prefs.contains(locKey)) {
                editor.putString(locKey, nodeName);
            }

            String distKey = "trip_segment_district_" + tripId + "_" + segId;
            if (isCreationFlow || !prefs.contains(distKey)) {
                TripSegmentResponse targetSeg = getStopSegmentToEdit(response);
                if (targetSeg != null && segId.equals(targetSeg.getSegmentId())) {
                    String generalDist = prefs.getString("trip_segment_district_" + tripId, null);
                    if (generalDist != null && !generalDist.trim().isEmpty()) {
                        editor.putString(distKey, generalDist);
                    } else {
                        editor.putString(distKey, "Chặng dừng");
                    }
                } else {
                    editor.putString(distKey, "Chặng dừng");
                }
            }
        }
        editor.apply();
    }

    private TripSegmentResponse getStopSegmentToEdit(PlannerTripResponse response) {
        if (response == null || response.getSegments() == null || response.getSegments().isEmpty()) {
            return null;
        }
        List<TripSegmentResponse> segments = response.getSegments();
        if (segments.size() >= 3) {
            return segments.get(1);
        }
        return segments.get(segments.size() - 1);
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
    protected void onStop() {
        if (mapView != null) mapView.onStop();
        super.onStop();
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

    @Override
    public void onBackPressed() {
        if (isDataChanged) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }
}
