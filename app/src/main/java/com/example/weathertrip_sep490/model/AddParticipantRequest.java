package com.example.weathertrip_sep490.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class AddParticipantRequest {
    @SerializedName("userId")
    @Nullable
    private final String userId;

    @SerializedName("email")
    @Nullable
    private final String email;

    @SerializedName("username")
    @Nullable
    private final String username;

    @SerializedName("autoAccept")
    private final boolean autoAccept;

    public AddParticipantRequest(@Nullable String userId, @Nullable String email, @Nullable String username, boolean autoAccept) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.autoAccept = autoAccept;
    }
}
