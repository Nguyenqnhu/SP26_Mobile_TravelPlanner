package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class SavedPromotionItem {
    @SerializedName(value = "savedPromotionId", alternate = {"SavedPromotionId"})
    private String savedPromotionId;

    @SerializedName(value = "promotionId", alternate = {"PromotionId"})
    private String promotionId;

    @SerializedName(value = "adId", alternate = {"AdId"})
    private String adId;

    @SerializedName(value = "savedAt", alternate = {"SavedAt"})
    private String savedAt;

    @SerializedName(value = "promotionTitle", alternate = {"PromotionTitle"})
    private String promotionTitle;

    @SerializedName(value = "advertisementTitle", alternate = {"AdvertisementTitle"})
    private String advertisementTitle;

    @SerializedName(value = "poiName", alternate = {"PoiName", "POIName"})
    private String poiName;

    @SerializedName(value = "promotionDescription", alternate = {"PromotionDescription"})
    private String promotionDescription;

    public String getSavedPromotionId() {
        return savedPromotionId;
    }

    public String getPromotionId() {
        return promotionId;
    }

    public String getAdId() {
        return adId;
    }

    public String getSavedAt() {
        return savedAt;
    }

    public String getPromotionTitle() {
        return promotionTitle;
    }

    public String getAdvertisementTitle() {
        return advertisementTitle;
    }

    public String getPoiName() {
        return poiName;
    }

    public String getPromotionDescription() {
        return promotionDescription;
    }
}

