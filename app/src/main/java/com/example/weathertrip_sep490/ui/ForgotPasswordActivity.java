package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.AuthAPI;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.util.AppToast;
import com.example.weathertrip_sep490.model.ForgotPasswordRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private Button btnSendOtp;
    private LinearLayout llBack;
    private AuthAPI authAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmailForgot);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        llBack = findViewById(R.id.llBack);
        authAPI = RetrofitClient.getInstance().getAuthAPI();

        llBack.setOnClickListener(v -> finish());

        btnSendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                etEmail.setError("Vui lòng nhập email");
                return;
            }
            sendOtpRequest(email);
        });
    }

    private void sendOtpRequest(String email) {
        btnSendOtp.setEnabled(false);
        authAPI.requestPasswordReset(new ForgotPasswordRequest(email)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnSendOtp.setEnabled(true);
                if (response.isSuccessful()) {
                    AppToast.showSuccess(ForgotPasswordActivity.this, "Mã OTP đã gửi đến email của bạn");
                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("isResetPassword", true);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = "Email không tồn tại hoặc có lỗi. Vui lòng thử lại.";
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            if (body != null && !body.isEmpty()) {
                                // Nếu backend trả về JSON {"message":"..."} có thể parse đơn giản
                                if (body.contains("\"message\"")) {
                                    int start = body.indexOf("\"message\"") + 11;
                                    int end = body.indexOf("\"", start);
                                    if (end > start) msg = body.substring(start, end);
                                    else msg = body;
                                } else {
                                    msg = body.length() > 80 ? body.substring(0, 80) + "…" : body;
                                }
                            }
                        }
                    } catch (Exception e) { /* ignore */ }
                    AppToast.showError(ForgotPasswordActivity.this, msg);
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSendOtp.setEnabled(true);
                AppToast.showError(ForgotPasswordActivity.this, "Lỗi kết nối");
            }
        });
    }
}