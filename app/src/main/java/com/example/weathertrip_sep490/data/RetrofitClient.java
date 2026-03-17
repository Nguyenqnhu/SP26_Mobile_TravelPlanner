package com.example.weathertrip_sep490.data;

import android.content.Context;
import android.util.Log;

import com.example.weathertrip_sep490.BuildConfig;
import com.example.weathertrip_sep490.model.POI;
import com.example.weathertrip_sep490.model.POIListResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = BuildConfig.API_BASE_URL;
    private static final String PREFS_NAME = "TravelGoPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private static RetrofitClient instance;
    private static Context appContext;
    private Retrofit retrofit;
    private AuthAPI authAPI;
    private UserAPI preferenceAPI;
    private UserAPI poiAPI;

    public static void init(Context context) {
        appContext = context != null ? context.getApplicationContext() : null;
    }

    private RetrofitClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor authInterceptor = chain -> {
            okhttp3.Request original = chain.request();
            String path = original.url().encodedPath();
            // Không gắn token cho login, register, refresh, verify OTP, forgot password
            if (path != null && (path.contains("/auth/login") || path.contains("/auth/register")
                    || path.contains("/auth/verify-") || path.contains("/auth/request-password-reset")
                    || path.contains("/auth/resend-") || path.contains("/auth/refresh-token"))) {
                return chain.proceed(original);
            }
            String token = null;
            if (appContext != null) {
                token = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .getString(KEY_ACCESS_TOKEN, null);
                if (token != null) token = token.trim();
            }
            okhttp3.Request.Builder builder = original.newBuilder();
            if (token != null && !token.isEmpty()) {
                String authValue = token.trim().startsWith("Bearer ") ? token.trim() : "Bearer " + token.trim();
                builder.addHeader("Authorization", authValue);
            }
            return chain.proceed(builder.build());
        };

        Interceptor urlLog = chain -> {
            okhttp3.Request r = chain.request();
            Log.d("RetrofitClient", ">>> " + r.url());
            okhttp3.Response resp = chain.proceed(r);
            if (!resp.isSuccessful()) {
                Log.e("RetrofitClient", "!!! " + resp.code() + " " + r.url());
            }
            return resp;
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(urlLog)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Type listPOIType = new TypeToken<List<POI>>() {}.getType();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .setLenient()
                .registerTypeAdapter(listPOIType, new POIListResponse.Deserializer())
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        authAPI = retrofit.create(AuthAPI.class);
        preferenceAPI = retrofit.create(UserAPI.class);
        poiAPI = retrofit.create(UserAPI.class);
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

    public UserAPI getPreferenceAPI() {
        return preferenceAPI;
    }

    public UserAPI getPOIAPI(){return poiAPI;}
}