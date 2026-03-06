package com.example.weathertrip_sep490;

import android.app.Application;

import com.example.weathertrip_sep490.data.RetrofitClient;

public class TravelGoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RetrofitClient.init(this);
    }
}
