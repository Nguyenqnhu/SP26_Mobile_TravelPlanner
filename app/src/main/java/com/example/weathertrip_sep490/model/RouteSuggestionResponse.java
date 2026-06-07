package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RouteSuggestionResponse {
    @SerializedName("routeId")
    private String routeId;

    @SerializedName("routeIndex")
    private int routeIndex;

    @SerializedName("stops")
    private List<RouteStopDto> stops;

    @SerializedName("totalDistanceKm")
    private double totalDistanceKm;

    @SerializedName("weatherSummary")
    private String weatherSummary;

    @SerializedName("travelAdvice")
    private String travelAdvice;

    @SerializedName("recommendedActivities")
    private List<String> recommendedActivities;

    @SerializedName("warnings")
    private List<String> warnings;

    @SerializedName("polyline")
    private List<RoutePolylinePointDto> polyline;

    public RouteSuggestionResponse() {}

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public int getRouteIndex() {
        return routeIndex;
    }

    public void setRouteIndex(int routeIndex) {
        this.routeIndex = routeIndex;
    }

    public List<RouteStopDto> getStops() {
        return stops;
    }

    public void setStops(List<RouteStopDto> stops) {
        this.stops = stops;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public String getWeatherSummary() {
        return weatherSummary;
    }

    public void setWeatherSummary(String weatherSummary) {
        this.weatherSummary = weatherSummary;
    }

    public String getTravelAdvice() {
        return travelAdvice;
    }

    public void setTravelAdvice(String travelAdvice) {
        this.travelAdvice = travelAdvice;
    }

    public List<String> getRecommendedActivities() {
        return recommendedActivities;
    }

    public void setRecommendedActivities(List<String> recommendedActivities) {
        this.recommendedActivities = recommendedActivities;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<RoutePolylinePointDto> getPolyline() {
        return polyline;
    }

    public void setPolyline(List<RoutePolylinePointDto> polyline) {
        this.polyline = polyline;
    }
}
