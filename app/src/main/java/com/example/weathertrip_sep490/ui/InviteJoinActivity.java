package com.example.weathertrip_sep490.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.JoinParticipantResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InviteJoinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleInviteLink();
    }

    private void handleInviteLink() {
        Uri data = getIntent() != null ? getIntent().getData() : null;
        String tripId = data != null ? data.getQueryParameter("tripId") : null;
        if (tripId == null || tripId.trim().isEmpty()) {
            Toast.makeText(this, "Link mời không hợp lệ", Toast.LENGTH_SHORT).show();
            finishToWelcome();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("TravelGoPrefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", "");
        if (token == null || token.trim().isEmpty()) {
            Intent login = new Intent(this, LoginActivity.class);
            login.putExtra("pending_join_trip_id", tripId.trim());
            startActivity(login);
            finish();
            return;
        }

        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.joinTrip(tripId.trim()).enqueue(new Callback<JoinParticipantResponse>() {
            @Override
            public void onResponse(@NonNull Call<JoinParticipantResponse> call, @NonNull Response<JoinParticipantResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InviteJoinActivity.this, "Đã tham gia chuyến đi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(InviteJoinActivity.this, "Không thể tham gia chuyến đi (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(InviteJoinActivity.this, TripManageActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<JoinParticipantResponse> call, @NonNull Throwable t) {
                Toast.makeText(InviteJoinActivity.this, "Lỗi mạng khi tham gia: " + (t.getMessage() != null ? t.getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(InviteJoinActivity.this, TripManageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void finishToWelcome() {
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }
}
