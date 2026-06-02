package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("avtUrl")
    private String avtUrl;

    @SerializedName("gender")
    private String gender;

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAvtUrl() {
        return avtUrl;
    }

    public String getGender() {
        return gender;
    }
}
