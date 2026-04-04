package com.example.weathertrip_sep490.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public final class ItineraryStopRow implements ItineraryRow {

    /** Hiển thị trên thanh xanh (vd: 08:30 – 09:30) */
    private final String timeRange;
    private final String timeStartCol;
    private final String timeEndCol;
    private final String title;
    private final String locationLine;
    private final String openingHours;
    private final String priceText;
    private final String nextDestination;
    private final String weatherTemp;
    @DrawableRes
    private final int imageResId;
    private final double latitude;
    private final double longitude;
    private final int markerOrder;

    public ItineraryStopRow(
            @NonNull String timeRange,
            @NonNull String timeStartCol,
            @NonNull String timeEndCol,
            @NonNull String title,
            @NonNull String locationLine,
            @NonNull String openingHours,
            @NonNull String priceText,
            @NonNull String nextDestination,
            @NonNull String weatherTemp,
            @DrawableRes int imageResId,
            double latitude,
            double longitude,
            int markerOrder
    ) {
        this.timeRange = timeRange;
        this.timeStartCol = timeStartCol;
        this.timeEndCol = timeEndCol;
        this.title = title;
        this.locationLine = locationLine;
        this.openingHours = openingHours;
        this.priceText = priceText;
        this.nextDestination = nextDestination;
        this.weatherTemp = weatherTemp;
        this.imageResId = imageResId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.markerOrder = markerOrder;
    }

    @NonNull
    public String getTimeRange() {
        return timeRange;
    }

    @NonNull
    public String getTimeStartCol() {
        return timeStartCol;
    }

    @NonNull
    public String getTimeEndCol() {
        return timeEndCol;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getLocationLine() {
        return locationLine;
    }

    @NonNull
    public String getOpeningHours() {
        return openingHours;
    }

    @NonNull
    public String getPriceText() {
        return priceText;
    }

    @NonNull
    public String getNextDestination() {
        return nextDestination;
    }

    @NonNull
    public String getWeatherTemp() {
        return weatherTemp;
    }

    public int getImageResId() {
        return imageResId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getMarkerOrder() {
        return markerOrder;
    }
}
