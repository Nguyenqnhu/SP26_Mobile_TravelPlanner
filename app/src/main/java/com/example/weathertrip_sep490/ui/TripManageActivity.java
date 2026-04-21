package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.TripCardAdapter;
import com.example.weathertrip_sep490.model.Trip;
import com.example.weathertrip_sep490.model.TripStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripManageActivity extends AppCompatActivity implements CreateTripBottomSheet.Listener, AddSegmentBottomSheet.Listener, SegmentManagerBottomSheet.Listener {

    private static final String PREFS_NAME = "TravelGoPrefs";
    private static final String KEY_MANAGED_TRIPS_PREFIX = "managed_trips_json_";

    private final List<Trip> allTrips = new ArrayList<>();
    private EditText etSearch;
    private TextView tvEmpty;
    private TripCardAdapter tripAdapter;

    private TextView tabAll;
    private TextView tabActive;
    private TextView tabDone;

    private String searchQuery = "";
    private int filterTabIndex;

    private String pendingTripTitle;
    private String pendingTripId;
    private String pendingStartPoint;
    private String pendingDestination;
    private boolean pendingRoundTrip;
    private String pendingStartDateDisplay;
    private String pendingEndDateDisplay;
    private String pendingStartDateIso;
    private String pendingEndDateIso;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_manage);

        etSearch = findViewById(R.id.etSearchTrip);
        tvEmpty = findViewById(R.id.tvTripEmpty);
        RecyclerView rvTrips = findViewById(R.id.rvTrips);

        tabAll = findViewById(R.id.tabTripAll);
        tabActive = findViewById(R.id.tabTripActive);
        tabDone = findViewById(R.id.tabTripDone);

        loadTripsFromLocal();

        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        tripAdapter = new TripCardAdapter(new TripCardAdapter.TripCardListener() {
            @Override
            public void onReplan(Trip trip) {
                Toast.makeText(TripManageActivity.this, "Re-plan: " + trip.getCity(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onViewDetails(Trip trip) {
                Intent intent = new Intent(TripManageActivity.this, TripDetailActivity.class);
                intent.putExtra(TripDetailActivity.EXTRA_CITY, trip.getCity());
                intent.putExtra(TripDetailActivity.EXTRA_TRIP_TITLE, trip.getTripTitle());
                intent.putExtra(TripDetailActivity.EXTRA_DATES, trip.getDateRange());
                intent.putExtra(TripDetailActivity.EXTRA_START_POINT, trip.getStartPoint());
                intent.putExtra(TripDetailActivity.EXTRA_DESTINATION, trip.getDestination());
                intent.putExtra(TripDetailActivity.EXTRA_ROUTE, buildRouteLabel(trip.getStartPoint(), trip.getDestination()));
                intent.putExtra("planner_trip_id", trip.getId());
                startActivity(intent);
            }

            @Override
            public void onReview(Trip trip) {
                Toast.makeText(TripManageActivity.this, "Đánh giá: " + trip.getCity(), Toast.LENGTH_SHORT).show();
            }
        });
        rvTrips.setAdapter(tripAdapter);

        tabAll.setOnClickListener(v -> selectStatusTab(0));
        tabActive.setOnClickListener(v -> selectStatusTab(1));
        tabDone.setOnClickListener(v -> selectStatusTab(2));

        refreshStatusTabLabels();
        selectStatusTab(0);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s != null ? s.toString() : "";
                refreshStatusTabLabels();
                applyTabVisualState();
                applyFiltersAndRefresh();
            }
        });

        findViewById(R.id.btnCreateTrip).setOnClickListener(v ->
                new CreateTripBottomSheet().show(getSupportFragmentManager(), CreateTripBottomSheet.TAG));

        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTripsWithCurrentStatus();
        refreshStatusTabLabels();
        applyTabVisualState();
        applyFiltersAndRefresh();
    }

    private void refreshStatusTabLabels() {
        List<String> labels = buildChipLabels();
        tabAll.setText(labels.get(0));
        tabActive.setText(labels.get(1));
        tabDone.setText(labels.get(2));
    }

    private void selectStatusTab(int index) {
        filterTabIndex = index;
        applyTabVisualState();
        applyFiltersAndRefresh();
    }

    private void applyTabVisualState() {
        styleStatusTab(tabAll, filterTabIndex == 0);
        styleStatusTab(tabActive, filterTabIndex == 1);
        styleStatusTab(tabDone, filterTabIndex == 2);
    }

    private void styleStatusTab(TextView tab, boolean selected) {
        int green = ContextCompat.getColor(this, R.color.text_title);
        int grey = Color.parseColor("#9CA3AF");
        float ez = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());

        if (selected) {
            tab.setBackgroundResource(R.drawable.bg_trip_status_thumb);
            tab.setTextColor(green);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tab.setElevation(ez);
            }
        } else {
            tab.setBackground(null);
            tab.setTextColor(grey);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tab.setElevation(0f);
            }
        }
    }

    private String getManagedTripsKey() {
        String userId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString("current_user_id", "");
        if (userId == null || userId.trim().isEmpty()) {
            return KEY_MANAGED_TRIPS_PREFIX + "anonymous";
        }
        return KEY_MANAGED_TRIPS_PREFIX + userId.trim();
    }

    private void loadTripsFromLocal() {
        allTrips.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(getManagedTripsKey(), null);
        if (json == null || json.trim().isEmpty()) return;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String id = o.optString("id", String.valueOf(System.currentTimeMillis() + i));
                String title = o.optString("title", "");
                String city = o.optString("city", "");
                String startPoint = o.optString("startPoint", "");
                String destination = o.optString("destination", city);
                String range = o.optString("range", "");
                String startIso = o.optString("startDateIso", "");
                String endIso = o.optString("endDateIso", "");
                String cost = o.optString("cost", null);
                String statusRaw = o.optString("status", TripStatus.UPCOMING.name());
                TripStatus status;
                try {
                    status = TripStatus.valueOf(statusRaw);
                } catch (Exception ignored) {
                    status = TripStatus.UPCOMING;
                }
                TripStatus computedStatus = computeStatusFromDate(startIso, endIso, status);
                allTrips.add(new Trip(id, title, startPoint, destination, range, startIso, endIso, cost, computedStatus, R.drawable.sampleplace));
            }
        } catch (Exception ignored) { }
    }

    private void persistTripsToLocal() {
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
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putString(getManagedTripsKey(), arr.toString())
                .apply();
    }

    private void refreshTripsWithCurrentStatus() {
        if (allTrips.isEmpty()) {
            return;
        }
        List<Trip> normalized = new ArrayList<>();
        for (Trip trip : allTrips) {
            TripStatus computed = computeStatusFromDate(trip.getStartDateIso(), trip.getEndDateIso(), trip.getStatus());
            normalized.add(new Trip(
                    trip.getId(),
                    trip.getTripTitle(),
                    trip.getStartPoint(),
                    trip.getDestination(),
                    trip.getDateRange(),
                    trip.getStartDateIso(),
                    trip.getEndDateIso(),
                    trip.getCostDisplay(),
                    computed,
                    trip.getImageResId()
            ));
        }
        allTrips.clear();
        allTrips.addAll(normalized);
        persistTripsToLocal();
    }

    @NonNull
    private TripStatus computeStatusFromDate(@Nullable String startIso, @Nullable String endIso, @Nullable TripStatus fallback) {
        Date startDate = parseIsoDate(startIso);
        Date endDate = parseIsoDate(endIso);
        if (startDate == null || endDate == null) {
            return fallback != null ? fallback : TripStatus.UPCOMING;
        }

        Calendar now = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        resetStartOfDay(start);

        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        setEndOfDay(end);

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
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setLenient(false);
                return sdf.parse(text);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void resetStartOfDay(@NonNull Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setEndOfDay(@NonNull Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    private List<String> buildChipLabels() {
        List<Trip> byCity = filterByCitySearch(allTrips, searchQuery);
        int nAll = byCity.size();
        int nActive = 0;
        int nDone = 0;
        for (Trip t : byCity) {
            if (t.isActive()) {
                nActive++;
            } else if (t.getStatus() == TripStatus.COMPLETED) {
                nDone++;
            }
        }
        List<String> labels = new ArrayList<>();
        labels.add("Tất cả (" + nAll + ")");
        labels.add("Đang diễn ra (" + nActive + ")");
        labels.add("Đã hoàn thành (" + nDone + ")");
        return labels;
    }

    private List<Trip> filterByCitySearch(List<Trip> source, String rawQuery) {
        String q = rawQuery == null ? "" : rawQuery.trim().toLowerCase(Locale.ROOT);
        List<Trip> out = new ArrayList<>();
        for (Trip t : source) {
            if (q.isEmpty()) {
                out.add(t);
            } else {
                String city = t.getCity() != null ? t.getCity().toLowerCase(Locale.ROOT) : "";
                if (city.contains(q)) {
                    out.add(t);
                }
            }
        }
        return out;
    }

    private void applyFiltersAndRefresh() {
        List<Trip> byCity = filterByCitySearch(allTrips, searchQuery);
        List<Trip> result = new ArrayList<>();
        for (Trip t : byCity) {
            if (filterTabIndex == 0) {
                result.add(t);
            } else if (filterTabIndex == 1) {
                if (t.isActive()) {
                    result.add(t);
                }
            } else {
                if (t.getStatus() == TripStatus.COMPLETED) {
                    result.add(t);
                }
            }
        }
        tripAdapter.updateData(result);
        tvEmpty.setVisibility(result.isEmpty() ? TextView.VISIBLE : TextView.GONE);
    }

    private void setupBottomNav() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_trip);
        if (bottomNav == null) return;
        bottomNav.setSelectedItemId(R.id.nav_trip);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_trip) {
                return true;
            }
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomepageActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_explore) {
                startActivity(new Intent(this, ExploreActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_user) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    @NonNull
    private static String buildRouteLabel(@Nullable String startPoint, @Nullable String destination) {
        String start = startPoint != null && !startPoint.trim().isEmpty() ? startPoint.trim() : "Hồ Chí Minh";
        String end = destination != null && !destination.trim().isEmpty() ? destination.trim() : "Đà Nẵng";
        return start + " → " + end;
    }

    @Override
    public void onTripCreated(
            @NonNull String tripId,
            @NonNull String tripTitle,
            @NonNull String startPoint,
            @NonNull String destination,
            boolean roundTrip,
            @NonNull String startDateDisplay,
            @NonNull String endDateDisplay,
            @NonNull String startDateIso,
            @NonNull String endDateIso
    ) {
        pendingTripTitle = tripTitle;
        pendingTripId = tripId;
        pendingStartPoint = startPoint;
        pendingDestination = destination;
        pendingRoundTrip = roundTrip;
        pendingStartDateDisplay = startDateDisplay;
        pendingEndDateDisplay = endDateDisplay;
        pendingStartDateIso = startDateIso;
        pendingEndDateIso = endDateIso;

        SegmentManagerBottomSheet.newInstance(
                tripId,
                tripTitle,
                startPoint,
                destination,
                startDateIso,
                endDateIso
        ).show(getSupportFragmentManager(), "SegmentManagerBottomSheet");
    }

    @Override
    public void onSegmentAddedAndReadyForAi(@NonNull String tripId, @NonNull String locationName, @NonNull String segmentStartDate, @NonNull String segmentEndDate, double latitude, double longitude) {
        // handled inside SegmentManagerBottomSheet via reloadPlanner()
    }

    @Override
    public void onGenerateCompleted(@NonNull String tripId) {
        String city = (pendingDestination != null && !pendingDestination.trim().isEmpty()) ? pendingDestination.trim() : (pendingTripTitle != null ? pendingTripTitle : "");
        String dates = buildDateRangeDisplay(pendingStartDateDisplay, pendingEndDateDisplay, pendingRoundTrip);
        TripStatus status = computeStatusFromDate(pendingStartDateIso, pendingEndDateIso, TripStatus.UPCOMING);

        upsertManagedTrip(new Trip(
                tripId,
                pendingTripTitle != null ? pendingTripTitle : city,
                pendingStartPoint != null ? pendingStartPoint : "",
                pendingDestination != null ? pendingDestination : city,
                dates,
                pendingStartDateIso,
                pendingEndDateIso,
                null,
                status,
                R.drawable.sampleplace
        ));

        refreshStatusTabLabels();
        applyTabVisualState();
        applyFiltersAndRefresh();

        Intent intent = new Intent(this, TripDetailActivity.class);
        intent.putExtra("planner_trip_id", tripId);
        intent.putExtra(TripDetailActivity.EXTRA_CITY, city);
        intent.putExtra(TripDetailActivity.EXTRA_TRIP_TITLE, pendingTripTitle);
        intent.putExtra(TripDetailActivity.EXTRA_DATES, dates);
        intent.putExtra(TripDetailActivity.EXTRA_START_POINT, pendingStartPoint);
        intent.putExtra(TripDetailActivity.EXTRA_DESTINATION, pendingDestination);
        intent.putExtra(TripDetailActivity.EXTRA_ROUTE, buildRouteLabel(pendingStartPoint, pendingDestination));
        startActivity(intent);
    }

    private void upsertManagedTrip(@NonNull Trip trip) {
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
        persistTripsToLocal();
    }

    @NonNull
    private String buildDateRangeDisplay(@Nullable String startDisplay, @Nullable String endDisplay, boolean roundTrip) {
        String start = startDisplay != null ? startDisplay.trim() : "";
        String end = endDisplay != null ? endDisplay.trim() : "";
        if (start.isEmpty()) return "—";
        if (!roundTrip || end.isEmpty()) return start + " · 1 chiều";
        return start + " - " + end;
    }
}
