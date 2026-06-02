package com.example.weathertrip_sep490.data;

import com.example.weathertrip_sep490.model.ChangePasswordRequest;
import com.example.weathertrip_sep490.model.ForgotPasswordRequest;
import com.example.weathertrip_sep490.model.LoginRequest;
import com.example.weathertrip_sep490.model.LoginResponse;
import com.example.weathertrip_sep490.model.OtpRequest;
import com.example.weathertrip_sep490.model.RegistRequest;
import com.example.weathertrip_sep490.model.ResetPasswordRequest;
import com.example.weathertrip_sep490.model.AccountResponse;
import com.example.weathertrip_sep490.model.User;
import com.example.weathertrip_sep490.model.VerifyResetPasswordOtpResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthAPI {

    @GET("api/auth/me")
    Call<AccountResponse> getCurrentUser();

    // 1. Đăng ký tài khoản mới
    @POST("api/auth/register")
    Call<Void> register(@Body RegistRequest request);

    // 2. Xác thực OTP khi đăng ký
    @POST("api/auth/verify-register-otp")
    Call<Void> verifyRegisterOtp(@Body OtpRequest request);

    // 3. Gửi lại OTP đăng ký (Sử dụng Query tham số email)
    @GET("api/auth/resend-register-otp")
    Call<Void> resendRegisterOtp(@Query("email") String email);

    // 4. Đăng nhập hệ thống
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // 5. Yêu cầu đặt lại mật khẩu (Quên mật khẩu)
    // Quên mật khẩu: Gửi email để nhận OTP
    @POST("api/auth/request-password-reset")
    Call<Void> requestPasswordReset(@Body ForgotPasswordRequest request);

    // 6. Xác thực OTP quên mật khẩu → trả về resetToken
    @POST("api/auth/verify-reset-password-otp")
    Call<VerifyResetPasswordOtpResponse> verifyResetPasswordOtp(@Body OtpRequest request);

    @POST("api/auth/reset-password")
    Call<Void> resetPassword(@Body ResetPasswordRequest request);

    // 7. Đổi mật khẩu khi đang đăng nhập (Ảnh image_acdddc)
    @POST("api/auth/change-password")
    Call<Void> changePassword(@Body ChangePasswordRequest request);

    // 8. Làm mới Token (Ảnh image_acdddc - Dùng Query parameter)
    @POST("api/auth/refresh-token")
    Call<LoginResponse> refreshToken(@Query("refreshToken") String refreshToken);

    // 9. Lấy thông tin user hiện tại qua Email (Ảnh image_abf53c)
    @GET("api/auth/account-by-email")
    Call<User> getAccountByEmail(@Query("email") String email);
    // 10. Lấy tất cả tài khoản (Quyền Admin)
    @GET("api/auth/all")
    Call<List<User>> getAllAccounts();
}