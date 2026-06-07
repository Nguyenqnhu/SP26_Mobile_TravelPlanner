package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class PlannerItemResponse {
    @SerializedName(value = "type", alternate = {"Type"})
    private String type;

    @SerializedName(value = "poiName", alternate = {"PoiName"})
    private String poiName;

    @SerializedName(value = "address", alternate = {"Address"})
    private String address;

    @SerializedName(value = "locationName", alternate = {"LocationName"})
    private String locationName;

    @SerializedName(value = "isIndoor", alternate = {"IsIndoor"})
    private boolean isIndoor;

    @SerializedName(value = "startTime", alternate = {"StartTime"})
    private String startTime;

    @SerializedName(value = "endTime", alternate = {"EndTime"})
    private String endTime;

    @SerializedName(value = "weatherRiskScore", alternate = {"WeatherRiskScore"})
    private double weatherRiskScore;

    @SerializedName(value = "weather", alternate = {"Weather"})
    private WeatherSnapshotDto weather;

    @SerializedName(value = "aiReason", alternate = {"AiReason", "AIReason"})
    private String aiReason;

    @SerializedName(value = "poiImg", alternate = {"PoiImg", "POIImg"})
    private String poiImg;

    public String getType() { return type; }
    public String getPoiName() { return poiName; }
    public String getAddress() { return address; }
    public String getLocationName() { return locationName; }
    public boolean isIndoor() { return isIndoor; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public double getWeatherRiskScore() { return weatherRiskScore; }
    public WeatherSnapshotDto getWeather() { return weather; }
    public String getAiReason() { return aiReason; }
    public String getPoiImg() { return poiImg; }
}
