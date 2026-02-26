package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class RegistRequest {

    @SerializedName("email")
    private String email;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("dateOfBirth")
    private Date dateOfBirth;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("gender")
    private String gender;

    public RegistRequest() {
    }

    public RegistRequest(String email, String password, Date dateOfBirth, String name, String address, String phoneNumber, String gender) {
        this.email = email;
        this.password = password;
        this.address = address;
        this.dateOfBirth=dateOfBirth;
        this.name=name;
        this.phoneNumber=phoneNumber;
        this.gender=gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
