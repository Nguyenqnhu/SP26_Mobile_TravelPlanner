package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class AdvertisementItem {
    @SerializedName("adId")
    private String adId;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName(value = "createdAt", alternate = {"CreatedAt"})
    private String createdAt;

    @SerializedName(value = "matchScore", alternate = {"MatchScore"})
    private double matchScore;

    @SerializedName(value = "matchPercentage", alternate = {"MatchPercentage"})
    private double matchPercentage;

    @SerializedName(value = "poiName", alternate = {"PoiName", "POIName"})
    private String poiName;

    @SerializedName(value = "partnerName", alternate = {"PartnerName"})
    private String partnerName;

    @SerializedName(value = "partnerAvatarUrl", alternate = {"PartnerAvatarUrl"})
    private String partnerAvatarUrl;

    @SerializedName("promotion")
    private PromotionSummary promotion;

    public String getAdId() {
        return adId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public String getPoiName() {
        return poiName;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public String getPartnerAvatarUrl() {
        return partnerAvatarUrl;
    }

    public PromotionSummary getPromotion() {
        return promotion;
    }

    public static class PromotionSummary {
        @SerializedName(value = "promotionId", alternate = {"PromotionId"})
        private String promotionId;

        @SerializedName(value = "title", alternate = {"Title"})
        private String title;

        @SerializedName(value = "description", alternate = {"Description"})
        private String description;

        public String getPromotionId() {
            return promotionId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }
}
