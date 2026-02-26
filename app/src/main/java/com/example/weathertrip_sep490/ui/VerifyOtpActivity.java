package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.AuthAPI;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.model.ForgotPasswordRequest;
import com.example.weathertrip_sep490.model.OtpRequest;
import com.example.weathertrip_sep490.model.VerifyResetPasswordOtpResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {

    private EditText[] otpInputs = new EditText[6];
    private Button btnVerify;
    private TextView tvResendOtp;
    private LinearLayout llBack;
    private AuthAPI authAPI;
    private String email;
    private boolean isResetPassword; // Biến phân biệt luồng
    private CountDownTimer countDownTimer;
    private boolean canResend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Lấy dữ liệu từ Intent
        email = getIntent().getStringExtra("email");
        isResetPassword = getIntent().getBooleanExtra("isResetPassword", false);

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy email", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupOtpLogic();
        authAPI = RetrofitClient.getInstance().getAuthAPI();

        startResendTimer();

        btnVerify.setOnClickListener(v -> performVerify());
        tvResendOtp.setOnClickListener(v -> { if (canResend) resendOtp(); });
        llBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        otpInputs[0] = findViewById(R.id.otp1);
        otpInputs[1] = findViewById(R.id.otp2);
        otpInputs[2] = findViewById(R.id.otp3);
        otpInputs[3] = findViewById(R.id.otp4);
        otpInputs[4] = findViewById(R.id.otp5);
        otpInputs[5] = findViewById(R.id.otp6);

        btnVerify = findViewById(R.id.btnVerify);
        tvResendOtp = findViewById(R.id.tvResend);
        llBack = findViewById(R.id.llBack);
    }

    private void setupOtpLogic() {
        for (int i = 0; i < 6; i++) {
            final int index = i;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && index < 5) {
                        otpInputs[index + 1].requestFocus();
                    }
                }
            });

            otpInputs[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpInputs[index].getText().toString().isEmpty() && index > 0) {
                        otpInputs[index - 1].requestFocus();
                        otpInputs[index - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void performVerify() {
        StringBuilder otpBuilder = new StringBuilder();
        for (EditText et : otpInputs) {
            otpBuilder.append(et.getText().toString().trim());
        }
        String otpCode = otpBuilder.toString();

        if (otpCode.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        if (isResetPassword) {
            OtpRequest request = new OtpRequest(email, otpCode);
            authAPI.verifyResetPasswordOtp(request).enqueue(new Callback<VerifyResetPasswordOtpResponse>() {
                @Override
                public void onResponse(Call<VerifyResetPasswordOtpResponse> call, Response<VerifyResetPasswordOtpResponse> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        String resetToken = response.body().getResetToken();
                        if (resetToken == null || resetToken.isEmpty()) {
                            Toast.makeText(VerifyOtpActivity.this, "Mã xác thực không hợp lệ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(VerifyOtpActivity.this, "Xác thực thành công! Tạo mật khẩu mới.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("resetToken", resetToken);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(VerifyOtpActivity.this, "Mã xác thực không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<VerifyResetPasswordOtpResponse> call, Throwable t) {
                    setLoading(false);
                    Toast.makeText(VerifyOtpActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Xác thực OTP cho luồng ĐĂNG KÝ
            authAPI.verifyRegisterOtp(new OtpRequest(email, otpCode)).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body()) {
                        Toast.makeText(VerifyOtpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(VerifyOtpActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(VerifyOtpActivity.this, "Mã không đúng hoặc hết hạn", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    setLoading(false);
                    Toast.makeText(VerifyOtpActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setLoading(boolean isLoading) {
        btnVerify.setEnabled(!isLoading);
        btnVerify.setText(isLoading ? "Đang xử lý..." : "Xác nhận");
    }

    private void resendOtp() {
        // Lưu ý: Tùy vào isResetPassword mà gọi API gửi lại mã tương ứng
        Call<Void> call = isResetPassword ? authAPI.requestPasswordReset(new ForgotPasswordRequest(email))
                : authAPI.resendRegisterOtp(email);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(VerifyOtpActivity.this, "Đã gửi lại mã mới", Toast.LENGTH_SHORT).show();
                    startResendTimer();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void startResendTimer() {
        canResend = false;
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResendOtp.setText("Gửi lại (" + (millisUntilFinished / 1000) + "s)");
                tvResendOtp.setTextColor(ContextCompat.getColor(VerifyOtpActivity.this, R.color.slate_400));
            }
            @Override
            public void onFinish() {
                tvResendOtp.setText("Gửi lại");
                tvResendOtp.setTextColor(ContextCompat.getColor(VerifyOtpActivity.this, R.color.emerald_600));
                canResend = true;
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}