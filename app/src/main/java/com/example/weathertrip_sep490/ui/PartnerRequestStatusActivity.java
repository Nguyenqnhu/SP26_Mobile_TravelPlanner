package com.example.weathertrip_sep490.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.PartnerRequestResponse;
import com.example.weathertrip_sep490.util.AppToast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PartnerRequestStatusActivity extends AppCompatActivity {

    private TextView tvStatus;
    private TextView tvDescription;
    private View viewDotStep1;
    private View viewDotStep2;
    private View viewDotStep3;
    private TextView tvStep3Title;
    private TextView tvStep3Description;
    private UserAPI userAPI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_request_status);

        userAPI = RetrofitClient.getInstance().getUserAPI();

        ImageButton btnBack = findViewById(R.id.btnBack);
        tvStatus = findViewById(R.id.tvCurrentStatus);
        tvDescription = findViewById(R.id.tvStatusDescription);
        viewDotStep1 = findViewById(R.id.viewDotStep1);
        viewDotStep2 = findViewById(R.id.viewDotStep2);
        viewDotStep3 = findViewById(R.id.viewDotStep3);
        tvStep3Title = findViewById(R.id.tvStep3Title);
        tvStep3Description = findViewById(R.id.tvStep3Description);

        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnRefreshStatus).setOnClickListener(v -> fetchPartnerRequestStatus(true));

        fetchPartnerRequestStatus(false);
    }

    private void fetchPartnerRequestStatus(boolean isManualRefresh) {
        if (isManualRefresh) {
            AppToast.showSuccess(this, "Đang làm mới trạng thái...");
        }

        userAPI.getMyPartnerRequestStatus().enqueue(new Callback<PartnerRequestResponse>() {
            @Override
            public void onResponse(Call<PartnerRequestResponse> call, Response<PartnerRequestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PartnerRequestResponse request = response.body();
                    if (request.getId() == null) {
                        // Backend returned a message response indicating no request has been submitted
                        showNoRequestUi();
                    } else {
                        updateUiWithRequest(request);
                    }
                } else {
                    // Try to handle case where backend might return empty string or error if no request exists
                    showNoRequestUi();
                }
            }

            @Override
            public void onFailure(Call<PartnerRequestResponse> call, Throwable t) {
                AppToast.showError(PartnerRequestStatusActivity.this, "Không thể kết nối máy chủ");
            }
        });
    }

    private void showNoRequestUi() {
        if (tvStatus != null) {
            tvStatus.setText("Chưa đăng ký");
            tvStatus.setBackgroundResource(R.drawable.bg_status_badge_pending);
            tvStatus.setTextColor(0xFF92400E);
        }
        if (tvDescription != null) {
            tvDescription.setText("Bạn chưa gửi bất kỳ đơn đăng ký đối tác nào.");
        }
        if (viewDotStep1 != null) viewDotStep1.setBackgroundResource(R.drawable.bg_status_step_dot_waiting);
        if (viewDotStep2 != null) viewDotStep2.setBackgroundResource(R.drawable.bg_status_step_dot_waiting);
        if (viewDotStep3 != null) viewDotStep3.setBackgroundResource(R.drawable.bg_status_step_dot_waiting);
        if (tvStep3Title != null) tvStep3Title.setText("Hoàn tất / Từ chối");
        if (tvStep3Description != null) tvStep3Description.setText("Kết quả cuối cùng sẽ hiển thị ở đây sau khi xét duyệt xong");
    }

    private void updateUiWithRequest(PartnerRequestResponse request) {
        String status = request.getStatus();
        if (status == null) status = "";
        status = status.trim();

        // Check if string matches "Approved" or enum code (e.g. "1")
        if (status.equalsIgnoreCase("Approved") || status.equals("1")) {
            if (tvStatus != null) {
                tvStatus.setText("Đã duyệt");
                tvStatus.setBackgroundResource(R.drawable.bg_status_badge_approved);
                tvStatus.setTextColor(0xFF065F46); // Emerald 800
            }
            if (tvDescription != null) {
                tvDescription.setText("Chúc mừng! Đơn đăng ký đối tác của bạn đã được phê duyệt thành công. Bạn đã trở thành Đối tác của TravelGo.");
            }
            if (viewDotStep1 != null) viewDotStep1.setBackgroundResource(R.drawable.bg_status_step_dot_done);
            if (viewDotStep2 != null) viewDotStep2.setBackgroundResource(R.drawable.bg_status_step_dot_done);
            if (viewDotStep3 != null) viewDotStep3.setBackgroundResource(R.drawable.bg_status_step_dot_done);
            if (tvStep3Title != null) {
                tvStep3Title.setText("Đã phê duyệt");
                tvStep3Title.setTextColor(0xFF065F46);
            }
            if (tvStep3Description != null) tvStep3Description.setText("Bạn đã trở thành đối tác chính thức");

        } else if (status.equalsIgnoreCase("Rejected") || status.equals("2")) {
            if (tvStatus != null) {
                tvStatus.setText("Bị từ chối");
                tvStatus.setBackgroundResource(R.drawable.bg_status_badge_rejected);
                tvStatus.setTextColor(0xFF991B1B); // Red 800
            }
            if (tvDescription != null) {
                tvDescription.setText("Rất tiếc, đơn đăng ký đối tác của bạn đã bị từ chối. Vui lòng kiểm tra lại thông tin và thử lại.");
            }
            if (viewDotStep1 != null) viewDotStep1.setBackgroundResource(R.drawable.bg_status_step_dot_done);
            if (viewDotStep2 != null) viewDotStep2.setBackgroundResource(R.drawable.bg_status_step_dot_done);
            if (viewDotStep3 != null) viewDotStep3.setBackgroundResource(R.drawable.bg_status_step_dot_rejected);
            if (tvStep3Title != null) {
                tvStep3Title.setText("Bị từ chối");
                tvStep3Title.setTextColor(0xFF991B1B);
            }
            if (tvStep3Description != null) tvStep3Description.setText("Yêu cầu đăng ký bị từ chối");

        } else {
            // Default to Pending ("Pending" or "0")
            if (tvStatus != null) {
                tvStatus.setText("Đang chờ duyệt");
                tvStatus.setBackgroundResource(R.drawable.bg_status_badge_pending);
                tvStatus.setTextColor(0xFF92400E); // Amber 800
            }
            if (tvDescription != null) {
                tvDescription.setText("Hồ sơ đã được gửi thành công và đang chờ bộ phận kiểm duyệt xem xét.");
            }
            if (viewDotStep1 != null) viewDotStep1.setBackgroundResource(R.drawable.bg_status_step_dot_done);
            if (viewDotStep2 != null) viewDotStep2.setBackgroundResource(R.drawable.bg_status_step_dot_waiting);
            if (viewDotStep3 != null) viewDotStep3.setBackgroundResource(R.drawable.bg_status_step_dot_waiting);
            if (tvStep3Title != null) {
                tvStep3Title.setText("Chờ phê duyệt");
                tvStep3Title.setTextColor(0xFF0F172A);
            }
            if (tvStep3Description != null) tvStep3Description.setText("Yêu cầu của bạn đang chờ phê duyệt");
        }
    }
}
