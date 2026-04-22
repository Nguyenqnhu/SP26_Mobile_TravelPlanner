package com.example.weathertrip_sep490.model;

public class Partner {
    private final String name;
    private final String subtitle;
    private final String imageUrl;

    public Partner(String name, String subtitle, String imageUrl) {
        this.name = name;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}