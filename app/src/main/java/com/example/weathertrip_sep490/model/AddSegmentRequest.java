package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class AddSegmentRequest {
    @SerializedName("locationId")
    private String locationId;

    @SerializedName("districtId")
    private String districtId;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    public AddSegmentRequest(String locationId, String districtId, String startDate, String endDate) {
        this.locationId = locationId;
        this.districtId = districtId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

