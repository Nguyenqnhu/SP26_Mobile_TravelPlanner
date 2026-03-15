package com.example.weathertrip_sep490.model;

public class Partner {
    private final String name;
    private final String description;
    private final String discount;
    private final int imageResId;

    public Partner(String name, String description, String discount, int imageResId) {
        this.name = name;
        this.description = description;
        this.discount = discount;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDiscount() {
        return discount;
    }

    public int getImageResId() {
        return imageResId;
    }
}