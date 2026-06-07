package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;

public class JoinParticipantResponse {
    @SerializedName("participantId")
    private String participantId;

    public String getParticipantId() {
        return participantId;
    }
}
