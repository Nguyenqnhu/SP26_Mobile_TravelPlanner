package com.example.weathertrip_sep490.model;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

public class Trip {

    private final String id;
    private final String tripTitle;
    private final String city;
    private final String dateRange;
    @Nullable
    private final String costDisplay;
    private final TripStatus status;
    private final int imageResId;

    public Trip(
            String id,
            @NonNull String tripTitle,
            String city,
            String dateRange,
            @Nullable String costDisplay,
            TripStatus status,
            int imageResId) {
        this.id = id;
        this.tripTitle = tripTitle;
        this.city = city;
        this.dateRange = dateRange;
        this.costDisplay = costDisplay;
        this.status = status;
        this.imageResId = imageResId;
    }

    public String getId() {
        return id;
    }

    public String getTripTitle() {
        return tripTitle;
    }

    public String getCity() {
        return city;
    }

    public String getDateRange() {
        return dateRange;
    }

    @Nullable
    public String getCostDisplay() {
        return costDisplay;
    }

    public TripStatus getStatus() {
        return status;
    }

    public int getImageResId() {
        return imageResId;
    }

    public boolean isActive() {
        return status == TripStatus.UPCOMING || status == TripStatus.ONGOING;
    }
}
