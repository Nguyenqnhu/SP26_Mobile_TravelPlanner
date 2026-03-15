package com.example.weathertrip_sep490.model;



public class QuickAction {
    private final String title;
    private final int iconResId;

    public QuickAction(String title, int iconResId) {
        this.title = title;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }
}