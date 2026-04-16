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
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.ItineraryListAdapter;
import com.example.weathertrip_sep490.adapter.TripDateRibbonAdapter;
import com.example.weathertrip_sep490.ui.decoration.ItineraryTimelineLineDecoration;
import com.example.weathertrip_sep490.model.ItineraryRow;
import com.example.weathertrip_sep490.model.ItinerarySegmentRow;
import com.example.weathertrip_sep490.model.ItineraryStopRow;
import com.example.weathertrip_sep490.model.TripRibbonDay;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TripDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_CITY = "extra_city";
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

    private MapView mapView;
    private GoogleMap googleMap;
    private boolean mapReady;

    private final List<Marker> mapMarkers = new ArrayList<>();
    private final List<ItineraryRow> currentRows = new ArrayList<>();

    private TripDateRibbonAdapter dayRibbonAdapter;
    private ItineraryListAdapter itineraryAdapter;
    private RecyclerView rvDays;
    private RecyclerView rvTimeline;

    private TextView tvRibbonMonthTitle;
    private TextView tvMapPlaceCount;

    /** true: sau khi AI generate — ribbon theo ngày thật của trip, timeline từ segment */
    private boolean useGeneratedItineraryMode;
    private String generatedLocationName;
    private double generatedLat;
    private double generatedLng;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        String city = getIntent().getStringExtra(EXTRA_CITY);
        String dates = getIntent().getStringExtra(EXTRA_DATES);
        String startPoint = getIntent().getStringExtra(EXTRA_START_POINT);
        String destination = getIntent().getStringExtra(EXTRA_DESTINATION);
        String route = getIntent().getStringExtra(EXTRA_ROUTE);
        String segLocation = getIntent().getStringExtra(EXTRA_GENERATED_SEGMENT_LOCATION);
        String segStart = getIntent().getStringExtra(EXTRA_GENERATED_SEGMENT_START);
        String segEnd = getIntent().getStringExtra(EXTRA_GENERATED_SEGMENT_END);
        double segLat = getIntent().getDoubleExtra(EXTRA_GENERATED_SEGMENT_LAT, 10.8231);
        double segLng = getIntent().getDoubleExtra(EXTRA_GENERATED_SEGMENT_LNG, 106.6297);
        boolean hasGeneratedData = segLocation != null && !segLocation.trim().isEmpty();

        ImageView btnBack = findViewById(R.id.btnBackTripDetail);
        TextView tvJourneyTitle = findViewById(R.id.tvTripJourneyTitle);
        TextView tvDates = findViewById(R.id.tvTripDetailDates);
        TextView tvRoute = findViewById(R.id.tvTripRouteBreadcrumb);
        TextView tvStatDays = findViewById(R.id.tvTripStatDays);
        TextView tvStatPlaces = findViewById(R.id.tvTripStatPlaces);
        tvRibbonMonthTitle = findViewById(R.id.tvRibbonMonthTitle);
        tvMapPlaceCount = findViewById(R.id.tvMapPlaceCount);

        tvJourneyTitle.setText(getString(R.string.trip_journey_at, primaryDestination(city)));
        tvDates.setText(dates != null && !dates.isEmpty() ? dates : "5/4/2026 – 12/4/2026");
        tvRoute.setText(resolveRouteLabel(route, startPoint, destination, city));
        btnBack.setOnClickListener(v -> finish());
        findViewById(R.id.btnTripInvite).setOnClickListener(v ->
                Toast.makeText(this, "Mời bạn bè cùng xem chuyến đi", Toast.LENGTH_SHORT).show());

        initMapView(savedInstanceState);

        rvTimeline = findViewById(R.id.rvTripTimeline);
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        itineraryAdapter = new ItineraryListAdapter();
        rvTimeline.setAdapter(itineraryAdapter);
        rvTimeline.addItemDecoration(new ItineraryTimelineLineDecoration(itineraryAdapter, getResources()));

        if (hasGeneratedData) {
            generatedLocationName = segLocation.trim();
            generatedLat = segLat;
            generatedLng = segLng;
            List<TripRibbonDay> days = buildRibbonDaysForTrip(dates, segStart, segEnd);
            if (days.isEmpty()) {
                Calendar today = Calendar.getInstance();
                days.add(new TripRibbonDay(normalizeDayStart(today), weekdayVnShort(today)));
            }
            tvStatDays.setText(days.size() + " ngày");
            setupDayRibbon(days, true);
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
            seedItinerary(0, days.size());
            updateMonthTitle(0);
        }
    }

    /**
     * Lịch trình theo ngày (vuốt ribbon) — dùng segment + địa điểm; nội dung gợi ý theo ngày.
     */
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
        TextView tvSp = findViewById(R.id.tvTripStatPlaces);
        if (tvSp != null) {
            tvSp.setText(countStops(currentRows) + " hoạt động");
        }
        applyMapMarkers();
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
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            f.setLenient(false);
            Calendar c = Calendar.getInstance();
            c.setTime(f.parse(s.trim()));
            return normalizeDayStart(c);
        } catch (ParseException e) {
            return null;
        }
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
        useGeneratedItineraryMode = generatedMode;
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

        SnapHelper snapHelper = new LinearSnapHelper();
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

    private void seedForRibbonDay(int pos) {
        int totalDays = dayRibbonAdapter != null ? dayRibbonAdapter.getItemCount() : 1;
        if (useGeneratedItineraryMode) {
            seedGeneratedItinerary(pos, totalDays);
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

        String startSegment = getRouteSegmentLabel(0);
        String endSegment = totalDays > 1 ? getRouteSegmentLabel(totalDays - 1) : startSegment;

        currentRows.add(new ItinerarySegmentRow(String.format(Locale.getDefault(), "Chặng %d", dayIndex + 1), startSegment, "27°"));
        currentRows.add(new ItineraryStopRow(
                "08:30 – 09:30", "08:30", "09:30",
                "Điểm tham quan tại " + startSegment,
                startSegment + " · Chặng user đã chọn",
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
                    endSegment + " · Chặng user đã chọn",
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

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        boolean has = false;
        for (ItineraryRow r : currentRows) {
            if (r instanceof ItineraryStopRow) {
                ItineraryStopRow s = (ItineraryStopRow) r;
                LatLng ll = new LatLng(s.getLatitude(), s.getLongitude());
                bounds.include(ll);
                has = true;
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(ll)
                        .title(String.valueOf(s.getMarkerOrder()))
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

    private static String primaryDestination(@Nullable String city) {
        if (city == null || city.trim().isEmpty()) return "Việt Nam";
        String c = city.trim();
        int idx = c.lastIndexOf('→');
        if (idx >= 0 && idx < c.length() - 1) {
            return c.substring(idx + 1).trim();
        }
        return c;
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

    @NonNull
    private String getRouteSegmentLabel(int index) {
        String route = getIntent().getStringExtra(EXTRA_ROUTE);
        if (route == null || route.trim().isEmpty()) {
            return "Chặng user đã chọn";
        }
        String[] parts = route.split("→");
        if (parts.length == 0) {
            return route.trim();
        }
        int safeIndex = Math.max(0, Math.min(index, parts.length - 1));
        return parts[safeIndex].trim();
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
}
