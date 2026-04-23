package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class DistrictOption {
    @SerializedName(value = "id", alternate = {"Id"})
    private String id;

    @SerializedName(value = "name", alternate = {"Name"})
    private String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}

