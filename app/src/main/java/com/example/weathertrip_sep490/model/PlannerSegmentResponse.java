package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlannerSegmentResponse {
    @SerializedName(value = "segmentId", alternate = {"SegmentId"})
    private String segmentId;

    @SerializedName(value = "orderIndex", alternate = {"OrderIndex"})
    private int orderIndex;

    @SerializedName(value = "startDate", alternate = {"StartDate"})
    private Date startDate;

    @SerializedName(value = "endDate", alternate = {"EndDate"})
    private Date endDate;

    @SerializedName(value = "days", alternate = {"Days"})
    private List<PlannerDayResponse> days = new ArrayList<>();

    public String getSegmentId() { return segmentId; }
    public int getOrderIndex() { return orderIndex; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public List<PlannerDayResponse> getDays() { return days; }
}
