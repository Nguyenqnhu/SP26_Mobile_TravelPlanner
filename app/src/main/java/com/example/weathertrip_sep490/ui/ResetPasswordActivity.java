package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.AuthAPI;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.model.ResetPasswordRequest;
import com.example.weathertrip_sep490.util.AppToast;
import com.example.weathertrip_sep490.util.ViewAnimationUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etNewPass, etConfirmPass;
    private Button btnReset;
    private ImageView ivToggleNewPass, ivToggleConfirmPass;
    private LinearLayout llBack;
    private String resetToken;
    private AuthAPI authAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        resetToken = getIntent().getStringExtra("resetToken");
        if (resetToken == null || resetToken.isEmpty()) {
            AppToast.showError(this, "Thiếu thông tin xác thực. Vui lòng thử lại từ bước quên mật khẩu.");
            finish();
            return;
        }

        authAPI = RetrofitClient.getInstance().getAuthAPI();
        etNewPass = findViewById(R.id.etNewPassword);
        etConfirmPass = findViewById(R.id.etConfirmPassword);
        btnReset = findViewById(R.id.btnResetPassword);
        ivToggleNewPass = findViewById(R.id.ivToggleNewPass);
        ivToggleConfirmPass = findViewById(R.id.ivToggleConfirmPass);
        llBack = findViewById(R.id.llBack);

        ViewAnimationUtil.setTouchScaleAnimation(btnReset);
        llBack.setOnClickListener(v -> finish());

        setupPasswordToggle(etNewPass, ivToggleNewPass);
        setupPasswordToggle(etConfirmPass, ivToggleConfirmPass);

        btnReset.setOnClickListener(v -> {
            String pass = etNewPass.getText().toString().trim();
            String confirm = etConfirmPass.getText().toString().trim();
            if (pass.length() < 6) {
                etNewPass.setError("Mật khẩu ít nhất 6 ký tự");
                return;
            }
            if (!pass.equals(confirm)) {
                etConfirmPass.setError("Mật khẩu không khớp");
                return;
            }
            performReset(pass);
        });
    }

    private void setupPasswordToggle(EditText editText, ImageView imageView) {
        imageView.setOnClickListener(v -> {
            int type = editText.getInputType();
            if ((type & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imageView.setImageResource(android.R.drawable.ic_lock_lock);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imageView.setImageResource(android.R.drawable.ic_menu_view);
            }
        });
    }

    private void performReset(String newPass) {
        btnReset.setEnabled(false);
        btnReset.setText("Đang cập nhật...");
        authAPI.resetPassword(new ResetPasswordRequest(resetToken, newPass)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnReset.setEnabled(true);
                btnReset.setText("Cập nhật mật khẩu");
                if (response.isSuccessful()) {
                    AppToast.showSuccess(ResetPasswordActivity.this, "Cập nhật mật khẩu thành công! Vui lòng đăng nhập lại.");
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    AppToast.showError(ResetPasswordActivity.this, "Cập nhật thất bại. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnReset.setEnabled(true);
                btnReset.setText("Cập nhật mật khẩu");
                AppToast.showError(ResetPasswordActivity.this, "Lỗi: " + t.getMessage());
            }
        });
    }
}
