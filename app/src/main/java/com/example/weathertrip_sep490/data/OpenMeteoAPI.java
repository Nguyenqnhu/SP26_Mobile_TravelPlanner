package com.example.weathertrip_sep490.data;

import com.example.weathertrip_sep490.model.OpenMeteoDailyResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenMeteoAPI {
    @GET("v1/forecast")
    Call<OpenMeteoDailyResponse> getDaily(
            @Query("latitude") String latitude,
            @Query("longitude") String longitude,
            @Query("daily") String daily,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("timezone") String timezone
    );
}

