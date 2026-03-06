package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.AuthAPI;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.model.LoginRequest;
import com.example.weathertrip_sep490.model.LoginResponse;
import com.example.weathertrip_sep490.util.ViewAnimationUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private SwitchCompat swRemember;
    private AppCompatButton btnLogin;
    private TextView tvGoToRegister;
    private TextView tvForgotPassword;
    private ImageView ivTogglePassword;
    private AuthAPI authAPI;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        ViewAnimationUtil.setTouchScaleAnimation(btnLogin);
        ViewAnimationUtil.setTouchScaleAnimation(tvGoToRegister);
        ViewAnimationUtil.setTouchScaleAnimation(tvForgotPassword);
        setupPasswordToggle(etPassword, ivTogglePassword);

        authAPI = RetrofitClient.getInstance().getAuthAPI();
        sharedPreferences = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);

        if (sharedPreferences.getBoolean("remember_me", false)) {
            etEmail.setText(sharedPreferences.getString("saved_email", ""));
            etPassword.setText(sharedPreferences.getString("saved_password", ""));
            swRemember.setChecked(true);
        }

        btnLogin.setOnClickListener(v -> performLogin());
        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        swRemember = findViewById(R.id.swRemember);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
    }

    private void setupPasswordToggle(EditText editText, ImageView imageView) {
        if (imageView == null) return;
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

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return;
        }

        setLoading(true);
        LoginRequest request = new LoginRequest(email, password);
        Call<LoginResponse> call = authAPI.login(request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String token = loginResponse.getAccessToken();
                    if (token == null || token.trim().isEmpty()) {
                        android.util.Log.w("Login", "Backend không trả accessToken. Kiểm tra API login trả về field accessToken/AccessToken.");
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("access_token", token != null ? token.trim() : "");
                    editor.putString("refresh_token", loginResponse.getRefreshToken() != null ? loginResponse.getRefreshToken().trim() : "");
                    if (swRemember.isChecked()) {
                        editor.putBoolean("remember_me", true);
                        editor.putString("saved_email", email);
                        editor.putString("saved_password", password);
                    } else {
                        editor.putBoolean("remember_me", false);
                        editor.remove("saved_email");
                        editor.remove("saved_password");
                    }
                    editor.commit();
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, PreferencesActivity.class);
                    if (token != null && !token.isEmpty()) {
                        intent.putExtra("access_token", token);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Đang đăng nhập..." : "Đăng nhập");
        btnLogin.setAlpha(loading ? 0.7f : 1f);
    }
}
