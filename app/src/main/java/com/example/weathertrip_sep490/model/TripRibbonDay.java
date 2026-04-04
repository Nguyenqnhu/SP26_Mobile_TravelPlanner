package com.example.weathertrip_sep490.model;

import androidx.annotation.NonNull;

import java.util.Calendar;

/** Một ô ngày trên băng lịch (T2, số ngày, …). */
public final class TripRibbonDay {

    private final Calendar calendar;
    private final String weekdayShort;

    public TripRibbonDay(@NonNull Calendar calendar, @NonNull String weekdayShort) {
        this.calendar = calendar;
        this.weekdayShort = weekdayShort;
    }

    @NonNull
    public Calendar getCalendar() {
        return calendar;
    }

    @NonNull
    public String getWeekdayShort() {
        return weekdayShort;
    }

    public int getDayOfMonth() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
}
