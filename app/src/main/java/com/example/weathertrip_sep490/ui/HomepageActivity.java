package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.DestinationAdapter;
import com.example.weathertrip_sep490.adapter.DestinationGridAdapter;
import com.example.weathertrip_sep490.adapter.PartnerAdapter;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.Destination;
import com.example.weathertrip_sep490.model.Partner;
import com.example.weathertrip_sep490.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomepageActivity extends AppCompatActivity {

    private TextView tvUserNameHome;
    private TextView tvWeatherCity;
    private TextView tvWeatherTemp;
    private TextView tvWeatherDesc;
    private EditText etSearchHome;
    private DestinationGridAdapter popularAdapter;
    private DestinationAdapter recentAdapter;
    private final List<Destination> popularAll = new ArrayList<>();
    private final List<Destination> recentAll = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        tvUserNameHome = findViewById(R.id.tvUserNameHome);
        etSearchHome = findViewById(R.id.etSearchHome);

        loadUserNameFromApi();
        setupHeaderAvatar();
        setupActionButtons();
        setupBottomNav();
        setupPopularDestinations();
        setupRecentlyViewed();
        setupPartners();
        setupSearch();
    }

    private void setupActionButtons() {
        View btnCreateTrip = findViewById(R.id.btnActionCreateTrip);
        View btnWeather = findViewById(R.id.btnActionWeather);
        View btnExplore = findViewById(R.id.btnActionExplore);

        if (btnCreateTrip != null) {
            btnCreateTrip.setOnClickListener(v -> {
                Toast.makeText(this, "Tạo chuyến đi", Toast.LENGTH_SHORT).show();
                // TODO: mở màn tạo lịch trình
            });
        }
        if (btnWeather != null) {
            btnWeather.setOnClickListener(v -> Toast.makeText(this, "Xem thời tiết", Toast.LENGTH_SHORT).show());
        }
        if (btnExplore != null) {
            btnExplore.setOnClickListener(v -> {
                Toast.makeText(this, "Khám phá", Toast.LENGTH_SHORT).show();
                // Có thể mở ListPOIActivity (Khám phá) hoặc cuộn tới Khám phá điểm đến
            });
        }
    }

    private void loadUserNameFromApi() {
        SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
        String userId = prefs.getString("current_user_id", null);
        if (userId == null || userId.trim().isEmpty()) {
            tvUserNameHome.setText("Xin chào, bạn!");
            return;
        }
        UserAPI api = RetrofitClient.getInstance().getPreferenceAPI();
        api.getUserById(userId.trim()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String name = response.body().getName();
                    if (name == null || name.trim().isEmpty()) {
                        name = "bạn";
                    }
                    tvUserNameHome.setText("Xin chào, " + name);
                } else {
                    tvUserNameHome.setText("Xin chào, bạn!");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                tvUserNameHome.setText("Xin chào, bạn!");
            }
        });
    }

    private void setupHeaderAvatar() {
        View btnAvatar = findViewById(R.id.btnAvatarHome);
        if (btnAvatar != null) {
            btnAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
    }

    private void setupBottomNav() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_home);
        if (bottomNav == null) return;
        bottomNav.setSelectedItemId(R.id.nav_now); // Tô đúng icon Trang chủ
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_now) {
                return true; // Đã ở trang chủ
            }
            if (id == R.id.nav_fav) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_hourly) {
                startActivity(new Intent(this, ListPOIActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_daily) {
                Toast.makeText(this, "Lịch trình", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }


    private void setupPopularDestinations() {
        RecyclerView rv = findViewById(R.id.rvPopularDestinations);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setNestedScrollingEnabled(false);

        popularAll.clear();
        popularAll.add(new Destination("Vịnh Hạ Long", "Quảng Ninh", 4.8, 1200, R.drawable.sampleplace));
        popularAll.add(new Destination("Phố cổ Hội An", "Quảng Nam", 4.9, 980, R.drawable.sampleplace));
        popularAll.add(new Destination("Sa Pa", "Lào Cai", 4.7, 860, R.drawable.sampleplace));
        popularAll.add(new Destination("Đà Lạt", "Lâm Đồng", 4.8, 1100, R.drawable.sampleplace));

        popularAdapter = new DestinationGridAdapter(new ArrayList<>(popularAll));
        rv.setAdapter(popularAdapter);
    }

    private void setupRecentlyViewed() {
        RecyclerView rv = findViewById(R.id.rvRecentlyViewed);
        rv.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Dùng lại Destination làm mock "đã xem gần đây"
        recentAll.clear();
        recentAll.add(new Destination("Hồ Hoàn Kiếm", "Hà Nội", 4.6, 540, R.drawable.sampleplace));
        recentAll.add(new Destination("Núi Phú Sĩ", "Nhật Bản", 4.9, 1350, R.drawable.sampleplace));

        recentAdapter = new DestinationAdapter(new ArrayList<>(recentAll));
        rv.setAdapter(recentAdapter);
    }

    private void setupPartners() {
        RecyclerView rv = findViewById(R.id.rvPartners);
        rv.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Partner> list = new ArrayList<>();
        list.add(new Partner("Vietravel", "Tour du lịch chất lượng cao", "30%", R.drawable.sampleplace));
        list.add(new Partner("VietJet Air", "Ưu đãi giá vé máy bay", "20%", R.drawable.sampleplace));
        list.add(new Partner("Booking.com", "Đặt phòng hoàn tiền 10%", "10%", R.drawable.sampleplace));
        list.add(new Partner("Grab", "Giảm 30k cho chuyến đầu", "30K", R.drawable.sampleplace));

        rv.setAdapter(new PartnerAdapter(list));
    }

    private void setupSearch() {
        etSearchHome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String q = (s != null ? s.toString() : "").trim().toLowerCase();
                filterDestinations(q);
            }
        });
    }

    private void filterDestinations(String query) {
        List<Destination> filteredPopular = new ArrayList<>();
        List<Destination> filteredRecent = new ArrayList<>();

        if (query.isEmpty()) {
            filteredPopular.addAll(popularAll);
            filteredRecent.addAll(recentAll);
        } else {
            for (Destination d : popularAll) {
                if (matchesQuery(d, query)) filteredPopular.add(d);
            }
            for (Destination d : recentAll) {
                if (matchesQuery(d, query)) filteredRecent.add(d);
            }
        }
        if (popularAdapter != null) {
            popularAdapter.updateData(filteredPopular);
        }
        if (recentAdapter != null) {
            recentAdapter.updateData(filteredRecent);
        }
    }

    private boolean matchesQuery(Destination d, String q) {
        return (d.getName() != null && d.getName().toLowerCase().contains(q))
                || (d.getCity() != null && d.getCity().toLowerCase().contains(q));
    }
}
