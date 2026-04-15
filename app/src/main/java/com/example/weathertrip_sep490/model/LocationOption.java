package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class LocationOption {
    @SerializedName(value = "locationId", alternate = {"LocationId"})
    private String locationId;

    @SerializedName(value = "locationName", alternate = {"LocationName"})
    private String locationName;

    public String getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    @Override
    public String toString() {
        return locationName != null ? locationName : "";
    }
}

