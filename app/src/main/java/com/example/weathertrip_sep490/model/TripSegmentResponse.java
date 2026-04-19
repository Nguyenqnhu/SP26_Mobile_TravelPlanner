package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class TripSegmentResponse {
    @SerializedName(value = "segmentId", alternate = {"SegmentId"})
    private String segmentId;

    @SerializedName(value = "tripId", alternate = {"TripId"})
    private String tripId;

    @SerializedName(value = "locationId", alternate = {"LocationId"})
    private String locationId;

    @SerializedName(value = "orderIndex", alternate = {"OrderIndex"})
    private int orderIndex;

    @SerializedName(value = "startDate", alternate = {"StartDate"})
    private Date startDate;

    @SerializedName(value = "endDate", alternate = {"EndDate"})
    private Date endDate;

    @SerializedName(value = "distanceKm", alternate = {"DistanceKm"})
    private Double distanceKm;

    @SerializedName(value = "createdAt", alternate = {"CreatedAt"})
    private Date createdAt;

    public String getSegmentId() { return segmentId; }
    public String getTripId() { return tripId; }
    public String getLocationId() { return locationId; }
    public int getOrderIndex() { return orderIndex; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public Double getDistanceKm() { return distanceKm; }
    public Date getCreatedAt() { return createdAt; }
}
