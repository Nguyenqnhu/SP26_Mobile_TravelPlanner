package com.example.weathertrip_sep490.model;


import com.google.gson.annotations.SerializedName;

public class POI {
    @SerializedName(value = "Id", alternate = {"id"})
    private String id;

    @SerializedName(value = "Name", alternate = {"name"})
    private String name;

    @SerializedName(value = "Address", alternate = {"address"})
    private String address;

    @SerializedName(value = "City", alternate = {"city"})
    private String city;

    @SerializedName(value = "ApproxCost", alternate = {"approxCost"})
    private String approxCost;


    @SerializedName(value = "OpenHour", alternate = {"openHour"})
    private String openHour;

    @SerializedName(value = "CloseHour", alternate = {"closeHour"})
    private String closeHour;

    @SerializedName(value = "Is24Hours", alternate = {"is24Hours"})
    private boolean is24Hours;

    // .NET thường serialize camelCase: visitRecommendation
    @SerializedName(value = "visitRecommendation", alternate = {"VisitRecommendation", "visit_recommendation"})
    private String visitRecommendation;

    @SerializedName(value = "GoogleMapLink", alternate = {"googleMapLink"})
    private String googleMapLink;

    @SerializedName(value = "IsIndoor", alternate = {"isIndoor"})
    private boolean isIndoor;

    @SerializedName(value = "Latitude", alternate = {"latitude"})
    private double latitude;

    @SerializedName(value = "Longitude", alternate = {"longitude"})
    private double longitude;

    @SerializedName(value = "LocationId", alternate = {"locationId"})
    private String locationId;

    @SerializedName(value = "LocationName", alternate = {"locationName"})
    private String locationName;

    @SerializedName(value = "POIImgUrl", alternate = {"poiImgUrl", "poiImgURL", "poiImageUrl", "POIImageUrl"})
    private String poiImgUrl;

    @SerializedName(value = "Type", alternate = {"type"})
    private String type;

    @SerializedName(value = "Status", alternate = {"status"})
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getApproxCost() {
        return approxCost;
    }

    public void setApproxCost(String approxCost) {
        this.approxCost = approxCost;
    }

    public String getOpenHour() {
        return openHour;
    }

    public void setOpenHour(String openHour) {
        this.openHour = openHour;
    }

    public String getCloseHour() {
        return closeHour;
    }

    public void setCloseHour(String closeHour) {
        this.closeHour = closeHour;
    }

    public boolean isIs24Hours() {
        return is24Hours;
    }

    public void setIs24Hours(boolean is24Hours) {
        this.is24Hours = is24Hours;
    }

    public String getVisitRecommendation() {
        return visitRecommendation;
    }

    public void setVisitRecommendation(String visitRecommendation) {
        this.visitRecommendation = visitRecommendation;
    }

    public String getGoogleMapLink() {
        return googleMapLink;
    }

    public void setGoogleMapLink(String googleMapLink) {
        this.googleMapLink = googleMapLink;
    }

    public boolean isIndoor() {
        return isIndoor;
    }

    public void setIndoor(boolean indoor) {
        isIndoor = indoor;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getPoiImgUrl() {
        return poiImgUrl;
    }

    public void setPoiImgUrl(String poiImgUrl) {
        this.poiImgUrl = poiImgUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}