package com.example.weathertrip_sep490.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.AuthAPI;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.model.RegistRequest;
import com.example.weathertrip_sep490.util.AppToast;
import com.example.weathertrip_sep490.util.ViewAnimationUtil;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {


    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etDateOfBirth;
    private EditText etPhone;
    private EditText etAddress;
    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ImageView ivTogglePassword;
    private AuthAPI authAPI;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        ViewAnimationUtil.setTouchScaleAnimation(btnRegister);
        ViewAnimationUtil.setTouchScaleAnimation(tvLoginLink);
        setupPasswordToggle(etPassword, ivTogglePassword);

        authAPI = RetrofitClient.getInstance().getAuthAPI();
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Set timezone UTC để đảm bảo format đúng với backend
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        etDateOfBirth.setOnClickListener(v -> showDatePicker());
        btnRegister.setOnClickListener(v -> performRegister());
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
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

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    etDateOfBirth.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void performRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String dateOfBirthStr = etDateOfBirth.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Vui lòng nhập họ tên");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (TextUtils.isEmpty(dateOfBirthStr)) {
            etDateOfBirth.setError("Vui lòng chọn ngày sinh");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            return;
        }

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Vui lòng nhập địa chỉ");
            return;
        }


        String gender = rbMale.isChecked() ? "Male" : "Female";

        // Parse date và set time thành 00:00:00 UTC để format đúng ISO 8601
        Date dateOfBirth;
        try {
            dateOfBirth = dateFormat.parse(dateOfBirthStr);
            // Set time thành 00:00:00 UTC
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTime(dateOfBirth);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            dateOfBirth = cal.getTime();
        } catch (Exception e) {
            etDateOfBirth.setError("Ngày sinh không hợp lệ");
            Log.e("RegisterActivity", "Date parse error: " + e.getMessage());
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Đang đăng ký...");

        // Map đúng theo JSON backend yêu cầu:
        //  email, password, dateOfBirth, name, address, phoneNumber, gender
        RegistRequest registRequest = new RegistRequest(
                email,
                password,
                dateOfBirth,
                name,
                address,
                phone,
                gender
        );

        // Log để debug
        Log.d("RegisterActivity", "Register request - Email: " + email + ", Name: " + name + ", Gender: " + gender);
        Log.d("RegisterActivity", "DateOfBirth: " + dateOfBirthStr + " -> " + dateOfBirth);

        Call<Void> call = authAPI.register(registRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");

                if (response.isSuccessful()) {
                    AppToast.showSuccess(RegisterActivity.this, "Đăng ký thành công! Vui lòng xác thực OTP");

                    // Navigate to OTP verification screen
                    Intent intent = new Intent(RegisterActivity.this, VerifyOtpActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e("RegisterActivity", "Error response: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e("RegisterActivity", "Error reading error body: " + e.getMessage());
                    }


                    String errorMessage;
                    if (!errorBody.isEmpty()) {
                        errorMessage = "Đăng ký thất bại: " + errorBody;
                    } else {
                        errorMessage = "Đăng ký thất bại (mã " + response.code() + ")";
                    }
                    AppToast.showError(RegisterActivity.this, errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");
                AppToast.showError(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
