package com.example.weathertrip_sep490.model;

import androidx.annotation.NonNull;

public final class ItinerarySegmentRow implements ItineraryRow {

    private final String segmentLabel;
    private final String cityName;
    private final String weatherTemp;
    private final String segmentId;

    public ItinerarySegmentRow(
            @NonNull String segmentLabel,
            @NonNull String cityName,
            @NonNull String weatherTemp
    ) {
        this(segmentLabel, cityName, weatherTemp, null);
    }

    public ItinerarySegmentRow(
            @NonNull String segmentLabel,
            @NonNull String cityName,
            @NonNull String weatherTemp,
            String segmentId
    ) {
        this.segmentLabel = segmentLabel;
        this.cityName = cityName;
        this.weatherTemp = weatherTemp;
        this.segmentId = segmentId;
    }

    @NonNull
    public String getSegmentLabel() {
        return segmentLabel;
    }

    @NonNull
    public String getCityName() {
        return cityName;
    }

    @NonNull
    public String getWeatherTemp() {
        return weatherTemp;
    }

    public String getSegmentId() {
        return segmentId;
    }
}
