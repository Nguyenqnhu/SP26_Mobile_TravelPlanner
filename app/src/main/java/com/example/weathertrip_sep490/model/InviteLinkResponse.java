package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class InviteLinkResponse {
    @SerializedName("inviteUrl")
    private String inviteUrl;

    public String getInviteUrl() {
        return inviteUrl;
    }
}
