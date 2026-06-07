package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request cho API reset-password.
 * Backend nhận { "resetToken": "...", "newPassword": "..." }.
 */
public class ResetPasswordRequest {

    @SerializedName("resetToken")
    private String resetToken;

    @SerializedName("newPassword")
    private String newPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String resetToken, String newPassword) {
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
