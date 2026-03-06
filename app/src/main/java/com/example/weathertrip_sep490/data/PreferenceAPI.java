package com.example.weathertrip_sep490.data;

import com.example.weathertrip_sep490.model.Preference;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface PreferenceAPI {
    @GET("api/preferences/get-all")
    Call<List<Preference>> getAllPreferences();

    @POST("api/user/update-preference")
    Call<Void> updateUserPreferences(@Body List<String> preferenceIds);
}
