package com.example.weathertrip_sep490.data;

import android.content.Context;
import android.util.Log;

import com.example.weathertrip_sep490.BuildConfig;
import com.example.weathertrip_sep490.model.POI;
import com.example.weathertrip_sep490.model.POIListResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = normalizeBaseUrl(BuildConfig.API_BASE_URL);
    private static final String PREFS_NAME = "TravelGoPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private static RetrofitClient instance;
    private static Context appContext;
    private Retrofit retrofit;
    private AuthAPI authAPI;
    private UserAPI userAPI;
    private WeatherAPI weatherAPI;

    private static String normalizeBaseUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("API_BASE_URL is empty");
        }
        String normalized = url.trim();
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }


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
            // Nếu request đã set Authorization (ví dụ truyền trực tiếp ở API call) thì không gắn thêm nữa
            if (original.header("Authorization") != null) {
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
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .callTimeout(120, TimeUnit.SECONDS)
                .build();

        Type listPOIType = new TypeToken<List<POI>>() {}.getType();
        JsonDeserializer<Date> multiFormatDateDeserializer = new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                if (json == null || json.isJsonNull()) {
                    return null;
                }
                String raw = json.getAsString();
                if (raw == null || raw.trim().isEmpty()) {
                    return null;
                }
                String value = raw.trim();
                String[] patterns = new String[]{
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        "yyyy-MM-dd'T'HH:mm:ss'Z'",
                        "yyyy-MM-dd'T'HH:mm:ss.SSS",
                        "yyyy-MM-dd'T'HH:mm:ss",
                        "yyyy-MM-dd"
                };
                for (String pattern : patterns) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                        sdf.setLenient(false);
                        if (pattern.contains("'Z'")) {
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        }
                        return sdf.parse(value);
                    } catch (ParseException ignored) {
                    }
                }
                throw new JsonParseException("Unparseable date: " + value);
            }
        };
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .setLenient()
                .registerTypeAdapter(Date.class, multiFormatDateDeserializer)
                .registerTypeAdapter(listPOIType, new POIListResponse.Deserializer())
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        authAPI = retrofit.create(AuthAPI.class);
        userAPI = retrofit.create(UserAPI.class);
        weatherAPI = retrofit.create(WeatherAPI.class);

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
        return userAPI;
    }

    public UserAPI getPOIAPI(){
        return userAPI;
    }

    public UserAPI getUserAPI() {
        return userAPI;
    }

    public WeatherAPI getWeatherAPI() {
        return weatherAPI;
    }

}