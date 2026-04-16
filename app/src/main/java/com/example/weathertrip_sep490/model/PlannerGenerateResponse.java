package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class PlannerGenerateResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("tripId")
    private String tripId;

    public String getMessage() {
        return message;
    }

    public String getTripId() {
        return tripId;
    }
}

