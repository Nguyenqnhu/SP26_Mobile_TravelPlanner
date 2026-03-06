package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserPreferencesRequest {

    @SerializedName("preferenceIds")
    private List<String> preferenceIds;

    public UserPreferencesRequest(List<String> preferenceIds) {
        this.preferenceIds = preferenceIds;
    }

    public List<String> getPreferenceIds() {
        return preferenceIds;
    }

    public void setPreferenceIds(List<String> preferenceIds) {
        this.preferenceIds = preferenceIds;
    }
}

