package com.example.weathertrip_sep490.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.PlannerGenerateResponse;
import com.example.weathertrip_sep490.model.RouteOption;
import com.example.weathertrip_sep490.model.RoutePolylinePointDto;
import com.example.weathertrip_sep490.model.RouteSuggestionResponse;
import com.example.weathertrip_sep490.model.Trip;
import com.example.weathertrip_sep490.model.TripStatus;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectRouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "SelectRouteActivity";
    private static final String MAPVIEW_BUNDLE_KEY = "SelectRouteMapViewBundle";
    private static final String PREFS_NAME = "TravelGoPrefs";
    private static final String KEY_MANAGED_TRIPS_PREFIX = "managed_trips_json_";

    private MapView mapView;
    private GoogleMap googleMap;
    private boolean mapReady = false;

    // Intents parameters
    private String tripId;
    private String tripTitle;
    private String startPoint;
    private String destination;
    private String startDateIso;
    private String endDateIso;
    private String startDateDisplay;
    private String endDateDisplay;
    private boolean roundTrip;

    // Routes list and selected route
    private final List<RouteOption> routeOptions = new ArrayList<>();
    private RouteOption selectedRoute = null;
    private final List<Polyline> mapPolylines = new ArrayList<>();
    private final List<Marker> mapMarkers = new ArrayList<>();

    // Views
    private LinearLayout layoutOptionsList;
    private TextView tvDetailRouteName;
    private TextView tvDetailRouteDistance;
    private TextView tvDetailRouteNodes;
    private TextView tvSelectRouteSub;
    private MaterialButton btnApplyRoute;
    private View layoutLoading;
    private TextView tvLoadingText;

    private View layoutWeatherSummary;
    private TextView tvDetailRouteWeatherSummary;
    private View cardAdvice;
    private TextView tvDetailRouteAdvice;
    private TextView tvDetailRouteWarnings;
    private ProgressBar pbAdviceLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_route);

        // Parse intents parameters
        Intent intent = getIntent();
        tripId = intent.getStringExtra("trip_id");
        tripTitle = intent.getStringExtra("trip_title");
        startPoint = intent.getStringExtra("start_point");
        destination = intent.getStringExtra("destination");
        startDateIso = intent.getStringExtra("start_date_iso");
        endDateIso = intent.getStringExtra("end_date_iso");
        startDateDisplay = intent.getStringExtra("start_date_display");
        endDateDisplay = intent.getStringExtra("end_date_display");
        roundTrip = intent.getBooleanExtra("round_trip", false);

        if (tripId == null || tripId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin chuyến đi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Views
        layoutOptionsList = findViewById(R.id.layoutRouteOptionsList);
        tvDetailRouteName = findViewById(R.id.tvDetailRouteName);
        tvDetailRouteDistance = findViewById(R.id.tvDetailRouteDistance);
        tvDetailRouteNodes = findViewById(R.id.tvDetailRouteNodes);
        tvSelectRouteSub = findViewById(R.id.tvSelectRouteSub);
        btnApplyRoute = findViewById(R.id.btnApplySelectedRoute);
        layoutLoading = findViewById(R.id.layoutSelectRouteLoading);
        tvLoadingText = findViewById(R.id.tvSelectRouteLoadingText);

        layoutWeatherSummary = findViewById(R.id.layoutWeatherSummary);
        tvDetailRouteWeatherSummary = findViewById(R.id.tvDetailRouteWeatherSummary);
        cardAdvice = findViewById(R.id.cardAdvice);
        tvDetailRouteAdvice = findViewById(R.id.tvDetailRouteAdvice);
        tvDetailRouteWarnings = findViewById(R.id.tvDetailRouteWarnings);
        pbAdviceLoading = findViewById(R.id.pbAdviceLoading);

        if (startPoint != null && destination != null) {
            tvSelectRouteSub.setText(String.format("Hành trình từ %s đi %s", startPoint, destination));
        }

        // Click listeners
        findViewById(R.id.btnBackSelectRoute).setOnClickListener(v -> finish());

        FloatingActionButton btnZoomIn = findViewById(R.id.btnZoomInSelectRoute);
        FloatingActionButton btnZoomOut = findViewById(R.id.btnZoomOutSelectRoute);
        if (btnZoomIn != null) {
            btnZoomIn.setOnClickListener(v -> {
                if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            });
        }
        if (btnZoomOut != null) {
            btnZoomOut.setOnClickListener(v -> {
                if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomOut());
            });
        }

        btnApplyRoute.setOnClickListener(v -> applySelectedRoute());

        // Initialize MapView
        mapView = findViewById(R.id.mapSelectRoute);
        Bundle mapBundle = null;
        if (savedInstanceState != null) {
            mapBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapBundle);
        mapView.getMapAsync(this);

        // Fetch Routes
        fetchAvailableRoutes();
    }

    private void fetchAvailableRoutes() {
        showLoading("Đang tải các tuyến đường có sẵn...");
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getAvailableRoutes(tripId).enqueue(new Callback<List<RouteOption>>() {
            @Override
            public void onResponse(@NonNull Call<List<RouteOption>> call, @NonNull Response<List<RouteOption>> response) {
                hideLoading();
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(SelectRouteActivity.this, "Không lấy được danh sách tuyến đường", Toast.LENGTH_SHORT).show();
                    return;
                }
                routeOptions.clear();
                routeOptions.addAll(response.body());

                if (routeOptions.isEmpty()) {
                    Toast.makeText(SelectRouteActivity.this, "Không có tuyến đường nào được tìm thấy", Toast.LENGTH_SHORT).show();
                    return;
                }

                populateRouteSelectors();
                selectRoute(routeOptions.get(0));
            }

            @Override
            public void onFailure(@NonNull Call<List<RouteOption>> call, @NonNull Throwable t) {
                hideLoading();
                Toast.makeText(SelectRouteActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateRouteSelectors() {
        if (layoutOptionsList == null) return;
        layoutOptionsList.removeAllViews();

        for (int i = 0; i < routeOptions.size(); i++) {
            final RouteOption option = routeOptions.get(i);
            TextView tab = new TextView(this);
            tab.setText(String.format("Tuyến %d", option.getRouteIndex()));
            tab.setTextSize(14);
            tab.setPadding(32, 16, 32, 16);
            tab.setGravity(android.view.Gravity.CENTER);
            tab.setClickable(true);
            tab.setFocusable(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            tab.setLayoutParams(params);

            final int index = i;
            tab.setOnClickListener(v -> selectRoute(option));

            layoutOptionsList.addView(tab);
        }
    }

    private void selectRoute(RouteOption option) {
        selectedRoute = option;

        // Update selector tabs visual state
        if (layoutOptionsList != null) {
            for (int i = 0; i < layoutOptionsList.getChildCount(); i++) {
                TextView tab = (TextView) layoutOptionsList.getChildAt(i);
                if (i == routeOptions.indexOf(option)) {
                    tab.setBackgroundResource(R.drawable.bg_trip_status_thumb);
                    tab.setTextColor(ContextCompat.getColor(this, R.color.text_title));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tab.setElevation(2f);
                    }
                } else {
                    tab.setBackground(null);
                    tab.setTextColor(Color.parseColor("#9CA3AF"));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tab.setElevation(0f);
                    }
                }
            }
        }

        // Update Details Text
        tvDetailRouteName.setText(String.format("Tuyến đường %d", option.getRouteIndex()));
        tvDetailRouteDistance.setText(String.format(Locale.getDefault(), "%.1f km", option.getTotalDistanceKm()));

        if (option.getNodes() != null && !option.getNodes().isEmpty()) {
            StringBuilder sb = new StringBuilder("Đi qua: ");
            for (int i = 0; i < option.getNodes().size(); i++) {
                sb.append(option.getNodes().get(i));
                if (i < option.getNodes().size() - 1) {
                    sb.append(" ➔ ");
                }
            }
            tvDetailRouteNodes.setText(sb.toString());
        } else {
            tvDetailRouteNodes.setText("Tuyến đường trực tiếp");
        }

        // Draw Polylines on Map
        drawRoutePolylines();

        // Fetch AI advice and weather summary for selected route
        fetchRouteAdvice(option.getRouteId());
    }

    private void fetchRouteAdvice(String routeId) {
        if (routeId == null || routeId.trim().isEmpty() || tripId == null) return;

        // Show loading state for advice
        if (pbAdviceLoading != null) pbAdviceLoading.setVisibility(View.VISIBLE);
        if (cardAdvice != null) cardAdvice.setVisibility(View.VISIBLE);
        if (tvDetailRouteAdvice != null) tvDetailRouteAdvice.setText("Đang lấy lời khuyên từ AI...");
        if (tvDetailRouteWarnings != null) tvDetailRouteWarnings.setVisibility(View.GONE);
        if (layoutWeatherSummary != null) layoutWeatherSummary.setVisibility(View.GONE);

        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getRouteAdvice(routeId, tripId).enqueue(new Callback<RouteSuggestionResponse>() {
            @Override
            public void onResponse(@NonNull Call<RouteSuggestionResponse> call, @NonNull Response<RouteSuggestionResponse> response) {
                if (pbAdviceLoading != null) pbAdviceLoading.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    if (tvDetailRouteAdvice != null) {
                        tvDetailRouteAdvice.setText("Không thể lấy lời khuyên từ AI lúc này.");
                    }
                    return;
                }

                RouteSuggestionResponse res = response.body();

                // Display Weather Summary
                if (layoutWeatherSummary != null && tvDetailRouteWeatherSummary != null && res.getWeatherSummary() != null && !res.getWeatherSummary().trim().isEmpty()) {
                    tvDetailRouteWeatherSummary.setText(String.format("Thời tiết: %s", res.getWeatherSummary()));
                    layoutWeatherSummary.setVisibility(View.VISIBLE);
                }

                // Display AI advice
                if (tvDetailRouteAdvice != null && res.getTravelAdvice() != null && !res.getTravelAdvice().trim().isEmpty()) {
                    tvDetailRouteAdvice.setText(res.getTravelAdvice());
                } else if (tvDetailRouteAdvice != null) {
                    tvDetailRouteAdvice.setText("Không có lời khuyên cho lộ trình này.");
                }

                // Display warnings if any
                if (tvDetailRouteWarnings != null && res.getWarnings() != null && !res.getWarnings().isEmpty()) {
                    StringBuilder sb = new StringBuilder("Cảnh báo:\n");
                    for (int i = 0; i < res.getWarnings().size(); i++) {
                        sb.append("- ").append(res.getWarnings().get(i));
                        if (i < res.getWarnings().size() - 1) sb.append("\n");
                    }
                    tvDetailRouteWarnings.setText(sb.toString());
                    tvDetailRouteWarnings.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RouteSuggestionResponse> call, @NonNull Throwable t) {
                if (pbAdviceLoading != null) pbAdviceLoading.setVisibility(View.GONE);
                if (tvDetailRouteAdvice != null) {
                    tvDetailRouteAdvice.setText("Lỗi mạng: " + t.getMessage());
                }
            }
        });
    }

    private void drawRoutePolylines() {
        if (!mapReady || googleMap == null || selectedRoute == null) return;

        // Clear existing polylines/markers
        for (Polyline poly : mapPolylines) {
            poly.remove();
        }
        mapPolylines.clear();

        for (Marker marker : mapMarkers) {
            marker.remove();
        }
        mapMarkers.clear();

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasPoints = false;

        // Draw unselected routes first in thin grey
        for (RouteOption option : routeOptions) {
            if (option.getRouteIndex() == selectedRoute.getRouteIndex()) continue;
            List<LatLng> points = new ArrayList<>();
            if (option.getPolyline() != null) {
                for (RoutePolylinePointDto p : option.getPolyline()) {
                    points.add(new LatLng(p.getLatitude(), p.getLongitude()));
                }
            }
            if (!points.isEmpty()) {
                Polyline poly = googleMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .color(Color.parseColor("#D1D5DB"))
                        .width(8f));
                mapPolylines.add(poly);
            }
        }

        // Draw selected route on top in thick emerald green
        List<LatLng> selectedPoints = new ArrayList<>();
        if (selectedRoute.getPolyline() != null) {
            for (RoutePolylinePointDto p : selectedRoute.getPolyline()) {
                LatLng latLng = new LatLng(p.getLatitude(), p.getLongitude());
                selectedPoints.add(latLng);
                boundsBuilder.include(latLng);
                hasPoints = true;
            }
        }

        if (!selectedPoints.isEmpty()) {
            Polyline poly = googleMap.addPolyline(new PolylineOptions()
                    .addAll(selectedPoints)
                    .color(ContextCompat.getColor(this, R.color.green_primary))
                    .width(14f));
            mapPolylines.add(poly);

            // Add markers for Start, End and intermediate stops
            LatLng startLatLng = selectedPoints.get(0);
            LatLng endLatLng = selectedPoints.get(selectedPoints.size() - 1);

            Marker startMarker = googleMap.addMarker(new MarkerOptions()
                    .position(startLatLng)
                    .title("Điểm xuất phát")
                    .snippet(startPoint != null ? startPoint : "Bắt đầu")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            if (startMarker != null) mapMarkers.add(startMarker);

            Marker endMarker = googleMap.addMarker(new MarkerOptions()
                    .position(endLatLng)
                    .title("Điểm đến")
                    .snippet(destination != null ? destination : "Kết thúc")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            if (endMarker != null) mapMarkers.add(endMarker);

            // If there are intermediate points, add light yellow markers for them
            if (selectedRoute.getNodes() != null && selectedRoute.getNodes().size() > 2) {
                // Try to space them or match them to nodes, for simplicity we put a marker at nodes
                // that correspond to the positions inside the polyline.
                // We'll skip the first and last node.
                for (int i = 1; i < selectedRoute.getNodes().size() - 1; i++) {
                    String nodeName = selectedRoute.getNodes().get(i);
                    // Find a suitable coordinate, let's interpolate or match. 
                    // As a simple approach, we can pick points from selectedPoints proportional to index.
                    if (selectedPoints.size() > 2) {
                        int idx = (int) (((double) i / (selectedRoute.getNodes().size() - 1)) * (selectedPoints.size() - 1));
                        if (idx > 0 && idx < selectedPoints.size() - 1) {
                            LatLng intermediatePos = selectedPoints.get(idx);
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
        }

        // Fit camera bounds
        if (hasPoints) {
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));
            } catch (Exception ignored) {
                if (!selectedPoints.isEmpty()) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPoints.get(0), 10f));
                }
            }
        }
    }

    private void applySelectedRoute() {
        if (selectedRoute == null) {
            Toast.makeText(this, "Vui lòng chọn một tuyến đường", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Đang áp dụng tuyến đường...");
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.applyRoute(tripId, selectedRoute).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideLoading();
                if (!response.isSuccessful()) {
                    Toast.makeText(SelectRouteActivity.this, "Áp dụng tuyến đường thất bại", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Toast.makeText(SelectRouteActivity.this, "Chọn tuyến đường thành công!", Toast.LENGTH_SHORT).show();
                
                // Save route polyline and details to SharedPreferences so TripDetailActivity can draw it on map!
                if (selectedRoute != null) {
                    Gson gson = new Gson();
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    if (selectedRoute.getPolyline() != null) {
                        String polylineJson = gson.toJson(selectedRoute.getPolyline());
                        editor.putString("route_polyline_" + tripId, polylineJson);
                    }
                    if (selectedRoute.getNodes() != null) {
                        String nodesJson = gson.toJson(selectedRoute.getNodes());
                        editor.putString("route_nodes_" + tripId, nodesJson);
                        if (selectedRoute.getNodes().size() > 2) {
                            String stopNode = selectedRoute.getNodes().get(1);
                            editor.putString("trip_segment_location_" + tripId, stopNode);
                            editor.putString("trip_segment_district_" + tripId, "Chặng dừng");
                        } else {
                            editor.putString("trip_segment_location_" + tripId, destination != null ? destination : "");
                            editor.putString("trip_segment_district_" + tripId, "Không có chặng dừng");
                        }
                    }
                    editor.putString("trip_start_date_iso_" + tripId, startDateIso);
                    editor.putString("trip_end_date_iso_" + tripId, endDateIso);
                    editor.apply();
                }

                // Save trip locally
                saveTripToLocal();

                // Navigate straight to TripDetailActivity (segments review mode)
                String city = (destination != null && !destination.trim().isEmpty()) ? destination.trim() : (tripTitle != null ? tripTitle : "");
                String dates = buildDateRangeDisplay(startDateDisplay, endDateDisplay, roundTrip);

                Intent intent = new Intent(SelectRouteActivity.this, TripDetailActivity.class);
                intent.putExtra("planner_trip_id", tripId);
                intent.putExtra(TripDetailActivity.EXTRA_CITY, city);
                intent.putExtra(TripDetailActivity.EXTRA_TRIP_TITLE, tripTitle);
                intent.putExtra(TripDetailActivity.EXTRA_DATES, dates);
                intent.putExtra(TripDetailActivity.EXTRA_START_POINT, startPoint);
                intent.putExtra(TripDetailActivity.EXTRA_DESTINATION, destination);
                intent.putExtra(TripDetailActivity.EXTRA_ROUTE, buildRouteLabel(startPoint, destination));
                intent.putExtra("auto_open_edit_segment", true);
                
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideLoading();
                Toast.makeText(SelectRouteActivity.this, "Lỗi khi áp dụng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateItineraryWithAi() {
        tvLoadingText.setText("Đang tạo lịch trình với AI...");
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.generatePlanner(tripId).enqueue(new Callback<PlannerGenerateResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlannerGenerateResponse> call, @NonNull Response<PlannerGenerateResponse> response) {
                hideLoading();
                if (!response.isSuccessful()) {
                    Toast.makeText(SelectRouteActivity.this, "AI lập lịch trình thất bại", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(SelectRouteActivity.this, "Lập lịch trình AI thành công!", Toast.LENGTH_SHORT).show();
                
                // Persist the trip locally so it appears in the trip lists!
                saveTripToLocal();

                // Navigate to TripDetailActivity
                String city = (destination != null && !destination.trim().isEmpty()) ? destination.trim() : (tripTitle != null ? tripTitle : "");
                String dates = buildDateRangeDisplay(startDateDisplay, endDateDisplay, roundTrip);

                Intent intent = new Intent(SelectRouteActivity.this, TripDetailActivity.class);
                intent.putExtra("planner_trip_id", tripId);
                intent.putExtra(TripDetailActivity.EXTRA_CITY, city);
                intent.putExtra(TripDetailActivity.EXTRA_TRIP_TITLE, tripTitle);
                intent.putExtra(TripDetailActivity.EXTRA_DATES, dates);
                intent.putExtra(TripDetailActivity.EXTRA_START_POINT, startPoint);
                intent.putExtra(TripDetailActivity.EXTRA_DESTINATION, destination);
                intent.putExtra(TripDetailActivity.EXTRA_ROUTE, buildRouteLabel(startPoint, destination));
                
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<PlannerGenerateResponse> call, @NonNull Throwable t) {
                hideLoading();
                Toast.makeText(SelectRouteActivity.this, "Lỗi lập lịch trình AI: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTripToLocal() {
        String city = (destination != null && !destination.trim().isEmpty()) ? destination.trim() : (tripTitle != null ? tripTitle : "");
        String dates = buildDateRangeDisplay(startDateDisplay, endDateDisplay, roundTrip);
        TripStatus status = computeStatusFromDate(startDateIso, endDateIso, TripStatus.UPCOMING);

        Trip trip = new Trip(
                tripId,
                tripTitle != null ? tripTitle : city,
                startPoint != null ? startPoint : "",
                destination != null ? destination : city,
                dates,
                startDateIso,
                endDateIso,
                null,
                status,
                R.drawable.sampleplace
        );

        List<Trip> allTrips = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(getManagedTripsKey(), null);
        if (json != null && !json.trim().isEmpty()) {
            try {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    String id = o.optString("id", "");
                    String title = o.optString("title", "");
                    String c = o.optString("city", "");
                    String startP = o.optString("startPoint", "");
                    String dest = o.optString("destination", c);
                    String range = o.optString("range", "");
                    String sIso = o.optString("startDateIso", "");
                    String eIso = o.optString("endDateIso", "");
                    String cost = o.optString("cost", null);
                    String statusRaw = o.optString("status", TripStatus.UPCOMING.name());
                    TripStatus s;
                    try {
                        s = TripStatus.valueOf(statusRaw);
                    } catch (Exception ignored) {
                        s = TripStatus.UPCOMING;
                    }
                    allTrips.add(new Trip(id, title, startP, dest, range, sIso, eIso, cost, s, R.drawable.sampleplace));
                }
            } catch (Exception ignored) { }
        }

        // Upsert
        int existingIndex = -1;
        for (int i = 0; i < allTrips.size(); i++) {
            if (trip.getId().equals(allTrips.get(i).getId())) {
                existingIndex = i;
                break;
            }
        }
        if (existingIndex >= 0) {
            allTrips.set(existingIndex, trip);
        } else {
            allTrips.add(0, trip);
        }

        // Persist
        JSONArray arr = new JSONArray();
        for (Trip t : allTrips) {
            try {
                JSONObject o = new JSONObject();
                o.put("id", t.getId());
                o.put("title", t.getTripTitle());
                o.put("city", t.getCity());
                o.put("startPoint", t.getStartPoint());
                o.put("destination", t.getDestination());
                o.put("range", t.getDateRange());
                o.put("startDateIso", t.getStartDateIso());
                o.put("endDateIso", t.getEndDateIso());
                o.put("cost", t.getCostDisplay());
                o.put("status", t.getStatus().name());
                arr.put(o);
            } catch (Exception ignored) { }
        }
        prefs.edit().putString(getManagedTripsKey(), arr.toString()).apply();
    }

    private String getManagedTripsKey() {
        String userId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString("current_user_id", "");
        if (userId == null || userId.trim().isEmpty()) {
            return KEY_MANAGED_TRIPS_PREFIX + "anonymous";
        }
        return KEY_MANAGED_TRIPS_PREFIX + userId.trim();
    }

    private TripStatus computeStatusFromDate(@Nullable String startIso, @Nullable String endIso, @Nullable TripStatus fallback) {
        Date startDate = parseIsoDate(startIso);
        Date endDate = parseIsoDate(endIso);
        if (startDate == null || endDate == null) {
            return fallback != null ? fallback : TripStatus.UPCOMING;
        }

        Calendar now = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        if (now.before(start)) {
            return TripStatus.UPCOMING;
        }
        if (now.after(end)) {
            return TripStatus.COMPLETED;
        }
        return TripStatus.ONGOING;
    }

    @Nullable
    private Date parseIsoDate(@Nullable String iso) {
        if (iso == null || iso.trim().isEmpty()) return null;
        String text = iso.trim();
        String[] patterns = new String[]{
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setLenient(false);
                if (pattern.contains("'Z'")) {
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                return sdf.parse(text);
            } catch (Exception ignored) { }
        }
        return null;
    }

    private String buildDateRangeDisplay(@Nullable String startDisplay, @Nullable String endDisplay, boolean roundTrip) {
        String start = startDisplay != null ? startDisplay.trim() : "";
        String end = endDisplay != null ? endDisplay.trim() : "";
        if (start.isEmpty()) return "—";
        if (end.isEmpty()) return start + " · 1 chiều";
        return start + " - " + end;
    }

    private String buildRouteLabel(@Nullable String startPoint, @Nullable String destination) {
        String start = startPoint != null && !startPoint.trim().isEmpty() ? startPoint.trim() : "Hồ Chí Minh";
        String end = destination != null && !destination.trim().isEmpty() ? destination.trim() : "Đà Nẵng";
        return start + " → " + end;
    }

    private void showLoading(String text) {
        if (layoutLoading != null) {
            tvLoadingText.setText(text);
            layoutLoading.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.GONE);
        }
    }

    private int sp(float size) {
        return (int) (size * getResources().getDisplayMetrics().scaledDensity);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        mapReady = true;
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        drawRoutePolylines();
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
