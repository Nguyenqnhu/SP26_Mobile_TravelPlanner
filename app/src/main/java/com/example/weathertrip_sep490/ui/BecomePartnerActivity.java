package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.PartnerRequestResponse;
import com.example.weathertrip_sep490.util.AppToast;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BecomePartnerActivity extends AppCompatActivity {

    private SharedPreferences travelPrefs;
    private UserAPI userAPI;
    private EditText etBusinessName, etBusinessAddress, etBusinessPhone, etBusinessEmail;
    private Button btnSubmit;
    private View btnLicensePicker;
    private TextView tvSelectedFileName;
    private File selectedLicenseFile;
    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_partner);

        travelPrefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
        userAPI = RetrofitClient.getInstance().getUserAPI();
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> handleLicenseFileSelected(uri)
        );

        etBusinessName = findViewById(R.id.etPartnerTitle);
        etBusinessAddress = findViewById(R.id.etPartnerAddress);
        etBusinessPhone = findViewById(R.id.etPartnerPhone);
        etBusinessEmail = findViewById(R.id.etPartnerEmail);
        btnSubmit = findViewById(R.id.btnPartnerSubmit);
        btnLicensePicker = findViewById(R.id.btnPartnerLicensePicker);
        tvSelectedFileName = findViewById(R.id.tvPartnerSelectedFile);
        findViewById(R.id.btnPartnerBack).setOnClickListener(v -> finish());
        btnLicensePicker.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        btnSubmit.setOnClickListener(v -> submitPartnerRequest());
    }

    private void handleLicenseFileSelected(Uri uri) {
        if (uri == null) return;
        try {
            String fileName = FileUtils.getFileName(this, uri);
            File file = FileUtils.createTempFileFromUri(this, uri, fileName);
            selectedLicenseFile = file;
            tvSelectedFileName.setText(fileName);
            AppToast.showSuccess(this, "Đã chọn file: " + fileName);
        } catch (Exception e) {
            selectedLicenseFile = null;
            AppToast.showError(this, "Không thể đọc file giấy phép");
        }
    }

    private void submitPartnerRequest() {
        String businessName = etBusinessName.getText().toString().trim();
        String businessAddress = etBusinessAddress.getText().toString().trim();
        String businessPhone = etBusinessPhone.getText().toString().trim();
        String businessEmail = etBusinessEmail.getText().toString().trim();

        if (businessName.isEmpty()) {
            AppToast.showError(this, "Vui lòng nhập tên địa điểm / thương hiệu");
            return;
        }
        if (businessPhone.isEmpty()) {
            AppToast.showError(this, "Vui lòng nhập số điện thoại liên hệ");
            return;
        }
        if (businessEmail.isEmpty()) {
            AppToast.showError(this, "Vui lòng nhập email liên hệ");
            return;
        }
        if (selectedLicenseFile == null || !selectedLicenseFile.exists()) {
            AppToast.showError(this, "Vui lòng chọn giấy phép kinh doanh");
            return;
        }

        setSubmitting(true);

        RequestBody businessNamePart = RequestBody.create(MediaType.parse("text/plain"), businessName);
        RequestBody businessAddressPart = RequestBody.create(MediaType.parse("text/plain"), businessAddress);
        RequestBody businessPhonePart = RequestBody.create(MediaType.parse("text/plain"), businessPhone);
        RequestBody businessEmailPart = RequestBody.create(MediaType.parse("text/plain"), businessEmail);
        RequestBody fileBody = RequestBody.create(MediaType.parse(getLicenseMimeType(selectedLicenseFile.getName())), selectedLicenseFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("BusinessLicenseFile", selectedLicenseFile.getName(), fileBody);

        userAPI.createPartnerRequest(businessNamePart, businessAddressPart, businessPhonePart, businessEmailPart, filePart)
                .enqueue(new Callback<PartnerRequestResponse>() {
                    @Override
                    public void onResponse(Call<PartnerRequestResponse> call, Response<PartnerRequestResponse> response) {
                        setSubmitting(false);
                        if (response.isSuccessful() && response.body() != null) {
                            travelPrefs.edit().putBoolean("is_partner_request_pending", true).apply();
                            AppToast.showSuccess(BecomePartnerActivity.this, "Gửi yêu cầu đối tác thành công");
                            startActivity(new Intent(BecomePartnerActivity.this, ProfileActivity.class));
                            finish();
                        } else {
                            AppToast.showError(BecomePartnerActivity.this, "Gửi yêu cầu thất bại");
                        }
                    }

                    @Override
                    public void onFailure(Call<PartnerRequestResponse> call, Throwable t) {
                        setSubmitting(false);
                        AppToast.showError(BecomePartnerActivity.this, "Không thể kết nối máy chủ");
                    }
                });
    }

    private void setSubmitting(boolean submitting) {
        btnSubmit.setEnabled(!submitting);
        btnSubmit.setAlpha(submitting ? 0.7f : 1f);
        btnSubmit.setText(submitting ? "Đang gửi..." : "Đăng ký trở thành đối tác");
        btnLicensePicker.setEnabled(!submitting);
        btnBusinessFieldsEnabled(!submitting);
    }

    private void btnBusinessFieldsEnabled(boolean enabled) {
        etBusinessName.setEnabled(enabled);
        etBusinessAddress.setEnabled(enabled);
        etBusinessPhone.setEnabled(enabled);
        etBusinessEmail.setEnabled(enabled);
    }

    private String getLicenseMimeType(String fileName) {
        String lowerName = fileName == null ? "" : fileName.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "image/jpeg";
        if (lowerName.endsWith(".png")) return "image/png";
        return "application/pdf";
    }

    private static class FileUtils {
        static String getFileName(BecomePartnerActivity activity, Uri uri) {
            String name = uri.getLastPathSegment();
            if (name == null || name.trim().isEmpty()) return "license.pdf";
            return name.contains("/") ? name.substring(name.lastIndexOf('/') + 1) : name;
        }

        static File createTempFileFromUri(BecomePartnerActivity activity, Uri uri, String fileName) throws Exception {
            File file = new File(activity.getCacheDir(), fileName);
            try (java.io.InputStream inputStream = activity.getContentResolver().openInputStream(uri);
                 java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file)) {
                if (inputStream == null) throw new IllegalStateException("Cannot open input stream");
                byte[] buffer = new byte[4096];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            }
            return file;
        }
    }
}
