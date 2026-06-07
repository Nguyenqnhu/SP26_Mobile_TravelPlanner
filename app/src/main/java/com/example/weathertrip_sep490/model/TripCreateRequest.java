package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class TripCreateRequest {
    @SerializedName("title")
    private final String title;

    @SerializedName("startLocation")
    private final String startLocation;

    @SerializedName("startDistrictId")
    private final String startDistrictId;

    @SerializedName("endLocation")
    private final String endLocation;

    @SerializedName("endDistrictId")
    private final String endDistrictId;


    @SerializedName("startDate")
    private final String startDate;


    @SerializedName("endDate")
    private final String endDate;

    public TripCreateRequest(
            String title,
            String startLocation,
            String startDistrictId,
            String endLocation,
            String endDistrictId,
            String startDate,
            String endDate
    ) {
        this.title = title;
        this.startLocation = startLocation;
        this.startDistrictId = startDistrictId;
        this.endLocation = endLocation;
        this.endDistrictId = endDistrictId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

