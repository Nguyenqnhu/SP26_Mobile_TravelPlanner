package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class RouteStopDto {
    @SerializedName("nodeId")
    private String nodeId;

    @SerializedName("label")
    private String label;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("distanceFromPrevKm")
    private double distanceFromPrevKm;

    @SerializedName("routeType")
    private String routeType;

    @SerializedName("weather")
    private WeatherSnapshotDto weather;

    public RouteStopDto() {}

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public double getDistanceFromPrevKm() {
        return distanceFromPrevKm;
    }

    public void setDistanceFromPrevKm(double distanceFromPrevKm) {
        this.distanceFromPrevKm = distanceFromPrevKm;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public WeatherSnapshotDto getWeather() {
        return weather;
    }

    public void setWeather(WeatherSnapshotDto weather) {
        this.weather = weather;
    }
}
