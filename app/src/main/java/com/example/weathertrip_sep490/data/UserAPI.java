package com.example.weathertrip_sep490.data;

import com.example.weathertrip_sep490.model.POI;
import com.example.weathertrip_sep490.model.Preference;
import com.example.weathertrip_sep490.model.AddParticipantRequest;
import com.example.weathertrip_sep490.model.AddSegmentRequest;
import com.example.weathertrip_sep490.model.InviteLinkResponse;
import com.example.weathertrip_sep490.model.InviteQrResponse;
import com.example.weathertrip_sep490.model.JoinParticipantResponse;
import com.example.weathertrip_sep490.model.LocationOption;
import com.example.weathertrip_sep490.model.AdvertisementItem;
import com.example.weathertrip_sep490.model.PlannerGenerateResponse;
import com.example.weathertrip_sep490.model.PlannerTripResponse;
import com.example.weathertrip_sep490.model.SavedPromotionItem;
import com.example.weathertrip_sep490.model.TripCreateRequest;
import com.example.weathertrip_sep490.model.TripSegmentResponse;
import com.example.weathertrip_sep490.model.TripResponse;
import com.example.weathertrip_sep490.model.User;
import com.example.weathertrip_sep490.model.UserPreferencesRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
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
    Call<Void> updateUserPreferences(@Header("Authorization") String authorization,
                                     @Body java.util.List<String> preferenceIds);

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

    //List all location
    @GET("api/trip/get-all-location")
    Call<List<LocationOption>> getAllLocations();

    //Add segment
    @POST("api/trip/{tripId}/segments")
    Call<List<TripSegmentResponse>> addTripSegments(
            @Path("tripId") String tripId,
            @Query("insertAt") int insertAt,
            @Body List<AddSegmentRequest> body
    );

    // AI generate trip
    @POST("api/planner/{tripId}/generate")
    Call<PlannerGenerateResponse> generatePlanner(@Path("tripId") String tripId);

    //Get trip detail
    @GET("api/planner/{tripId}/get")
    Call<PlannerTripResponse> getPlanner(@Path("tripId") String tripId);

    //invite by link
    @GET("api/trip/{tripId}/invite-link")
    Call<InviteLinkResponse> getTripInviteLink(@Path("tripId") String tripId);

    //invite by email/user
    @POST("api/trip/{tripId}/participants")
    Call<Void> addTripParticipant(
            @Path("tripId") String tripId,
            @Body AddParticipantRequest body
    );

    //invite by QR
    @GET("api/trip/{tripId}/generate-qr")
    Call<InviteQrResponse> getTripInviteQr(@Path("tripId") String tripId);

    
    @POST("api/invites/join")
    Call<JoinParticipantResponse> joinTrip(@Query("tripId") String tripId);

    @GET("api/advertisements/active")
    Call<List<AdvertisementItem>> getActiveAdvertisements();

    @POST("api/promotions/{promotionId}/save")
    Call<Void> savePromotion(@Path("promotionId") String promotionId);

    @GET("api/users/me/saved-promotions")
    Call<List<SavedPromotionItem>> getMySavedPromotions();

}
