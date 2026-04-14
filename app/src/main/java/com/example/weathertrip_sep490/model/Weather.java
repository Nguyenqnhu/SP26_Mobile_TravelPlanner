package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class Weather {
    @SerializedName(value = "date", alternate = {"Date"})
    private String date;

    @SerializedName(value = "maxTemperature", alternate = {"MaxTemperature", "max_temperature"})
    private double maxTemperature;

    @SerializedName(value = "precipitationProbability", alternate = {"PrecipitationProbability", "precipitation_probability"})
    private double precipitationProbability;

    @SerializedName(value = "maxWindSpeed", alternate = {"MaxWindSpeed", "max_wind_speed"})
    private double maxWindSpeed;

    public String getDate() {
        return date;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getPrecipitationProbability() {
        return precipitationProbability;
    }

    public double getMaxWindSpeed() {
        return maxWindSpeed;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public void setPrecipitationProbability(double precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
    }

    public void setMaxWindSpeed(double maxWindSpeed) {
        this.maxWindSpeed = maxWindSpeed;
    }
}
