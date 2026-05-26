package com.example.weathertrip_sep490.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weathertrip_sep490.R;

public class PartnerRequestStatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_request_status);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvStatus = findViewById(R.id.tvCurrentStatus);
        TextView tvDescription = findViewById(R.id.tvStatusDescription);

        btnBack.setOnClickListener(v -> finish());

        // UI demo state trước khi gắn API
        if (tvStatus != null) {
            tvStatus.setText("Đang chờ duyệt");
            tvStatus.setBackgroundResource(R.drawable.bg_status_badge_pending);
            tvStatus.setTextColor(0xFF92400E);
        }
        if (tvDescription != null) {
            tvDescription.setText("Hồ sơ đã được gửi thành công và đang chờ bộ phận kiểm duyệt xem xét.");
        }

        findViewById(R.id.btnRefreshStatus).setOnClickListener(v -> {
            if (tvDescription != null) {
                tvDescription.setText("Đây là giao diện xem trạng thái đơn. Mình sẽ gắn API GET /api/partner-requests/my-status ở bước tiếp theo.");
            }
        });
    }
}
