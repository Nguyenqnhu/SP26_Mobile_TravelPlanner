package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("gender")
    private String gender;

    @SerializedName(value = "isPartner", alternate = {"partner", "is_partner", "partnerStatus", "isMerchant"})
    private Boolean partner;


    public User() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Boolean getPartner() { return partner; }
    public boolean isPartner() { return Boolean.TRUE.equals(partner); }
    public void setPartner(Boolean partner) { this.partner = partner; }
}
