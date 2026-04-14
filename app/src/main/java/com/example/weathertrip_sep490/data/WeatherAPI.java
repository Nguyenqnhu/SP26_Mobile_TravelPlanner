package com.example.weathertrip_sep490.data;

import com.example.weathertrip_sep490.model.Weather;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherAPI {
    @GET("api/WeatherForecast")
    Call<List<Weather>> getDailyWeather(
            @Query("latitude") String latitude,
            @Query("longitude") String longitude,
            @Query("from") String from,
            @Query("to") String to
    );
}

