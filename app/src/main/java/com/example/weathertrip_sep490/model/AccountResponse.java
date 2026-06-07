package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class AccountResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("roleName")
    private String roleName;

    @SerializedName("name")
    private String name;

    @SerializedName("profile")
    private ProfileResponse profile;

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getName() {
        return name;
    }

    public ProfileResponse getProfile() {
        return profile;
    }
}
