package com.example.weathertrip_sep490.model;

public class OtpRequest {
    private String email;
    private String otpCode;

    public OtpRequest() {
    }

    public OtpRequest(String email, String otpCode) {
        this.email = email;
        this.otpCode=otpCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
