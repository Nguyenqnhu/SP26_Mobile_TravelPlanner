package com.example.weathertrip_sep490.model;



public class Destination {
    private final String name;
    private final String city;
    private final double rating;
    private final int reviewCount;
    private final int imageResId;

    public Destination(String name, String city, double rating, int reviewCount, int imageResId) {
        this.name = name;
        this.city = city;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public double getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public int getImageResId() {
        return imageResId;
    }
}