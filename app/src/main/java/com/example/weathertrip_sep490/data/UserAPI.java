package com.example.weathertrip_sep490.data;

import com.example.weathertrip_sep490.model.POI;
import com.example.weathertrip_sep490.model.Preference;
import com.example.weathertrip_sep490.model.TripCreateRequest;
import com.example.weathertrip_sep490.model.TripResponse;
import com.example.weathertrip_sep490.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserAPI {
    @GET("api/preferences/get-all")
    Call<List<Preference>> getAllPreferences();

    @POST("api/user/update-preference")
    Call<Void> updateUserPreferences(@Body List<String> preferenceIds);

    // Get user by Id
    @GET("api/user/{id}")
    Call<User> getUserById(@Path("id") String id);

    // Update Profile
    @Multipart
    @PUT("api/user/update")
    Call<Void> updateUserProfile(
            @Part("DateOfBirth") RequestBody dateOfBirth,
            @Part("Address") RequestBody address,
            @Part("Name") RequestBody name,
            @Part("PhoneNumber") RequestBody phoneNumber,
            @Part("Gender") RequestBody gender,
            @Part MultipartBody.Part avatarUrl
    );

    // Get POIs
    @GET("api/pois/recommended")
    Call<List<POI>> getRecommendedPOIs(@Query("lang") String lang);

    //Create trip
    @POST("api/trip/create")
    Call<TripResponse> createTrip(
            @Query("type") String type,
            @Body TripCreateRequest body);


}
