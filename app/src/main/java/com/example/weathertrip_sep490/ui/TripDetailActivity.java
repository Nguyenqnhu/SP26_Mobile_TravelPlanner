package com.example.weathertrip_sep490.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.TimelineEventAdapter;
import com.example.weathertrip_sep490.adapter.TripDayBandAdapter;
import com.example.weathertrip_sep490.model.TimelineEvent;

import java.util.ArrayList;
import java.util.List;

public class TripDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CITY = "extra_city";
    public static final String EXTRA_DATES = "extra_dates";

    private final List<TimelineEvent> events = new ArrayList<>();
    private TimelineEventAdapter timelineAdapter;
    private TripDayBandAdapter dayBandAdapter;
    private RecyclerView rvDays;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        String city = getIntent().getStringExtra(EXTRA_CITY);
        String dates = getIntent().getStringExtra(EXTRA_DATES);

        ImageView btnBack = findViewById(R.id.btnBackTripDetail);
        TextView tvTitle = findViewById(R.id.tvTripDetailTitle);
        TextView tvCity = findViewById(R.id.tvTripDetailCity);
        TextView tvDates = findViewById(R.id.tvTripDetailDates);

        tvTitle.setText("Lịch trình chi tiết");
        tvCity.setText(city != null ? city : "Chuyến đi");
        tvDates.setText(dates != null ? dates : "");

        btnBack.setOnClickListener(v -> finish());

        setupDayChips();
        setupTimeline();
        seedSampleTimeline(0);
    }

    private void setupDayChips() {
        rvDays = findViewById(R.id.rvTripDetailDays);
        rvDays.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<String> days = new ArrayList<>();
        // Mock nhiều ngày để "băng" có thể trượt như ảnh 2
        days.add("15.01");
        days.add("16.01");
        days.add("17.01");
        days.add("18.01");
        days.add("19.01");
        days.add("20.01");
        days.add("21.01");

        dayBandAdapter = new TripDayBandAdapter(days, (pos, value) -> {
            // Click: focus item và đổi data theo ngày
            rvDays.smoothScrollToPosition(pos);
            seedSampleTimeline(pos);
        });
        rvDays.setAdapter(dayBandAdapter);

        // Snap/center giống "băng" chọn ngày (pill)
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
                if (dayBandAdapter.getSelectedPosition() != pos) {
                    dayBandAdapter.setSelectedPosition(pos);
                    seedSampleTimeline(pos);
                }
            }
        });
    }

    private void setupTimeline() {
        RecyclerView rv = findViewById(R.id.rvTripTimeline);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        timelineAdapter = new TimelineEventAdapter();
        rv.setAdapter(timelineAdapter);
    }

    private void seedSampleTimeline(int dayIndex) {
        int p1 = R.drawable.sampleplace3;
        int p2 = R.drawable.sampleplace3;
        int p3 = R.drawable.sampleplace3;

        events.clear();
        if (dayIndex % 3 == 0) {
            events.add(new TimelineEvent("14:00", "Hồ Hoàn Kiếm", "Đà Lạt · Hoàn Kiếm · Food", p1, "22°C", "Nắng nhẹ"));
            events.add(new TimelineEvent("16:00", "Phố cổ Đà Lạt", "Đà Lạt · Hoàn Kiếm · Food", p2, "26°C", "Ít mây"));
            events.add(new TimelineEvent("19:00", "Ăn tối", "Quán đặc sản địa phương", p3, "24°C", "Mát"));
        } else if (dayIndex % 3 == 1) {
            events.add(new TimelineEvent("08:30", "Cà phê sáng", "Quán view hồ", p2, "21°C", "Mát"));
            events.add(new TimelineEvent("10:30", "Tham quan", "Chợ địa phương", p3, "24°C", "Nắng"));
            events.add(new TimelineEvent("15:00", "Check-in", "Điểm chụp ảnh", p1, "25°C", "Nắng nhẹ"));
        } else {
            events.add(new TimelineEvent("09:00", "Di chuyển", "Taxi tới điểm tham quan", p3, "20°C", "Ít mây"));
            events.add(new TimelineEvent("11:00", "Bảo tàng", "Vé từ 50.000 đ", p1, "23°C", "Nắng"));
            events.add(new TimelineEvent("17:30", "Nghỉ ngơi", "Về khách sạn", p2, "22°C", "Mát"));
        }

        if (timelineAdapter != null) {
            timelineAdapter.updateData(new ArrayList<>(events));
        }
    }
}

