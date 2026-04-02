package com.example.weathertrip_sep490.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

public class TimelineEvent {

    private final String time;
    private final String title;
    @Nullable
    private final String subtitle;
    @DrawableRes
    private final int imageResId;
    @Nullable
    private final String weatherTemp;
    @Nullable
    private final String weatherDesc;

    public TimelineEvent(
            String time,
            String title,
            @Nullable String subtitle,
            @DrawableRes int imageResId,
            @Nullable String weatherTemp,
            @Nullable String weatherDesc
    ) {
        this.time = time;
        this.title = title;
        this.subtitle = subtitle;
        this.imageResId = imageResId;
        this.weatherTemp = weatherTemp;
        this.weatherDesc = weatherDesc;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    @Nullable
    public String getSubtitle() {
        return subtitle;
    }

    public int getImageResId() {
        return imageResId;
    }

    @Nullable
    public String getWeatherTemp() {
        return weatherTemp;
    }

    @Nullable
    public String getWeatherDesc() {
        return weatherDesc;
    }
}

