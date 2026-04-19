package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlannerDayResponse {
    @SerializedName(value = "date", alternate = {"Date"})
    private Date date;

    @SerializedName(value = "items", alternate = {"Items"})
    private List<PlannerItemResponse> items = new ArrayList<>();

    public Date getDate() { return date; }
    public List<PlannerItemResponse> getItems() { return items; }
}
