package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class TripCreateRequest {
    @SerializedName("title")
    private final String title;

    @SerializedName("startLocation")
    private final String startLocation;

    @SerializedName("endLocation")
    private final String endLocation;


    @SerializedName("startDate")
    private final String startDate;


    @SerializedName("endDate")
    private final String endDate;

    public TripCreateRequest(
            String title,
            String startLocation,
            String endLocation,
            String startDate,
            String endDate
    ) {
        this.title = title;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

