package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class PartnerRequestResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("accountId")
    private String accountId;

    @SerializedName("accountName")
    private String accountName;

    @SerializedName("accountEmail")
    private String accountEmail;

    @SerializedName("businessName")
    private String businessName;

    @SerializedName("businessAddress")
    private String businessAddress;

    @SerializedName("businessPhone")
    private String businessPhone;

    @SerializedName("businessEmail")
    private String businessEmail;

    @SerializedName("businessLicenseUrl")
    private String businessLicenseUrl;

    @SerializedName("status")
    private String status;

    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public String getAccountName() { return accountName; }
    public String getAccountEmail() { return accountEmail; }
    public String getBusinessName() { return businessName; }
    public String getBusinessAddress() { return businessAddress; }
    public String getBusinessPhone() { return businessPhone; }
    public String getBusinessEmail() { return businessEmail; }
    public String getBusinessLicenseUrl() { return businessLicenseUrl; }
    public String getStatus() { return status; }
}
