package com.example.weathertrip_sep490.ui;

import com.example.weathertrip_sep490.util.AppToast;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;

import com.example.weathertrip_sep490.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etPhone;
    private EditText etDob;
    private EditText etAddress;
    private RadioGroup rgGender;
    private UserAPI userAPI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etDob = findViewById(R.id.etDob);
        etAddress = findViewById(R.id.etAddress);
        rgGender = findViewById(R.id.rgGender);

        userAPI = RetrofitClient.getInstance().getPreferenceAPI();

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnSave = findViewById(R.id.btnSave);
        androidx.appcompat.widget.AppCompatButton btnUpdate = findViewById(R.id.btnUpdate);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> submitProfile());
        btnUpdate.setOnClickListener(v -> submitProfile());

        etDob.setOnClickListener(v -> showDatePicker());
        loadCurrentUser();
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (DatePicker view, int y, int m, int d) -> {
                    String formatted = String.format("%02d/%02d/%04d", d, m + 1, y);
                    etDob.setText(formatted);
                },
                year, month, day
        );
        dialog.show();
    }

    private void loadCurrentUser() {
        SharedPreferences loginPrefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
        String userId = loginPrefs.getString("current_user_id", null);
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }
        userAPI.getUserById(userId.trim()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    if (user.getName() != null) {
                        etName.setText(user.getName());
                    }
                    if (user.getPhoneNumber() != null) {
                        etPhone.setText(user.getPhoneNumber());
                    }
                    if (user.getAddress() != null) {
                        etAddress.setText(user.getAddress());
                    }
                    if (user.getDateOfBirth() != null && !user.getDateOfBirth().isEmpty()) {
                        try {
                            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                            Date date = iso.parse(user.getDateOfBirth());
                            if (date != null) {
                                SimpleDateFormat display = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                etDob.setText(display.format(date));
                            }
                        } catch (Exception ignored) { }
                    }
                    if (user.getGender() != null) {
                        String g = user.getGender().toLowerCase(Locale.ROOT);
                        if (g.contains("male") || g.equals("nam")) {
                            rgGender.check(R.id.rbMale);
                        } else if (g.contains("female") || g.equals("nữ") || g.equals("nu")) {
                            rgGender.check(R.id.rbFemale);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });
    }

    private void submitProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dobInput = etDob.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty()) {
            AppToast.show(this, "Vui lòng nhập tên");
            return;
        }
        if (phone.isEmpty()) {
            AppToast.show(this, "Vui lòng nhập số điện thoại");
            return;
        }
        if (dobInput.isEmpty()) {
            AppToast.show(this, "Vui lòng chọn ngày sinh");
            return;
        }
        if (address.isEmpty()) {
            AppToast.show(this, "Vui lòng nhập địa chỉ");
            return;
        }

        String gender = "Other";
        int checkedId = rgGender.getCheckedRadioButtonId();
        if (checkedId == R.id.rbMale) {
            gender = "Male";
        } else if (checkedId == R.id.rbFemale) {
            gender = "Female";
        }

        String dobIso;
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dobInput);
            if (date == null) {
                AppToast.show(this, "Ngày sinh không hợp lệ");
                return;
            }
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            dobIso = isoFormat.format(date);
        } catch (ParseException e) {
            AppToast.show(this, "Ngày sinh không hợp lệ");
            return;
        }
        MediaType textMediaType = MediaType.parse("text/plain");
        RequestBody dobBody = RequestBody.create(textMediaType, dobIso);
        RequestBody addressBody = RequestBody.create(textMediaType, address);
        RequestBody nameBody = RequestBody.create(textMediaType, name);
        RequestBody phoneBody = RequestBody.create(textMediaType, phone);
        RequestBody genderBody = RequestBody.create(textMediaType, gender);

        
        MultipartBody.Part avatarPart = null;

        UserAPI api = RetrofitClient.getInstance().getPreferenceAPI();
        api.updateUserProfile(dobBody, addressBody, nameBody, phoneBody, genderBody, avatarPart)
                .enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    AppToast.show(EditProfileActivity.this, "Cập nhật hồ sơ thành công");
                    finish();
                } else {
                    AppToast.show(EditProfileActivity.this, "Cập nhật thất bại (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                AppToast.show(EditProfileActivity.this, "Lỗi kết nối: " + (t.getMessage() != null ? t.getMessage() : "Unknown"));
            }
        });
    }
}

