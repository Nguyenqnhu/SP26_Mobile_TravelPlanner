package com.example.weathertrip_sep490.model;


import com.google.gson.annotations.SerializedName;

public class POI {
    private String id;
    private String name;
    private String address;
    private String city;
    private String approxCost;
    private String openingHours;
    private String googleMapLink;
    private boolean isIndoor;
    private double latitude;
    private double longitude;
    private String locationId;

    @SerializedName(value = "POIImgUrl", alternate = {"poiImgUrl", "poiImgURL", "poiImageUrl", "POIImageUrl"})
    private String poiImgUrl;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getApproxCost() {
        return approxCost;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public String getGoogleMapLink() {
        return googleMapLink;
    }

    public boolean isIndoor() {
        return isIndoor;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getPoiImgUrl() {
        return poiImgUrl;
    }
}