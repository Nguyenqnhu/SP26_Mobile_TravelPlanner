package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class LocationOption {
    @SerializedName(value = "locationId", alternate = {"LocationId"})
    private String locationId;

    @SerializedName(value = "locationName", alternate = {"LocationName"})
    private String locationName;

    @SerializedName(value = "latitude", alternate = {"Latitude"})
    private double latitude;

    @SerializedName(value = "longitude", alternate = {"Longitude"})
    private double longitude;

    public String getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return locationName != null ? locationName : "";
    }
}

