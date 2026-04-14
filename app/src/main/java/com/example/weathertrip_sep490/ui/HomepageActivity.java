package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.HeroBannerAdapter;
import com.example.weathertrip_sep490.adapter.PartnerAdapter;
import com.example.weathertrip_sep490.adapter.PoiGridAdapter;
import com.example.weathertrip_sep490.adapter.PoiRecentAdapter;
import com.example.weathertrip_sep490.data.RecentPoiStorage;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.POI;
import com.example.weathertrip_sep490.model.Partner;
import com.example.weathertrip_sep490.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomepageActivity extends AppCompatActivity {

    private TextView tvUserNameHome;
    private EditText etSearchHome;
    private TextView tvFeaturedEmpty;
    private NestedScrollView nestedHomeScroll;
    private PoiGridAdapter popularAdapter;
    private PoiRecentAdapter recentAdapter;
    private final List<POI> popularAll = new ArrayList<>();
    private final List<POI> recentAll = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        tvUserNameHome = findViewById(R.id.tvUserNameHome);
        etSearchHome = findViewById(R.id.etSearchHome);
        tvFeaturedEmpty = findViewById(R.id.tvFeaturedEmpty);
        nestedHomeScroll = findViewById(R.id.nestedHomeScroll);

        loadUserNameFromApi();
        setupHeaderAvatar();
        setupHeroBanner();
        setupHeroAndSectionScroll();
        setupSeeAllLinks();
        setupActionButtons();
        setupBottomNav();
        setupPopularDestinations();
        setupRecentlyViewed();
        setupPartners();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshRecentFromLocal();
    }

    private void setupHeroBanner() {
        ViewPager2 vp = findViewById(R.id.vpHeroBanner);
        if (vp == null) return;

        View dot0 = findViewById(R.id.dotHero0);
        View dot1 = findViewById(R.id.dotHero1);
        int[] slides = new int[]{R.drawable.sampleplace3, R.drawable.sampleplace};
        vp.setAdapter(new HeroBannerAdapter(slides));

        ViewPager2.OnPageChangeCallback cb = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (dot0 != null) {
                    dot0.setBackgroundResource(
                            position == 0
                                    ? R.drawable.bg_carousel_dot_selected
                                    : R.drawable.bg_carousel_dot_unselected);
                }
                if (dot1 != null) {
                    dot1.setBackgroundResource(
                            position == 1
                                    ? R.drawable.bg_carousel_dot_selected
                                    : R.drawable.bg_carousel_dot_unselected);
                }
            }
        };
        vp.registerOnPageChangeCallback(cb);
        cb.onPageSelected(0);
    }

    private void setupHeroAndSectionScroll() {
        View btnHeroAbout = findViewById(R.id.btnHeroAbout);
        if (btnHeroAbout != null) {
            btnHeroAbout.setOnClickListener(v -> scrollToSection(R.id.sectionAbout));
        }
        View btnHeroExplore = findViewById(R.id.btnHeroExplore);
        if (btnHeroExplore != null) {
            btnHeroExplore.setOnClickListener(v ->
                    startActivity(new Intent(this, ExploreActivity.class)));
        }
        View btnAboutFeatured = findViewById(R.id.btnAboutFeatured);
        if (btnAboutFeatured != null) {
            btnAboutFeatured.setOnClickListener(v -> scrollToSection(R.id.sectionFeaturedDestinations));
        }
        View btnAboutLearnMore = findViewById(R.id.btnAboutLearnMore);
        if (btnAboutLearnMore != null) {
            btnAboutLearnMore.setOnClickListener(v -> scrollToSection(R.id.sectionWhyUs));
        }
    }

    private void scrollToSection(int viewId) {
        View section = findViewById(viewId);
        if (section == null || nestedHomeScroll == null) return;
        nestedHomeScroll.post(() -> {
            int y = 0;
            View v = section;
            while (v != null && v != nestedHomeScroll) {
                y += v.getTop();
                ViewParent vp = v.getParent();
                v = vp instanceof View ? (View) vp : null;
            }
            nestedHomeScroll.smoothScrollTo(0, Math.max(0, y - 24));
        });
    }

    private void setupSeeAllLinks() {
        View.OnClickListener openExplore = v ->
                startActivity(new Intent(this, ExploreActivity.class));
        TextView tvPopular = findViewById(R.id.tvPopularSeeAll);
        if (tvPopular != null) tvPopular.setOnClickListener(openExplore);
        TextView tvRecent = findViewById(R.id.tvRecentSeeAll);
        if (tvRecent != null) tvRecent.setOnClickListener(openExplore);
        TextView tvPartner = findViewById(R.id.tvPartnerSeeAll);
        if (tvPartner != null) tvPartner.setOnClickListener(openExplore);
    }

    private void setupActionButtons() {
        View btnCreateTrip = findViewById(R.id.btnActionCreateTrip);
        View btnWeather = findViewById(R.id.btnActionWeather);
        View btnExplore = findViewById(R.id.btnActionExplore);

        if (btnCreateTrip != null) {
            btnCreateTrip.setOnClickListener(v ->
                    startActivity(new Intent(this, TripManageActivity.class)));
        }
        if (btnWeather != null) {
            btnWeather.setOnClickListener(v -> new WeatherBottomSheet().show(getSupportFragmentManager(), "WeatherBottomSheet"));
        }
        if (btnExplore != null) {
            btnExplore.setOnClickListener(v ->
                    startActivity(new Intent(this, ExploreActivity.class)));
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
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            }
            if (id == R.id.nav_user) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_explore) {
                startActivity(new Intent(this, ExploreActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_trip) {
                startActivity(new Intent(this, TripManageActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupPopularDestinations() {
        RecyclerView rv = findViewById(R.id.rvPopularDestinations);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setNestedScrollingEnabled(false);

        popularAdapter = new PoiGridAdapter(this::openPoiDetail);
        rv.setAdapter(popularAdapter);
        loadPopularFromApi();
    }

    private void setupRecentlyViewed() {
        RecyclerView rv = findViewById(R.id.rvRecentlyViewed);
        rv.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        recentAdapter = new PoiRecentAdapter(this::openPoiDetail);
        rv.setAdapter(recentAdapter);
        refreshRecentFromLocal();
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
                filterPois(q);
            }
        });
    }

    private void updateFeaturedEmpty(List<POI> filteredPopular) {
        if (tvFeaturedEmpty == null) return;
        boolean empty = filteredPopular == null || filteredPopular.isEmpty();
        tvFeaturedEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void filterPois(String query) {
        List<POI> filteredPopular = new ArrayList<>();
        List<POI> filteredRecent = new ArrayList<>();

        if (query.isEmpty()) {
            filteredPopular.addAll(popularAll);
            filteredRecent.addAll(recentAll);
        } else {
            for (POI p : popularAll) {
                if (matchesQuery(p, query)) filteredPopular.add(p);
            }
            for (POI p : recentAll) {
                if (matchesQuery(p, query)) filteredRecent.add(p);
            }
        }
        if (popularAdapter != null) {
            popularAdapter.updateData(filteredPopular);
        }
        if (recentAdapter != null) {
            recentAdapter.updateData(filteredRecent);
        }
        updateFeaturedEmpty(filteredPopular);
    }

    private boolean matchesQuery(POI p, String q) {
        if (p == null) return false;
        return (p.getName() != null && p.getName().toLowerCase().contains(q))
                || (p.getCity() != null && p.getCity().toLowerCase().contains(q));
    }

    private void loadPopularFromApi() {
        UserAPI api = RetrofitClient.getInstance().getPOIAPI();
        api.getRecommendedPOIs("vi").enqueue(new Callback<List<POI>>() {
            @Override
            public void onResponse(Call<List<POI>> call, Response<List<POI>> response) {
                popularAll.clear();
                if (response.isSuccessful() && response.body() != null) {
                    List<POI> all = response.body();
                    if (all != null) {
                        int limit = Math.min(4, all.size());
                        for (int i = 0; i < limit; i++) {
                            POI p = all.get(i);
                            if (p != null) popularAll.add(p);
                        }
                    }
                }
                if (popularAdapter != null) {
                    popularAdapter.updateData(new ArrayList<>(popularAll));
                }
                filterPois(safeLower(etSearchHome != null ? etSearchHome.getText() : null));
            }

            @Override
            public void onFailure(Call<List<POI>> call, Throwable t) {
                popularAll.clear();
                if (popularAdapter != null) {
                    popularAdapter.updateData(new ArrayList<>());
                }
                filterPois(safeLower(etSearchHome != null ? etSearchHome.getText() : null));
            }
        });
    }

    private void refreshRecentFromLocal() {
        List<POI> stored = RecentPoiStorage.getRecent(this);
        recentAll.clear();
        if (stored != null) recentAll.addAll(stored);
        if (recentAdapter != null) {
            recentAdapter.updateData(new ArrayList<>(recentAll));
        }
        filterPois(safeLower(etSearchHome != null ? etSearchHome.getText() : null));
    }

    private void openPoiDetail(POI poi) {
        if (poi == null || poi.getId() == null || poi.getId().trim().isEmpty()) return;
        Intent intent = new Intent(this, ExplorePOIDetailActivity.class);
        intent.putExtra(ExplorePOIDetailActivity.EXTRA_POI_ID, poi.getId().trim());
        startActivity(intent);
    }

    private String safeLower(CharSequence s) {
        String raw = s == null ? "" : s.toString();
        return raw.trim().toLowerCase();
    }
}
