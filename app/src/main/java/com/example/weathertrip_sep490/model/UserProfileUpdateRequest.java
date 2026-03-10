package com.example.weathertrip_sep490.model;

public class UserProfileUpdateRequest {
    private String dateOfBirth;
    private String address;
    private String name;
    private String phoneNumber;
    private String avatarUrl;
    private String gender;

    public UserProfileUpdateRequest(String dateOfBirth,
                                    String address,
                                    String name,
                                    String phoneNumber,
                                    String avatarUrl,
                                    String gender) {
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getGender() {
        return gender;
    }
}

