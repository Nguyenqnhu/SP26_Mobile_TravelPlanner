package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class UserPreferenceItem {
    @SerializedName("id")
    private String id;

    @SerializedName("preferenceId")
    private String preferenceId;

    @SerializedName("preferenceName")
    private String preferenceName;

    public String getId() {
        return id;
    }

    public String getPreferenceId() {
        return preferenceId;
    }

    public String getPreferenceName() {
        return preferenceName;
    }
}
