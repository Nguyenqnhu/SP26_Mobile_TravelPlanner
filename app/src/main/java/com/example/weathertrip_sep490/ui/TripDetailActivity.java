package com.example.weathertrip_sep490.ui;

import com.example.weathertrip_sep490.util.AppToast;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import com.example.weathertrip_sep490.model.LocationOption;
import com.example.weathertrip_sep490.model.DistrictOption;
import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.ItineraryListAdapter;
import com.example.weathertrip_sep490.adapter.TripDateRibbonAdapter;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.ItineraryRow;
import com.example.weathertrip_sep490.model.ItinerarySegmentRow;
import com.example.weathertrip_sep490.model.ItineraryStopRow;
import com.example.weathertrip_sep490.model.PlannerDayResponse;
import com.example.weathertrip_sep490.model.PlannerGenerateResponse;
import com.example.weathertrip_sep490.model.PlannerItemResponse;
import com.example.weathertrip_sep490.model.PlannerTripResponse;
import com.example.weathertrip_sep490.model.TripSegmentResponse;
import com.example.weathertrip_sep490.model.POI;
import com.example.weathertrip_sep490.model.TripRibbonDay;
import com.example.weathertrip_sep490.ui.decoration.ItineraryTimelineLineDecoration;
import com.example.weathertrip_sep490.util.PlannerSegmentValidation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripDetailActivity extends AppCompatActivity implements OnMapReadyCallback, TripInviteDialogFragment.Listener {

    public static final String EXTRA_CITY = "extra_city";
    public static final String EXTRA_TRIP_TITLE = "extra_trip_title";
    public static final String EXTRA_DATES = "extra_dates";
    public static final String EXTRA_START_POINT = "extra_start_point";
    public static final String EXTRA_DESTINATION = "extra_destination";
    public static final String EXTRA_ROUTE = "extra_route";
    public static final String EXTRA_GENERATED_SEGMENT_LOCATION = "extra_generated_segment_location";
    public static final String EXTRA_GENERATED_SEGMENT_START = "extra_generated_segment_start";
    public static final String EXTRA_GENERATED_SEGMENT_END = "extra_generated_segment_end";
    public static final String EXTRA_GENERATED_SEGMENT_LAT = "extra_generated_segment_lat";
    public static final String EXTRA_GENERATED_SEGMENT_LNG = "extra_generated_segment_lng";

    private static final String MAPVIEW_BUNDLE_KEY = "TripDetailMapViewBundle";
    private static final int REQUEST_CODE_EDIT_SEGMENT = 1002;

    private MapView mapView;
    private GoogleMap googleMap;
    private boolean mapReady;

    private final List<Marker> mapMarkers = new ArrayList<>();
    private final List<com.google.android.gms.maps.model.Polyline> mapPolylines = new ArrayList<>();
    private final List<ItineraryRow> currentRows = new ArrayList<>();
    private final Map<String, POI> poiCacheMap = new java.util.HashMap<>();

    private TripDateRibbonAdapter dayRibbonAdapter;
    private ItineraryListAdapter itineraryAdapter;
    private RecyclerView rvDays;
    private RecyclerView rvTimeline;
    private androidx.recyclerview.widget.SnapHelper snapHelper;

    private TextView tvRibbonMonthTitle;
    private TextView tvMapPlaceCount;
    private String tripTitleLabel;
    private String startPointLabel;
    private String destinationLabel;

    /** true: dùng planner thật từ BE */
    private boolean usePlannerApiMode;
    private String plannerTripId;
    private PlannerTripResponse plannerResponse;
    private String generatedLocationName;
    private double generatedLat;
    private double generatedLng;
    private String pendingDates;
    private String pendingSegStart;
    private String pendingSegEnd;
    private TextView tvStatDays;
    private TextView tvStatPlaces;
    private TextView btnGenerateAi;
    private TextView btnAddSegment;
    private TextView btnInvite;

    private View cardDayRibbon;
    private View btnGenerateFinalItinerary;

    private View cardReviewFields;
    private View layoutTimelineContainer;
    private TextView tvReviewTripTitle;
    private TextView tvReviewStartLocation;
    private TextView tvReviewStartDistrict;
    private TextView tvReviewSegmentLocation;
    private TextView tvReviewSegmentDistrict;
    private TextView tvReviewSegmentDates;

    private View btnEditTripTitle;
    private View btnEditStartLocation;
    private View btnEditStartDistrict;
    private View btnEditSegmentLocation;
    private View btnEditSegmentDistrict;
    private View btnEditSegmentDates;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        String city = getIntent().getStringExtra(EXTRA_CITY);
        String tripTitle = getIntent().getStringExtra(EXTRA_TRIP_TITLE);
        String dates = getIntent().getStringExtra(EXTRA_DATES);
        String startPoint = getIntent().getStringExtra(EXTRA_START_POINT);
        String destination = getIntent().getStringExtra(EXTRA_DESTINATION);
        String route = getIntent().getStringExtra(EXTRA_ROUTE);
        startPointLabel = startPoint;
        destinationLabel = destination;
        tripTitleLabel = normalizeTripTitle(tripTitle, city, route, startPoint, destination);
        String segLocation = getIntent().getStringExtra(EXTRA_GENERATED_SEGMENT_LOCATION);
        String segStart = getIntent().getStringExtra(EXTRA_GENERATED_SEGMENT_START);
        String segEnd = getIntent().getStringExtra(EXTRA_GENERATED_SEGMENT_END);
        double segLat = getIntent().getDoubleExtra(EXTRA_GENERATED_SEGMENT_LAT, 10.8231);
        double segLng = getIntent().getDoubleExtra(EXTRA_GENERATED_SEGMENT_LNG, 106.6297);
        plannerTripId = getIntent().getStringExtra("planner_trip_id");
        boolean hasPlannerId = plannerTripId != null && !plannerTripId.trim().isEmpty();
        boolean hasGeneratedData = segLocation != null && !segLocation.trim().isEmpty();
        pendingDates = dates;
        pendingSegStart = segStart;
        pendingSegEnd = segEnd;

        ImageView btnBack = findViewById(R.id.btnBackTripDetail);
        TextView tvJourneyTitle = findViewById(R.id.tvTripJourneyTitle);
        TextView tvDates = findViewById(R.id.tvTripDetailDates);
        TextView tvRoute = findViewById(R.id.tvTripRouteBreadcrumb);
        tvStatDays = findViewById(R.id.tvTripStatDays);
        tvStatPlaces = findViewById(R.id.tvTripStatPlaces);
        tvRibbonMonthTitle = findViewById(R.id.tvRibbonMonthTitle);
        tvMapPlaceCount = findViewById(R.id.tvMapPlaceCount);
        btnAddSegment = findViewById(R.id.btnAddSegment);
        btnGenerateAi = findViewById(R.id.btnTripInvite);
        if (btnAddSegment != null) btnAddSegment.setOnClickListener(v -> openAddSegment());
        if (btnGenerateAi != null) btnGenerateAi.setText("Mời bạn tham gia");

        tvJourneyTitle.setText(tripTitleLabel);
        tvDates.setText(dates != null && !dates.isEmpty() ? dates : "5/4/2026 – 12/4/2026");
        tvRoute.setText(resolveRouteLabel(route, startPoint, destination, city));
        btnBack.setOnClickListener(v -> finish());
        if (btnGenerateAi != null) {
            btnGenerateAi.setOnClickListener(v -> showInviteDialog());
        }

        initMapView(savedInstanceState);

        rvTimeline = findViewById(R.id.rvTripTimeline);
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        itineraryAdapter = new ItineraryListAdapter();
        rvTimeline.setAdapter(itineraryAdapter);
        rvTimeline.addItemDecoration(new ItineraryTimelineLineDecoration(itineraryAdapter, getResources()));

        cardDayRibbon = findViewById(R.id.cardDayRibbon);
        btnGenerateFinalItinerary = findViewById(R.id.btnGenerateFinalItinerary);
        if (btnGenerateFinalItinerary != null) {
            btnGenerateFinalItinerary.setOnClickListener(v -> generatePlannerAndReload());
        }

        loadPoiCache();
        initReviewCardViews();

        if (hasPlannerId) {
            usePlannerApiMode = true;
            generatedLocationName = segLocation != null ? segLocation.trim() : "";
            generatedLat = segLat;
            generatedLng = segLng;
            loadPlannerData(plannerTripId);
        } else if (hasGeneratedData) {
            generatedLocationName = segLocation.trim();
            generatedLat = segLat;
            generatedLng = segLng;
            List<TripRibbonDay> days = buildRibbonDaysForTrip(dates, segStart, segEnd);
            if (days.isEmpty()) {
                Calendar today = Calendar.getInstance();
                days.add(new TripRibbonDay(normalizeDayStart(today), weekdayVnShort(today)));
            }
            tvStatDays.setText(days.size() + " ngày");
            setupDayRibbon(days, false);
            seedGeneratedItinerary(0, days.size());
            updateMonthTitle(0);
        } else {
            List<TripRibbonDay> days = buildRibbonDaysForTrip(dates, null, null);
            if (days.isEmpty()) {
                days = buildRibbonDaysMock();
            }
            tvStatDays.setText(days.size() + " ngày");
            tvStatPlaces.setText("0 địa điểm");
            setupDayRibbon(days, false);
            seedDefaultRouteSegment();
            updateMonthTitle(0);
        }
    }

    private void seedGeneratedItinerary(int dayIndex, int totalDays) {
        if (dayRibbonAdapter == null || generatedLocationName == null) return;
        currentRows.clear();
        String locationName = generatedLocationName;
        double latitude = generatedLat;
        double longitude = generatedLng;

        currentRows.add(new ItinerarySegmentRow(
                String.format(Locale.getDefault(), "Chặng %d", Math.min(dayIndex + 1, totalDays)),
                locationName,
                "--"
        ));

        currentRows.add(new ItineraryStopRow(
                "08:00 – 17:00", "08:00", "17:00",
                "Khám phá " + locationName,
                locationName,
                "Theo lịch trình AI",
                "Tùy hoạt động",
                "Tự động tối ưu theo chặng user đã chọn",
                "--",
                R.drawable.bg_image_placeholder,
                latitude, longitude, 1
        ));

        itineraryAdapter.updateData(new ArrayList<>(currentRows));
        rvTimeline.invalidateItemDecorations();
        tvMapPlaceCount.setText(countStops(currentRows) + " hoạt động");
        if (tvStatPlaces != null) {
            tvStatPlaces.setText(countStops(currentRows) + " hoạt động");
        }
        applyMapMarkers();
    }

    private void seedDefaultRouteSegment() {
        currentRows.clear();
        String from = startPointLabel != null && !startPointLabel.trim().isEmpty() ? startPointLabel.trim() : "Điểm đi";
        String to = destinationLabel != null && !destinationLabel.trim().isEmpty() ? destinationLabel.trim() : "Điểm đến";

        currentRows.add(new ItinerarySegmentRow("Chặng gốc", from, "--"));
        currentRows.add(new ItineraryStopRow(
                "--:-- – --:--",
                "--:--",
                "--:--",
                "Chặng khởi tạo",
                from,
                "Sẵn từ lúc tạo trip",
                "--",
                to,
                "--",
                R.drawable.bg_image_placeholder,
                10.8231,
                106.6297,
                1
        ));

        itineraryAdapter.updateData(new ArrayList<>(currentRows));
        rvTimeline.invalidateItemDecorations();
        tvMapPlaceCount.setText(countStops(currentRows) + " địa điểm");
        if (tvStatPlaces != null) {
            tvStatPlaces.setText(countStops(currentRows) + " địa điểm");
        }
        applyMapMarkers();
    }

    private boolean isAiGenerated(PlannerTripResponse response) {
        if (response == null || response.getSegments() == null) return false;
        for (com.example.weathertrip_sep490.model.TripSegmentResponse segment : response.getSegments()) {
            if (segment.getDays() != null && !segment.getDays().isEmpty()) {
                for (com.example.weathertrip_sep490.model.PlannerDayResponse day : segment.getDays()) {
                    if (day.getItems() != null && !day.getItems().isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void loadPoiCache() {
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getRecommendedPOIsWithLimit("vi", 1000).enqueue(new Callback<List<POI>>() {
            @Override
            public void onResponse(@NonNull Call<List<POI>> call, @NonNull Response<List<POI>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (POI p : response.body()) {
                        if (p.getName() != null) {
                            poiCacheMap.put(p.getName().trim().toLowerCase(), p);
                        }
                    }
                    if (usePlannerApiMode && dayRibbonAdapter != null) {
                        seedFromPlanner(dayRibbonAdapter.getSelectedPosition());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<POI>> call, @NonNull Throwable t) {
                // Silently ignore
            }
        });
    }

    private void loadPlannerData(@NonNull String tripId) {
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getPlanner(tripId).enqueue(new Callback<PlannerTripResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlannerTripResponse> call, @NonNull Response<PlannerTripResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    AppToast.show(TripDetailActivity.this, "Không tải được planner");
                    return;
                }
                plannerResponse = response.body();
                if (plannerResponse != null && plannerResponse.getSegments() != null) {
                    java.util.Collections.sort(plannerResponse.getSegments(), (s1, s2) -> Integer.compare(s1.getOrderIndex(), s2.getOrderIndex()));
                }

                boolean isAiGen = isAiGenerated(plannerResponse);

                if (cardReviewFields != null) {
                    cardReviewFields.setVisibility(isAiGen ? View.GONE : View.VISIBLE);
                }
                if (layoutTimelineContainer != null) {
                    layoutTimelineContainer.setVisibility(isAiGen ? View.VISIBLE : View.GONE);
                }
                populateReviewCard();

                // Pre-populate segment-specific keys in SharedPreferences if they are not already set
                if (plannerResponse != null && plannerResponse.getSegments() != null) {
                    List<com.example.weathertrip_sep490.model.TripSegmentResponse> segList = plannerResponse.getSegments();
                    android.content.SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
                    String nodesJson = prefs.getString("route_nodes_" + plannerTripId, null);
                    List<String> routeNodes = null;
                    if (nodesJson != null && !nodesJson.trim().isEmpty()) {
                        try {
                            java.lang.reflect.Type stringListType = new com.google.gson.reflect.TypeToken<List<String>>(){}.getType();
                            routeNodes = new com.google.gson.Gson().fromJson(nodesJson, stringListType);
                        } catch (Exception ignored) {}
                    }
                    android.content.SharedPreferences.Editor editor = prefs.edit();
                    for (int i = 0; i < segList.size(); i++) {
                        com.example.weathertrip_sep490.model.TripSegmentResponse seg = segList.get(i);
                        if (seg == null) continue;
                        String segId = seg.getSegmentId();
                        String locKey = "trip_segment_location_" + plannerTripId + "_" + segId;
                        if (!prefs.contains(locKey)) {
                            String nodeName = null;
                            if (routeNodes != null && i < routeNodes.size()) {
                                nodeName = routeNodes.get(i);
                            }
                            if (nodeName == null || nodeName.trim().isEmpty()) {
                                nodeName = (destinationLabel != null && !destinationLabel.trim().isEmpty()) ? destinationLabel : "Chặng dừng";
                            }
                            editor.putString(locKey, nodeName);
                        }
                        String distKey = "trip_segment_district_" + plannerTripId + "_" + segId;
                        if (!prefs.contains(distKey)) {
                            com.example.weathertrip_sep490.model.TripSegmentResponse targetSeg = getStopSegmentToEdit(plannerResponse);
                            if (targetSeg != null && segId.equals(targetSeg.getSegmentId())) {
                                String generalDist = prefs.getString("trip_segment_district_" + plannerTripId, null);
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

                // Auto open removed to prioritize wizard flow and timeline view

                // Update adapter edit button visibility & edit listener
                if (itineraryAdapter != null) {
                    itineraryAdapter.setShowEditButtons(!isAiGen);
                    if (!isAiGen) {
                        itineraryAdapter.setOnSegmentEditListener((segmentId, segmentLabel) -> {
                            android.content.Intent intent = new android.content.Intent(TripDetailActivity.this, EditSegmentActivity.class);
                            intent.putExtra("planner_trip_id", plannerTripId);
                            startActivityForResult(intent, REQUEST_CODE_EDIT_SEGMENT);
                        });
                    } else {
                        itineraryAdapter.setOnSegmentEditListener(null);
                    }
                }

                // Show/hide sticky AI generation button
                if (btnGenerateFinalItinerary != null) {
                    btnGenerateFinalItinerary.setVisibility(isAiGen ? View.GONE : View.VISIBLE);
                }

                // Show/hide day ribbon card
                if (cardDayRibbon != null) {
                    cardDayRibbon.setVisibility(isAiGen ? View.VISIBLE : View.GONE);
                }

                List<TripRibbonDay> days = buildDaysFromPlanner(plannerResponse, pendingDates, pendingSegStart, pendingSegEnd);
                if (days.isEmpty()) {
                    days = buildRibbonDaysForTrip(pendingDates, pendingSegStart, pendingSegEnd);
                }
                if (days.isEmpty()) {
                    Calendar today = Calendar.getInstance();
                    days.add(new TripRibbonDay(normalizeDayStart(today), weekdayVnShort(today)));
                }
                tvStatDays.setText(days.size() + " ngày");
                setupDayRibbon(days, true);
                seedFromPlanner(0);
                updateMonthTitle(0);
            }

            @Override
            public void onFailure(@NonNull Call<PlannerTripResponse> call, @NonNull Throwable t) {
                AppToast.show(TripDetailActivity.this, "Lỗi mạng khi tải planner: " + (t.getMessage() != null ? t.getMessage() : "unknown"));
            }
        });
    }

    private List<TripRibbonDay> buildDaysFromPlanner(@Nullable PlannerTripResponse response, @Nullable String datesDisplay, @Nullable String segStart, @Nullable String segEnd) {
        List<TripRibbonDay> fixedRange = buildRibbonDaysForTrip(datesDisplay, segStart, segEnd);
        if (!fixedRange.isEmpty()) {
            return fixedRange;
        }
        List<TripRibbonDay> out = new ArrayList<>();
        if (response == null || response.getSegments() == null) return out;
        Calendar minDay = null;
        Calendar maxDay = null;
        for (TripSegmentResponse segment : response.getSegments()) {
            if (segment == null) continue;
            if (segment.getDays() != null && !segment.getDays().isEmpty()) {
                for (PlannerDayResponse day : segment.getDays()) {
                    if (day == null || day.getDate() == null) continue;
                    Calendar c = calendarFromDate(day.getDate());
                    if (c != null) {
                        if (minDay == null || c.before(minDay)) minDay = (Calendar) c.clone();
                        if (maxDay == null || c.after(maxDay)) maxDay = (Calendar) c.clone();
                    }
                }
            }
            if (segment.getStartDate() != null) {
                Calendar c = calendarFromDate(segment.getStartDate());
                if (c != null) {
                    if (minDay == null || c.before(minDay)) minDay = (Calendar) c.clone();
                    if (maxDay == null || c.after(maxDay)) maxDay = (Calendar) c.clone();
                }
            }
            if (segment.getEndDate() != null) {
                Calendar c = calendarFromDate(segment.getEndDate());
                if (c != null) {
                    if (minDay == null || c.before(minDay)) minDay = (Calendar) c.clone();
                    if (maxDay == null || c.after(maxDay)) maxDay = (Calendar) c.clone();
                }
            }
        }
        if (minDay != null) {
            return buildRibbonDaysFromRange(minDay, maxDay != null ? maxDay : minDay);
        }
        return out;
    }

    @Nullable
    private Calendar calendarFromDate(@Nullable Date date) {
        if (date == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return normalizeDayStart(c);
    }

    private void seedFromPlanner(int dayIndex) {
        currentRows.clear();
        if (plannerResponse == null || plannerResponse.getSegments() == null || plannerResponse.getSegments().isEmpty()) {
            seedDefaultRouteSegment();
            return;
        }
        Calendar selectedDay = null;
        if (dayRibbonAdapter != null && dayIndex >= 0 && dayIndex < dayRibbonAdapter.getItemCount()) {
            selectedDay = dayRibbonAdapter.getDayAt(dayIndex).getCalendar();
        }
        int globalStopOrder = 1;
        android.content.SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
        for (TripSegmentResponse segment : plannerResponse.getSegments()) {
            if (segment == null) continue;
            String segmentLabel = "Chặng " + segment.getOrderIndex();
            String segmentId = segment.getSegmentId();
            String localSegLoc = prefs.getString("trip_segment_location_" + plannerTripId + "_" + segmentId, null);
            String cityName = localSegLoc != null && !localSegLoc.trim().isEmpty() ? localSegLoc : (generatedLocationName != null && !generatedLocationName.isEmpty() ? generatedLocationName : "Chặng đã chọn");
            currentRows.add(new ItinerarySegmentRow(segmentLabel, cityName, segment.getDistanceKm() != null ? String.format(Locale.getDefault(), "%.1f km", segment.getDistanceKm()) : "--", segment.getSegmentId()));
            List<PlannerDayResponse> days = segment.getDays();
            if (days != null && !days.isEmpty()) {
                for (PlannerDayResponse day : days) {
                    if (day == null) continue;
                    if (selectedDay != null && day.getDate() != null && !isSameDay(day.getDate(), selectedDay)) {
                        continue;
                    }
                    List<PlannerItemResponse> items = day.getItems();
                    if (items == null || items.isEmpty()) continue;
                    for (PlannerItemResponse item : items) {
                        if (item == null) continue;
                        String startTime = normalizeApiTime(item.getStartTime());
                        String endTime = normalizeApiTime(item.getEndTime());
                        String title = safeText(item.getPoiName());
                        String locationLine = joinLocation(item.getLocationName(), item.getAddress());
                        String indoorText = item.isIndoor() ? "Trong nhà" : "Ngoài trời";
                        String weather = "";
                        if (item.getWeather() != null) {
                            int rainPercent = (int) (item.getWeather().getPrecipitationProbability() * 100);
                            int temp = (int) item.getWeather().getTemperatureCelsius();
                            weather = "Mưa: " + rainPercent + "% | " + temp + "°C";
                        }
                        double lat = generatedLat;
                        double lng = generatedLng;
                        if (title != null) {
                            String key = title.trim().toLowerCase();
                            if (poiCacheMap.containsKey(key)) {
                                POI cachedPoi = poiCacheMap.get(key);
                                if (cachedPoi != null) {
                                    lat = cachedPoi.getLatitude();
                                    lng = cachedPoi.getLongitude();
                                }
                            }
                        }
                        currentRows.add(new ItineraryStopRow(
                                formatTimeRange(startTime, endTime),
                                safeTime(startTime),
                                safeTime(endTime),
                                title,
                                locationLine,
                                indoorText,
                                safeText(item.getType()),
                                "Theo lịch AI",
                                weather,
                                R.drawable.bg_image_placeholder,
                                lat,
                                lng,
                                globalStopOrder++,
                                item.getAiReason(),
                                item.getPoiImg()
                        ));
                    }
                }
            } else if (segment.getStartDate() != null || segment.getEndDate() != null) {
                String startTxt = segment.getStartDate() != null ? new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(segment.getStartDate()) : "--";
                String endTxt = segment.getEndDate() != null ? new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(segment.getEndDate()) : "--";
                currentRows.add(new ItineraryStopRow(
                        startTxt + " – " + endTxt,
                        startTxt,
                        endTxt,
                        "Segment " + segment.getOrderIndex(),
                        cityName,
                        "Khoảng ngày",
                        "--",
                        "--",
                        "--",
                        R.drawable.bg_image_placeholder,
                        generatedLat,
                        generatedLng,
                        globalStopOrder++
                ));
            }
        }
        if (countStops(currentRows) == 0) {
            seedDefaultRouteSegment();
            return;
        }
        itineraryAdapter.updateData(new ArrayList<>(currentRows));
        rvTimeline.invalidateItemDecorations();
        tvMapPlaceCount.setText(countStops(currentRows) + " địa điểm");
        if (tvStatPlaces != null) tvStatPlaces.setText(countStops(currentRows) + " địa điểm");
        applyMapMarkers();
    }

    private void generatePlannerAndReload() {
        if (plannerTripId == null || plannerTripId.trim().isEmpty()) {
            AppToast.show(this, "Thiếu tripId planner");
            return;
        }
        if (plannerResponse != null
                && plannerResponse.getSegments() != null
                && !PlannerSegmentValidation.segmentsLookValidForAi(plannerResponse.getSegments())) {
            AppToast.show(this, "Dữ liệu chặng chưa hợp lệ (thiếu ngày hoặc ngày kết thúc trước ngày bắt đầu). Hãy chỉnh chặng rồi thử lại.");
            return;
        }
        AppToast.show(this, "Đang generate AI...");
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.generatePlanner(plannerTripId).enqueue(new Callback<com.example.weathertrip_sep490.model.PlannerGenerateResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.example.weathertrip_sep490.model.PlannerGenerateResponse> call, @NonNull Response<com.example.weathertrip_sep490.model.PlannerGenerateResponse> response) {
                if (!response.isSuccessful()) {
                    String details = "";
                    try {
                        okhttp3.ResponseBody eb = response.errorBody();
                        if (eb != null) details = eb.string();
                    } catch (Exception ignored) {}
                    AppToast.show(TripDetailActivity.this, buildGenerateErrorMessage(response.code(), details));
                    return;
                }
                AppToast.show(TripDetailActivity.this, "AI đã tạo xong lịch trình");
                reloadPlanner();
            }

            @Override
            public void onFailure(@NonNull Call<com.example.weathertrip_sep490.model.PlannerGenerateResponse> call, @NonNull Throwable t) {
                AppToast.show(TripDetailActivity.this, "Lỗi mạng khi generate AI: " + (t.getMessage() != null ? t.getMessage() : "unknown"));
            }
        });
    }

    private void reloadPlanner() {
        if (plannerTripId == null || plannerTripId.trim().isEmpty()) return;
        loadPlannerData(plannerTripId);
    }

    private void openAddSegment() {
        if (plannerTripId == null || plannerTripId.trim().isEmpty()) {
            AppToast.show(this, "Thiếu tripId");
            return;
        }

        String startPoint = startPointLabel;
        String destination = destinationLabel;
        String title = tripTitleLabel;
        String startDateIso = null;
        String endDateIso = null;
        String startDateDisplay = null;
        String endDateDisplay = null;
        boolean roundTrip = false;

        android.content.SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
        String currentUserId = prefs.getString("current_user_id", "");
        String tripsKey = "managed_trips_json_" + (currentUserId.trim().isEmpty() ? "anonymous" : currentUserId.trim());
        String json = prefs.getString(tripsKey, null);
        if (json != null && !json.trim().isEmpty()) {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(json);
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject o = arr.getJSONObject(i);
                    if (plannerTripId.equals(o.optString("id", ""))) {
                        startDateIso = o.optString("startDateIso", "");
                        endDateIso = o.optString("endDateIso", "");
                        String range = o.optString("range", "");
                        if (!range.isEmpty()) {
                            if (range.contains("-")) {
                                String[] parts = range.split("-");
                                if (parts.length >= 2) {
                                    startDateDisplay = parts[0].trim();
                                    endDateDisplay = parts[1].trim();
                                    roundTrip = true;
                                }
                            } else if (range.contains("·")) {
                                String[] parts = range.split("·");
                                startDateDisplay = parts[0].trim();
                                roundTrip = false;
                            } else {
                                startDateDisplay = range;
                            }
                        }
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        Intent intent = new Intent(this, SelectRouteActivity.class);
        intent.putExtra("trip_id", plannerTripId);
        intent.putExtra("trip_title", title);
        intent.putExtra("start_point", startPoint);
        intent.putExtra("destination", destination);
        intent.putExtra("start_date_iso", startDateIso);
        intent.putExtra("end_date_iso", endDateIso);
        intent.putExtra("start_date_display", startDateDisplay);
        intent.putExtra("end_date_display", endDateDisplay);
        intent.putExtra("round_trip", roundTrip);
        startActivity(intent);
        finish();
    }

    private void showInviteDialog() {
        if (plannerTripId == null || plannerTripId.trim().isEmpty()) {
            AppToast.show(this, "Thiếu tripId để tạo link mời");
            return;
        }
        String title = tripTitleLabel != null && !tripTitleLabel.trim().isEmpty()
                ? tripTitleLabel.trim()
                : (destinationLabel != null ? destinationLabel : "Chuyến đi");
        TripInviteDialogFragment
                .newInstance(plannerTripId, title)
                .show(getSupportFragmentManager(), "TripInviteDialogFragment");
    }



    @Override
    public void onInviteJoined(@NonNull String tripId) {
        AppToast.show(this, "Đã tham gia chuyến đi");
        reloadPlanner();
    }

    @Override
    public void onInviteAuthRequired(@NonNull String tripId) {
        AppToast.show(this, "Vui lòng đăng nhập để tham gia");
        startActivity(new Intent(this, LoginActivity.class));
    }

    private static String safeText(@Nullable String s) { return s == null || s.trim().isEmpty() ? "--" : s.trim(); }
    private static String buildGenerateErrorMessage(int code, @Nullable String rawError) {
        if (rawError == null || rawError.trim().isEmpty()) {
            return "Generate thất bại (" + code + ")";
        }
        try {
            JSONObject obj = new JSONObject(rawError);
            String message = obj.optString("message", "").trim();
            String detail = obj.optString("detail", "").trim();
            if (!detail.isEmpty()) {
                return "Generate thất bại (" + code + "): " + detail;
            }
            if (!message.isEmpty()) {
                return "Generate thất bại (" + code + "): " + message;
            }
        } catch (Exception ignored) {
        }
        return "Generate thất bại (" + code + "): " + rawError;
    }
    private static String safeTime(@Nullable String s) { return s == null || s.trim().isEmpty() ? "--:--" : s.trim(); }
    private static String formatRisk(double risk) { return String.format(Locale.getDefault(), "%.1f", risk); }
    private static String joinLocation(@Nullable String locationName, @Nullable String address) {
        String ln = locationName == null ? "" : locationName.trim();
        String ad = address == null ? "" : address.trim();
        if (ln.isEmpty()) return ad.isEmpty() ? "--" : ad;
        if (ad.isEmpty()) return ln;
        return ln + " · " + ad;
    }
    private static String formatTimeRange(@Nullable String start, @Nullable String end) {
        return safeTime(start) + " – " + safeTime(end);
    }

    private static String normalizeApiTime(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) return "--:--";
        String v = value.trim();
        if (v.length() >= 5 && v.charAt(2) == ':') return v.substring(0, 5);
        return v;
    }

    private static boolean isSameDay(@NonNull Date date, @NonNull Calendar selectedDay) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.YEAR) == selectedDay.get(Calendar.YEAR)
                && c.get(Calendar.DAY_OF_YEAR) == selectedDay.get(Calendar.DAY_OF_YEAR);
    }
    private List<TripRibbonDay> buildRibbonDaysForTrip(
            @Nullable String datesDisplay,
            @Nullable String segmentStartIso,
            @Nullable String segmentEndIso
    ) {
        Calendar start = null;
        Calendar end = null;
        if (datesDisplay != null && !datesDisplay.trim().isEmpty()) {
            String d = datesDisplay.trim().replace('–', '-').replace('—', '-');
            if (d.contains("·")) {
                String part = d.split("·")[0].trim();
                start = parseDdMmYyyy(part);
                start = normalizeDayStart(start);
                if (start != null) end = (Calendar) start.clone();
            } else {
                String[] parts = d.split("\\s*-\\s*", 2);
                if (parts.length >= 2) {
                    start = parseDdMmYyyy(parts[0].trim());
                    end = parseDdMmYyyy(parts[1].trim());
                }
            }
        }
        if (start == null && segmentStartIso != null && !segmentStartIso.isEmpty()) {
            start = parseIsoDateOnly(segmentStartIso);
            end = segmentEndIso != null && !segmentEndIso.isEmpty()
                    ? parseIsoDateOnly(segmentEndIso)
                    : (start != null ? (Calendar) start.clone() : null);
        }
        start = normalizeDayStart(start);
        end = normalizeDayStart(end);
        return buildRibbonDaysFromRange(start, end);
    }

    private static Calendar normalizeDayStart(@Nullable Calendar c) {
        if (c == null) return null;
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    @Nullable
    private static Calendar parseDdMmYyyy(@Nullable String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            f.setLenient(false);
            Calendar c = Calendar.getInstance();
            c.setTime(f.parse(s.trim()));
            return normalizeDayStart(c);
        } catch (ParseException e) {
            return null;
        }
    }

    @Nullable
    private static Calendar parseIsoDateOnly(@Nullable String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String raw = s.trim();
        String[] patterns = new String[]{
                "yyyy-MM-dd",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(pattern, Locale.US);
                f.setLenient(false);
                Calendar c = Calendar.getInstance();
                c.setTime(f.parse(raw));
                return normalizeDayStart(c);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @NonNull
    private static List<TripRibbonDay> buildRibbonDaysFromRange(@Nullable Calendar start, @Nullable Calendar end) {
        List<TripRibbonDay> out = new ArrayList<>();
        if (start == null) return out;
        if (end == null) end = (Calendar) start.clone();
        if (end.before(start)) {
            Calendar t = start;
            start = end;
            end = t;
        }
        Calendar cur = (Calendar) start.clone();
        int guard = 0;
        while (!cur.after(end) && guard++ < 400) {
            out.add(new TripRibbonDay((Calendar) cur.clone(), weekdayVnShort(cur)));
            cur.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (out.isEmpty()) {
            out.add(new TripRibbonDay((Calendar) start.clone(), weekdayVnShort(start)));
        }
        return out;
    }

    private void initMapView(@Nullable Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapTripDetail);
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
        applyMapMarkers();
    }

    private void setupDayRibbon(@NonNull List<TripRibbonDay> days, boolean generatedMode) {
        usePlannerApiMode = generatedMode;
        rvDays = findViewById(R.id.rvTripDetailDays);
        if (rvDays != null) {
            rvDays.setVisibility(View.VISIBLE);
        }
        rvDays.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        dayRibbonAdapter = new TripDateRibbonAdapter((pos, day) -> {
            rvDays.smoothScrollToPosition(pos);
            seedForRibbonDay(pos);
            updateMonthTitle(pos);
            applyMapMarkers();
        });
        dayRibbonAdapter.setDays(days);
        rvDays.setAdapter(dayRibbonAdapter);

        if (snapHelper == null) {
            snapHelper = new LinearSnapHelper();
            snapHelper.attachToRecyclerView(rvDays);
            rvDays.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState != RecyclerView.SCROLL_STATE_IDLE) return;
                    View snapped = snapHelper.findSnapView(recyclerView.getLayoutManager());
                    if (snapped == null) return;
                    int pos = recyclerView.getLayoutManager().getPosition(snapped);
                    if (pos == RecyclerView.NO_POSITION) return;
                    if (dayRibbonAdapter.getSelectedPosition() != pos) {
                        dayRibbonAdapter.setSelectedPosition(pos);
                        seedForRibbonDay(pos);
                        updateMonthTitle(pos);
                        applyMapMarkers();
                    }
                }
            });
        }
    }

    private void seedForRibbonDay(int pos) {
        int totalDays = dayRibbonAdapter != null ? dayRibbonAdapter.getItemCount() : 1;
        if (usePlannerApiMode) {
            seedFromPlanner(pos);
        } else {
            seedItinerary(pos, totalDays);
        }
    }

    private void shiftRibbonDay(int delta) {
        int next = dayRibbonAdapter.getSelectedPosition() + delta;
        if (next < 0 || next >= dayRibbonAdapter.getItemCount()) return;
        dayRibbonAdapter.setSelectedPosition(next);
        rvDays.smoothScrollToPosition(next);
        seedForRibbonDay(next);
        updateMonthTitle(next);
        applyMapMarkers();
    }

    private List<TripRibbonDay> buildRibbonDaysMock() {
        List<TripRibbonDay> out = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.set(2026, Calendar.APRIL, 5, 0, 0, 0);
        for (int i = 0; i < 8; i++) {
            Calendar copy = (Calendar) c.clone();
            copy.add(Calendar.DAY_OF_MONTH, i);
            out.add(new TripRibbonDay(copy, weekdayVnShort(copy)));
        }
        return out;
    }

    private static String weekdayVnShort(Calendar cal) {
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return "T2";
            case Calendar.TUESDAY:
                return "T3";
            case Calendar.WEDNESDAY:
                return "T4";
            case Calendar.THURSDAY:
                return "T5";
            case Calendar.FRIDAY:
                return "T6";
            case Calendar.SATURDAY:
                return "T7";
            case Calendar.SUNDAY:
            default:
                return "CN";
        }
    }

    private void updateMonthTitle(int dayIndex) {
        if (dayRibbonAdapter == null || dayIndex < 0 || dayIndex >= dayRibbonAdapter.getItemCount()) return;
        Calendar c = dayRibbonAdapter.getDayAt(dayIndex).getCalendar();
        int m = c.get(Calendar.MONTH) + 1;
        int y = c.get(Calendar.YEAR);
        tvRibbonMonthTitle.setText(String.format(Locale.getDefault(), "THÁNG %d %d", m, y));
    }

    private void seedItinerary(int dayIndex, int totalDays) {
        currentRows.clear();
        int img = R.drawable.bg_image_placeholder;

        String startSegment = startPointLabel != null && !startPointLabel.trim().isEmpty() ? startPointLabel.trim() : "Điểm đi";
        String endSegment = destinationLabel != null && !destinationLabel.trim().isEmpty() ? destinationLabel.trim() : "Điểm đến";

        currentRows.add(new ItinerarySegmentRow(String.format(Locale.getDefault(), "Chặng %d", dayIndex + 1), startSegment, "27°"));
        currentRows.add(new ItineraryStopRow(
                "08:30 – 09:30", "08:30", "09:30",
                "Điểm tham quan tại " + startSegment,
                startSegment + " · Chặng gốc",
                "Cả ngày",
                "Miễn phí",
                "Điểm gợi ý tiếp theo",
                "27°",
                img,
                10.773, 106.698,
                1
        ));
        if (!endSegment.equals(startSegment)) {
            currentRows.add(new ItinerarySegmentRow(String.format(Locale.getDefault(), "Chặng %d", Math.max(dayIndex + 2, 2)), endSegment, "28°"));
            currentRows.add(new ItineraryStopRow(
                    "14:00 – 16:00", "14:00", "16:00",
                    "Điểm tham quan tại " + endSegment,
                    endSegment + " · Chặng gốc",
                    "Cả ngày",
                    "Miễn phí",
                    "Điểm gợi ý tiếp theo",
                    "28°",
                    img,
                    15.880, 108.338,
                    2
            ));
        }

        itineraryAdapter.updateData(new ArrayList<>(currentRows));
        rvTimeline.invalidateItemDecorations();
        tvMapPlaceCount.setText(countStops(currentRows) + " địa điểm");
        if (tvStatPlaces != null) tvStatPlaces.setText(countStops(currentRows) + " địa điểm");
        applyMapMarkers();
    }

    private static int countStops(List<ItineraryRow> rows) {
        int n = 0;
        for (ItineraryRow r : rows) {
            if (r instanceof ItineraryStopRow) n++;
        }
        return n;
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

        boolean isAiGen = isAiGenerated(plannerResponse);
        if (!isAiGen) {
            android.content.SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
            String polylineJson = prefs.getString("route_polyline_" + plannerTripId, null);
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
                            String nodesJson = prefs.getString("route_nodes_" + plannerTripId, null);
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
        }

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        boolean has = false;
        int index = 1;
        for (ItineraryRow r : currentRows) {
            if (r instanceof ItineraryStopRow) {
                ItineraryStopRow s = (ItineraryStopRow) r;
                LatLng ll = new LatLng(s.getLatitude(), s.getLongitude());
                bounds.include(ll);
                has = true;
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(ll)
                        .title(String.valueOf(index++))
                        .snippet(s.getTitle())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                if (marker != null) {
                    mapMarkers.add(marker);
                }
            }
        }
        if (has) {
            LatLngBounds b = bounds.build();
            if (mapMarkers.size() == 1) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(b.getCenter(), 13f));
            } else {
                try {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(b, 80));
                } catch (Exception ignored) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(b.getCenter(), 10f));
                }
            }
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(16.0, 107.5), 5.5f));
        }
    }

    @NonNull
    private static String normalizeTripTitle(
            @Nullable String tripTitle,
            @Nullable String city,
            @Nullable String route,
            @Nullable String startPoint,
            @Nullable String destination
    ) {
        if (tripTitle != null && !tripTitle.trim().isEmpty()) {
            return tripTitle.trim();
        }
        if (city != null && !city.trim().isEmpty()) {
            return city.trim();
        }
        return resolveRouteLabel(route, startPoint, destination, city);
    }


    @NonNull
    private static String resolveRouteLabel(@Nullable String route, @Nullable String startPoint, @Nullable String destination, @Nullable String city) {
        if (route != null && !route.trim().isEmpty()) {
            return route;
        }
        if (startPoint != null && !startPoint.trim().isEmpty() && destination != null && !destination.trim().isEmpty()) {
            return startPoint.trim() + " → " + destination.trim();
        }
        if (city != null && !city.trim().isEmpty()) {
            return city;
        }
        return "Hồ Chí Minh → Hội An → Đà Nẵng";
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapBundle == null) {
            mapBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapBundle);
        }
        mapView.onSaveInstanceState(mapBundle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_SEGMENT && resultCode == RESULT_OK) {
            if (plannerTripId != null && !plannerTripId.trim().isEmpty()) {
                loadPlannerData(plannerTripId);
            }
        }
    }

    private void initReviewCardViews() {
        cardReviewFields = findViewById(R.id.cardReviewFields);
        layoutTimelineContainer = findViewById(R.id.layoutTimelineContainer);
        tvReviewTripTitle = findViewById(R.id.tvReviewTripTitle);
        tvReviewStartLocation = findViewById(R.id.tvReviewStartLocation);
        tvReviewStartDistrict = findViewById(R.id.tvReviewStartDistrict);
        tvReviewSegmentLocation = findViewById(R.id.tvReviewSegmentLocation);
        tvReviewSegmentDistrict = findViewById(R.id.tvReviewSegmentDistrict);
        tvReviewSegmentDates = findViewById(R.id.tvReviewSegmentDates);

        btnEditTripTitle = findViewById(R.id.btnEditTripTitle);
        btnEditStartLocation = findViewById(R.id.btnEditStartLocation);
        btnEditStartDistrict = findViewById(R.id.btnEditStartDistrict);
        btnEditSegmentLocation = findViewById(R.id.btnEditSegmentLocation);
        btnEditSegmentDistrict = findViewById(R.id.btnEditSegmentDistrict);
        btnEditSegmentDates = findViewById(R.id.btnEditSegmentDates);

        if (btnEditTripTitle != null) {
            btnEditTripTitle.setOnClickListener(v -> showEditTitleDialog());
        }

        View.OnClickListener editSegmentListener = v -> {
            if (plannerTripId == null || plannerTripId.trim().isEmpty()) {
                AppToast.show(this, "Không có Trip ID");
                return;
            }
            String city = getIntent().getStringExtra(EXTRA_CITY);
            String tripTitle = getIntent().getStringExtra(EXTRA_TRIP_TITLE);
            String dates = getIntent().getStringExtra(EXTRA_DATES);
            String route = getIntent().getStringExtra(EXTRA_ROUTE);

            android.content.Intent intent = new android.content.Intent(TripDetailActivity.this, EditSegmentActivity.class);
            intent.putExtra("planner_trip_id", plannerTripId);
            intent.putExtra(EXTRA_CITY, city);
            intent.putExtra(EXTRA_TRIP_TITLE, tripTitleLabel != null ? tripTitleLabel : tripTitle);
            intent.putExtra(EXTRA_DATES, dates != null ? dates : pendingDates);
            intent.putExtra(EXTRA_START_POINT, startPointLabel);
            intent.putExtra(EXTRA_DESTINATION, destinationLabel);
            intent.putExtra(EXTRA_ROUTE, route);
            startActivityForResult(intent, REQUEST_CODE_EDIT_SEGMENT);
        };

        if (btnEditStartLocation != null) {
            btnEditStartLocation.setVisibility(View.VISIBLE);
            btnEditStartLocation.setOnClickListener(editSegmentListener);
        }
        if (btnEditStartDistrict != null) {
            btnEditStartDistrict.setVisibility(View.VISIBLE);
            btnEditStartDistrict.setOnClickListener(editSegmentListener);
        }

        if (btnEditSegmentLocation != null) {
            btnEditSegmentLocation.setOnClickListener(editSegmentListener);
        }
        if (btnEditSegmentDistrict != null) {
            btnEditSegmentDistrict.setOnClickListener(editSegmentListener);
        }
        if (btnEditSegmentDates != null) {
            btnEditSegmentDates.setOnClickListener(editSegmentListener);
        }
    }

    private void populateReviewCard() {
        if (cardReviewFields == null) return;

        android.content.SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);

        String localTitle = prefs.getString("trip_local_title_" + plannerTripId, tripTitleLabel);
        if (localTitle == null || localTitle.trim().isEmpty()) {
            localTitle = tripTitleLabel != null ? tripTitleLabel : "--";
        }
        tvReviewTripTitle.setText(localTitle);

        String localStartLoc = prefs.getString("trip_start_location_" + plannerTripId, startPointLabel);
        if (localStartLoc == null || localStartLoc.trim().isEmpty()) {
            localStartLoc = startPointLabel != null ? startPointLabel : "--";
        }
        tvReviewStartLocation.setText(localStartLoc);

        String localStartDist = prefs.getString("trip_start_district_" + plannerTripId, "--");
        tvReviewStartDistrict.setText(localStartDist);

        String localSegLoc = prefs.getString("trip_segment_location_" + plannerTripId, null);
        if (localSegLoc == null || localSegLoc.trim().isEmpty()) {
            localSegLoc = destinationLabel != null ? destinationLabel : "--";
        }
        tvReviewSegmentLocation.setText(localSegLoc);

        String localSegDist = prefs.getString("trip_segment_district_" + plannerTripId, null);
        if (localSegDist == null || localSegDist.trim().isEmpty()) {
            localSegDist = prefs.getString("trip_end_district_" + plannerTripId, "--");
        }
        tvReviewSegmentDistrict.setText(localSegDist);

        String localSegDates = prefs.getString("trip_segment_dates_" + plannerTripId, null);
        if (localSegDates == null || localSegDates.trim().isEmpty()) {
            if (plannerResponse != null && plannerResponse.getSegments() != null) {
                com.example.weathertrip_sep490.model.TripSegmentResponse seg = getStopSegmentToEdit(plannerResponse);
                if (seg != null && seg.getStartDate() != null && seg.getEndDate() != null) {
                    try {
                        java.text.DateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        localSegDates = df.format(seg.getStartDate()) + " - " + df.format(seg.getEndDate());
                    } catch (Exception ignored) {}
                }
            }
            if (localSegDates == null || localSegDates.trim().isEmpty()) {
                localSegDates = pendingDates != null ? pendingDates : "--";
            }
        }
        tvReviewSegmentDates.setText(localSegDates);
    }

    private void showEditTitleDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Sửa tên chuyến đi");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        input.setText(tripTitleLabel != null ? tripTitleLabel : "");
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                tripTitleLabel = newTitle;
                TextView tvJourneyTitle = findViewById(R.id.tvTripJourneyTitle);
                if (tvJourneyTitle != null) tvJourneyTitle.setText(newTitle);
                if (tvReviewTripTitle != null) tvReviewTripTitle.setText(newTitle);

                android.content.SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
                prefs.edit().putString("trip_local_title_" + plannerTripId, newTitle).apply();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditStartPointDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_start_point, null);
        builder.setView(dialogView);

        AutoCompleteTextView etStartLoc = dialogView.findViewById(R.id.etEditStartLocation);
        Spinner spStartDist = dialogView.findViewById(R.id.spinnerEditStartDistrict);

        final List<String> locItems = new ArrayList<>();
        final Map<String, String> locNameToId = new HashMap<>();
        final List<String> distLabels = new ArrayList<>();
        final List<String> distIds = new ArrayList<>();
        final ArrayAdapter<String> distAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, distLabels);
        distAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStartDist.setAdapter(distAdapter);

        etStartLoc.setText(startPointLabel != null ? startPointLabel : "");

        RetrofitClient.getInstance().getUserAPI().getAllLocations().enqueue(new Callback<List<LocationOption>>() {
            @Override
            public void onResponse(@NonNull Call<List<LocationOption>> call, @NonNull Response<List<LocationOption>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    locItems.clear();
                    locNameToId.clear();
                    for (LocationOption loc : response.body()) {
                        if (loc != null && loc.getLocationName() != null) {
                            locItems.add(loc.getLocationName());
                            locNameToId.put(loc.getLocationName(), loc.getLocationId());
                        }
                    }
                    ArrayAdapter<String> locAdapter = new ArrayAdapter<>(
                            TripDetailActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            locItems
                    );
                    etStartLoc.setAdapter(locAdapter);
                }
            }
            @Override public void onFailure(@NonNull Call<List<LocationOption>> call, @NonNull Throwable t) {}
        });

        etStartLoc.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            String locId = locNameToId.get(selectedName);
            if (locId != null) {
                RetrofitClient.getInstance().getUserAPI().getDistrictsByLocation(locId).enqueue(new Callback<List<DistrictOption>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<DistrictOption>> call, @NonNull Response<List<DistrictOption>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            distLabels.clear();
                            distIds.clear();
                            distLabels.add("-- Chọn quận/huyện --");
                            distIds.add("");
                            for (DistrictOption d : response.body()) {
                                if (d != null && d.getName() != null) {
                                    distLabels.add(d.getName());
                                    distIds.add(d.getId());
                                }
                            }
                            distAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override public void onFailure(@NonNull Call<List<DistrictOption>> call, @NonNull Throwable t) {}
                });
            }
        });

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newLoc = etStartLoc.getText().toString().trim();
            int pos = spStartDist.getSelectedItemPosition();
            String newDist = (pos > 0 && pos < distLabels.size()) ? distLabels.get(pos) : "";

            if (!newLoc.isEmpty()) {
                startPointLabel = newLoc;
                if (tvReviewStartLocation != null) tvReviewStartLocation.setText(newLoc);
                if (tvReviewStartDistrict != null) tvReviewStartDistrict.setText(newDist.isEmpty() ? "--" : newDist);

                TextView tvRoute = findViewById(R.id.tvTripRouteBreadcrumb);
                if (tvRoute != null) {
                    tvRoute.setText(startPointLabel + " → " + (destinationLabel != null ? destinationLabel : ""));
                }

                android.content.SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
                prefs.edit()
                     .putString("trip_start_location_" + plannerTripId, newLoc)
                     .putString("trip_start_district_" + plannerTripId, newDist)
                     .apply();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private com.example.weathertrip_sep490.model.TripSegmentResponse getStopSegmentToEdit(com.example.weathertrip_sep490.model.PlannerTripResponse response) {
        if (response == null || response.getSegments() == null || response.getSegments().isEmpty()) {
            return null;
        }
        List<com.example.weathertrip_sep490.model.TripSegmentResponse> segments = response.getSegments();
        // Fallback to middle segment (index 1) if size >= 3
        if (segments.size() >= 3) {
            return segments.get(1);
        }
        // Fallback to the last segment
        return segments.get(segments.size() - 1);
    }
}
