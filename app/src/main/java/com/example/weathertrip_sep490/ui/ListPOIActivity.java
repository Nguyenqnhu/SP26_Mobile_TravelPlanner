package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weathertrip_sep490.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ListPOIActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_poi);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_hourly); // Tô đúng icon Khám phá
            bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.nav_now) {
                        startActivity(new Intent(ListPOIActivity.this, HomepageActivity.class));
                        finish();
                        return true;
                    }
                    if (id == R.id.nav_hourly) {
                        return true; // Đã ở Khám phá
                    }
                    if (id == R.id.nav_fav) {
                        startActivity(new Intent(ListPOIActivity.this, ProfileActivity.class));
                        finish();
                        return true;
                    }
                    if (id == R.id.nav_daily) {
                        return true;
                    }
                    return false;
                }
            });
        }
    }

}
