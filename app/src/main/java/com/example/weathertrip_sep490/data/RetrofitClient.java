package com.example.weathertrip_sep490.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // For Android Emulator, use 10.0.2.2 instead of localhost
    // For real device, use your computer's IP address (e.g., http://192.168.1.100:5131/)
    private static final String BASE_URL = "http://10.0.2.2:5131/";
    private static RetrofitClient instance;
    private Retrofit retrofit;
    private AuthAPI authAPI;
    private PreferenceAPI preferenceAPI;

    private RetrofitClient() {
        // Configure Gson với ISO 8601 date format
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        authAPI = retrofit.create(AuthAPI.class);
        preferenceAPI = retrofit.create(PreferenceAPI.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public AuthAPI getAuthAPI() {
        return authAPI;
    }

    public PreferenceAPI getPreferenceAPI() {
        return preferenceAPI;
    }
}
