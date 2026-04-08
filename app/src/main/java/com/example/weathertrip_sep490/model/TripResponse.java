package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class TripResponse {
    @SerializedName("tripId")
    private String tripId;

    @SerializedName("ownerId")
    private String ownerId;

    @SerializedName("title")
    private String title;


    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("status")
    private String status;

    @SerializedName("tripType")
    private String tripType;

    @SerializedName("createdAt")
    private String createdAt;

    public String getTripId() {
        return tripId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }

    public String getTripType() {
        return tripType;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

