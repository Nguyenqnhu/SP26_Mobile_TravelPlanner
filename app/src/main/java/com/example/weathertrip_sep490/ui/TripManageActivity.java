package com.example.weathertrip_sep490.ui;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripManageActivity extends AppCompatActivity implements CreateTripBottomSheet.Listener {

    private final List<Trip> allTrips = new ArrayList<>();
    private EditText etSearch;
    private TextView tvEmpty;
    private TripCardAdapter tripAdapter;

    private TextView tabAll;
    private TextView tabActive;
    private TextView tabDone;

    private String searchQuery = "";
    private int filterTabIndex;

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

        seedSampleTrips();

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
                intent.putExtra(TripDetailActivity.EXTRA_DATES, trip.getDateRange());
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

    private void seedSampleTrips() {
        int ph = R.drawable.bg_image_placeholder;
        allTrips.add(new Trip("1", "Đà Lạt", "31/3/2026 - 5/4/2026", "10.000.000 đ", TripStatus.UPCOMING, ph));
        allTrips.add(new Trip("2", "Hội An", "15/3/2026 - 22/3/2026", null, TripStatus.ONGOING, ph));
        allTrips.add(new Trip("3", "Sapa", "1/3/2026 - 8/3/2026", "8.500.000 đ", TripStatus.ONGOING, ph));
        allTrips.add(new Trip("4", "Nha Trang", "10/3/2026 - 14/3/2026", null, TripStatus.ONGOING, ph));
        allTrips.add(new Trip("5", "Phú Quốc", "1/2/2026 - 7/2/2026", "15.000.000 đ", TripStatus.COMPLETED, ph));
        allTrips.add(new Trip("6", "Huế", "20/1/2026 - 25/1/2026", null, TripStatus.COMPLETED, ph));
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

    @Override
    public void onCreateTripWithAi(
            @NonNull String startPoint,
            @NonNull String destination,
            boolean roundTrip,
            @NonNull String startDateDisplay,
            @NonNull String endDateDisplay
    ) {
        String title = startPoint + " → " + destination;
        String range = roundTrip
                ? (startDateDisplay + " - " + endDateDisplay)
                : (startDateDisplay + " · 1 chiều");
        int ph = R.drawable.bg_image_placeholder;
        allTrips.add(0, new Trip(String.valueOf(System.currentTimeMillis()), title, range, null, TripStatus.UPCOMING, ph));
        refreshStatusTabLabels();
        applyTabVisualState();
        applyFiltersAndRefresh();
        Toast.makeText(
                this,
                "Đã tạo chuyến với AI (demo): " + title + (roundTrip ? "" : " (1 chiều)"),
                Toast.LENGTH_SHORT
        ).show();
    }
}
