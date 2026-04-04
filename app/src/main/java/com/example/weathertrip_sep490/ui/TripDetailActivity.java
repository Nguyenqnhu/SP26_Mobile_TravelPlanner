package com.example.weathertrip_sep490.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TripDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_CITY = "extra_city";
    public static final String EXTRA_DATES = "extra_dates";
    public static final String EXTRA_ROUTE = "extra_route";

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        String city = getIntent().getStringExtra(EXTRA_CITY);
        String dates = getIntent().getStringExtra(EXTRA_DATES);
        String route = getIntent().getStringExtra(EXTRA_ROUTE);

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
        tvRoute.setText(route != null && !route.isEmpty()
                ? route
                : "Hồ Chí Minh → Hội An → Đà Nẵng → Huế → Hà Nội");
        tvStatDays.setText("8 ngày");
        tvStatPlaces.setText("15 địa điểm gợi ý");

        btnBack.setOnClickListener(v -> finish());
        findViewById(R.id.btnTripInvite).setOnClickListener(v ->
                Toast.makeText(this, "Mời bạn bè cùng xem chuyến đi", Toast.LENGTH_SHORT).show());

        initMapView(savedInstanceState);

        rvTimeline = findViewById(R.id.rvTripTimeline);
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        itineraryAdapter = new ItineraryListAdapter();
        rvTimeline.setAdapter(itineraryAdapter);
        rvTimeline.addItemDecoration(new ItineraryTimelineLineDecoration(itineraryAdapter, getResources()));

        setupDayRibbon();


        seedItinerary(0);
        updateMonthTitle(0);
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

    private void setupDayRibbon() {
        rvDays = findViewById(R.id.rvTripDetailDays);
        rvDays.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<TripRibbonDay> days = buildRibbonDays();
        dayRibbonAdapter = new TripDateRibbonAdapter((pos, day) -> {
            rvDays.smoothScrollToPosition(pos);
            seedItinerary(pos);
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
                    seedItinerary(pos);
                    updateMonthTitle(pos);
                    applyMapMarkers();
                }
            }
        });
    }

    private void shiftRibbonDay(int delta) {
        int next = dayRibbonAdapter.getSelectedPosition() + delta;
        if (next < 0 || next >= dayRibbonAdapter.getItemCount()) return;
        dayRibbonAdapter.setSelectedPosition(next);
        rvDays.smoothScrollToPosition(next);
        seedItinerary(next);
        updateMonthTitle(next);
        applyMapMarkers();
    }

    private List<TripRibbonDay> buildRibbonDays() {
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

    private void seedItinerary(int dayIndex) {
        currentRows.clear();
        int img = R.drawable.bg_image_placeholder;

        if (dayIndex % 2 == 0) {
            currentRows.add(new ItinerarySegmentRow("Segment 1", "Hồ Chí Minh", "27°"));
            currentRows.add(new ItineraryStopRow(
                    "08:30 – 09:30", "08:30", "09:30",
                    "Chợ địa phương Hồ Chí Minh",
                    "Hồ Chí Minh · Khu vực trung tâm",
                    "9:00 – 18:00",
                    "Từ 30.000 đ",
                    "Cà phê view đẹp Hồ Chí Minh",
                    "27°",
                    img,
                    10.773, 106.698,
                    1
            ));
            currentRows.add(new ItineraryStopRow(
                    "11:00 – 12:00", "11:00", "12:00",
                    "Cà phê view đẹp Hồ Chí Minh",
                    "Quận 1 · View phố",
                    "8:00 – 11:00 và 14:00 – 20:00",
                    "Miễn phí",
                    "Nhà hàng đặc sản",
                    "28°",
                    img,
                    10.782, 106.698,
                    2
            ));
            currentRows.add(new ItinerarySegmentRow("Segment 2", "Hội An", "28°"));
            currentRows.add(new ItineraryStopRow(
                    "14:00 – 16:00", "14:00", "16:00",
                    "Phố cổ Hội An",
                    "Hội An · Di sản",
                    "Cả ngày",
                    "Miễn phí tham quan",
                    "Bãi biển An Bàng",
                    "28°",
                    img,
                    15.880, 108.338,
                    3
            ));
        } else {
            currentRows.add(new ItinerarySegmentRow("Segment 1", "Đà Nẵng", "26°"));
            currentRows.add(new ItineraryStopRow(
                    "09:00 – 11:00", "09:00", "11:00",
                    "Bán đảo Sơn Trà",
                    "Đà Nẵng",
                    "7:00 – 17:30",
                    "Miễn phí",
                    "Cầu Rồng buổi tối",
                    "26°",
                    img,
                    16.059, 108.245,
                    1
            ));
            currentRows.add(new ItineraryStopRow(
                    "15:30 – 17:00", "15:30", "17:00",
                    "Cầu Rồng",
                    "Sông Hàn",
                    "19:00 – 22:00 (lửa)",
                    "Miễn phí",
                    "Ẩm thực đêm",
                    "25°",
                    img,
                    16.061, 108.228,
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
