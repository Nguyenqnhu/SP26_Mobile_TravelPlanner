package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response từ API verify-reset-password-otp.
 * Backend trả về { "resetToken": "..." }.
 */
public class VerifyResetPasswordOtpResponse {

    @SerializedName("resetToken")
    private String resetToken;

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
}
