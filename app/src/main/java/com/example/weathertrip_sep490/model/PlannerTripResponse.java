package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PlannerTripResponse {
    @SerializedName(value = "tripId", alternate = {"TripId"})
    private String tripId;

    @SerializedName(value = "segments", alternate = {"Segments"})
    private List<TripSegmentResponse> segments = new ArrayList<>();

    public String getTripId() { return tripId; }
    public List<TripSegmentResponse> getSegments() { return segments; }
}
