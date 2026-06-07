package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class InviteQrResponse {
    @SerializedName("inviteUrl")
    private String inviteUrl;

    @SerializedName("qrCode")
    private String qrCode;

    public String getInviteUrl() {
        return inviteUrl;
    }

    public String getQrCode() {
        return qrCode;
    }
}
