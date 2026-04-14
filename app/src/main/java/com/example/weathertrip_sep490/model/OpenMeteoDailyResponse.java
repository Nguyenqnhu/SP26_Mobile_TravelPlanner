package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpenMeteoDailyResponse {
    @SerializedName("daily")
    public Daily daily;

    public static class Daily {
        @SerializedName("time")
        public List<String> time;

        @SerializedName("temperature_2m_max")
        public List<Double> temperatureMax;

        @SerializedName("precipitation_probability_max")
        public List<Double> precipitationProbabilityMax;

        @SerializedName("wind_speed_10m_max")
        public List<Double> windSpeedMax;
    }
}

