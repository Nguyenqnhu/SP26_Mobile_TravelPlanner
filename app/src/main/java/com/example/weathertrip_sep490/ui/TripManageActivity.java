package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.FilterChipAdapter;
import com.example.weathertrip_sep490.adapter.TripCardAdapter;
import com.example.weathertrip_sep490.model.Trip;
import com.example.weathertrip_sep490.model.TripStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripManageActivity extends AppCompatActivity {

    private final List<Trip> allTrips = new ArrayList<>();
    private EditText etSearch;
    private TextView tvEmpty;
    private TripCardAdapter tripAdapter;
    private FilterChipAdapter chipAdapter;

    private String searchQuery = "";
    private int filterTabIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_manage);

        etSearch = findViewById(R.id.etSearchTrip);
        tvEmpty = findViewById(R.id.tvTripEmpty);
        RecyclerView rvTrips = findViewById(R.id.rvTrips);
        RecyclerView rvChips = findViewById(R.id.rvTripStatusChips);

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

        rvChips.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<String> chipLabels = buildChipLabels();
        chipAdapter = new FilterChipAdapter(chipLabels, (position, value) -> {
            filterTabIndex = position;
            applyFiltersAndRefresh();
        });
        rvChips.setAdapter(chipAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s != null ? s.toString() : "";
                chipAdapter.setItems(buildChipLabels());
                applyFiltersAndRefresh();
            }
        });

        findViewById(R.id.btnCreateTrip).setOnClickListener(v ->
                Toast.makeText(this, "Tạo chuyến đi", Toast.LENGTH_SHORT).show());

        setupBottomNav();
        applyFiltersAndRefresh();
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
}
