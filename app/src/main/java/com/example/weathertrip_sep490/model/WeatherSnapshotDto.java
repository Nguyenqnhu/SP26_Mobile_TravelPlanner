package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class WeatherSnapshotDto {
    @SerializedName(value = "temperatureCelsius", alternate = {"TemperatureCelsius"})
    private double temperatureCelsius;

    @SerializedName(value = "precipitationProbability", alternate = {"PrecipitationProbability"})
    private double precipitationProbability;

    @SerializedName(value = "windSpeed", alternate = {"WindSpeed"})
    private double windSpeed;

    public WeatherSnapshotDto() {}

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(double temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public double getPrecipitationProbability() {
        return precipitationProbability;
    }

    public void setPrecipitationProbability(double precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }
}
