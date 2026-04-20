package com.example.weathertrip_sep490.ui;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.InviteLinkResponse;
import com.example.weathertrip_sep490.model.InviteQrResponse;
import com.example.weathertrip_sep490.model.JoinParticipantResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripInviteDialogFragment extends DialogFragment {

    public interface Listener {
        void onInviteJoined(@NonNull String tripId);
        void onInviteAuthRequired(@NonNull String tripId);
    }

    private static final String ARG_TRIP_ID = "arg_trip_id";
    private static final String ARG_TRIP_TITLE = "arg_trip_title";

    public static TripInviteDialogFragment newInstance(@NonNull String tripId, @NonNull String tripTitle) {
        TripInviteDialogFragment f = new TripInviteDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TRIP_ID, tripId);
        b.putString(ARG_TRIP_TITLE, tripTitle);
        f.setArguments(b);
        return f;
    }

    private String tripId = "";
    private String tripTitle = "";
    private TextView tvLink;
    private TextView tvStatus;
    private ImageView ivQr;
    private MaterialButton btnCopy;
    private MaterialButton btnQr;
    private MaterialButton btnJoin;
    private MaterialButton btnEmail;
    private ImageView btnCloseIcon;
    private String inviteUrl = "";
    private Listener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) listener = (Listener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_trip_invite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tripId = getArguments() != null ? getArguments().getString(ARG_TRIP_ID, "") : "";
        tripTitle = getArguments() != null ? getArguments().getString(ARG_TRIP_TITLE, "") : "";

        tvLink = view.findViewById(R.id.tvInviteLink);
        tvStatus = view.findViewById(R.id.tvInviteStatus);
        ivQr = view.findViewById(R.id.ivInviteQr);
        btnCopy = view.findViewById(R.id.btnInviteCopy);
        btnQr = view.findViewById(R.id.btnInviteQr);
        btnJoin = view.findViewById(R.id.btnInviteJoin);
        btnEmail = view.findViewById(R.id.btnInviteEmail);
        btnCloseIcon = view.findViewById(R.id.btnInviteCloseIcon);

        ((TextView) view.findViewById(R.id.tvInviteDialogTitle)).setText("Chia sẻ chuyến đi");
        ((TextView) view.findViewById(R.id.tvInviteDialogSubtitle)).setText("Thêm người bằng email, sao chép liên kết hoặc quét QR để mời tham gia");

        btnCopy.setEnabled(false);
        btnQr.setEnabled(false);
        btnJoin.setEnabled(false);

        btnCloseIcon.setOnClickListener(v -> dismiss());
        btnCopy.setOnClickListener(v -> copyLink());
        btnQr.setOnClickListener(v -> showQr());
        btnJoin.setOnClickListener(v -> joinTrip());
        btnEmail.setOnClickListener(v -> inviteByEmail());

        loadInviteData();
    }

    private void loadInviteData() {
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getTripInviteLink(tripId).enqueue(new Callback<InviteLinkResponse>() {
            @Override
            public void onResponse(@NonNull Call<InviteLinkResponse> call, @NonNull Response<InviteLinkResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvLink.setText("Không tải được link mời");
                    tvStatus.setText("Vui lòng thử lại sau");
                    return;
                }
                inviteUrl = response.body().getInviteUrl() != null ? response.body().getInviteUrl() : "";
                tvLink.setText(inviteUrl.isEmpty() ? "Chưa có link" : inviteUrl);
                btnCopy.setEnabled(!inviteUrl.isEmpty());
                btnQr.setEnabled(!inviteUrl.isEmpty());
                loadQr();
            }

            @Override
            public void onFailure(@NonNull Call<InviteLinkResponse> call, @NonNull Throwable t) {
                tvLink.setText("Lỗi mạng khi tải invite link");
                tvStatus.setText(t.getMessage() != null ? t.getMessage() : "unknown");
            }
        });
    }

    private void loadQr() {
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getTripInviteQr(tripId).enqueue(new Callback<InviteQrResponse>() {
            @Override
            public void onResponse(@NonNull Call<InviteQrResponse> call, @NonNull Response<InviteQrResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvStatus.setText("Không tải được QR");
                    btnJoin.setEnabled(true);
                    return;
                }
                String base64 = response.body().getQrCode();
                if (base64 != null && !base64.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ivQr.setImageBitmap(bmp);
                    } catch (Exception e) {
                        tvStatus.setText("QR không hợp lệ");
                    }
                }
                btnJoin.setEnabled(true);
                tvStatus.setText("Sẵn sàng để mời bạn bè");
            }

            @Override
            public void onFailure(@NonNull Call<InviteQrResponse> call, @NonNull Throwable t) {
                tvStatus.setText("Lỗi mạng khi tải QR");
                btnJoin.setEnabled(true);
            }
        });
    }

    private void copyLink() {
        if (inviteUrl == null || inviteUrl.trim().isEmpty()) return;
        ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("inviteUrl", inviteUrl));
        Toast.makeText(requireContext(), "Đã copy link", Toast.LENGTH_SHORT).show();
    }

    private void showQr() {
        if (inviteUrl == null || inviteUrl.trim().isEmpty()) return;
        tvStatus.setText("QR đã sẵn sàng bên dưới");
    }

    private void shareLink() {
        if (inviteUrl == null || inviteUrl.trim().isEmpty()) return;
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, tripTitle);
        send.putExtra(Intent.EXTRA_TEXT, inviteUrl);
        try {
            startActivity(Intent.createChooser(send, "Chia sẻ link mời"));
        } catch (ActivityNotFoundException ignored) { }
    }

    private void inviteByEmail() {
        Toast.makeText(requireContext(), "Tính năng mời qua email sẽ được nối theo API participants", Toast.LENGTH_SHORT).show();
    }

    private void joinTrip() {
        String token = requireContext().getSharedPreferences("TravelGoPrefs", Context.MODE_PRIVATE)
                .getString("access_token", "");
        if (token == null || token.trim().isEmpty()) {
            if (listener != null) listener.onInviteAuthRequired(tripId);
            dismiss();
            return;
        }

        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.joinTrip(tripId).enqueue(new Callback<JoinParticipantResponse>() {
            @Override
            public void onResponse(@NonNull Call<JoinParticipantResponse> call, @NonNull Response<JoinParticipantResponse> response) {
                if (!response.isSuccessful()) {
                    String details = "";
                    try {
                        okhttp3.ResponseBody eb = response.errorBody();
                        if (eb != null) details = eb.string();
                    } catch (Exception ignored) {}
                    tvStatus.setText("Join thất bại: " + response.code() + (details.isEmpty() ? "" : " · " + details));
                    return;
                }
                Toast.makeText(requireContext(), "Đã tham gia chuyến đi", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onInviteJoined(tripId);
                dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<JoinParticipantResponse> call, @NonNull Throwable t) {
                tvStatus.setText("Lỗi mạng khi join: " + (t.getMessage() != null ? t.getMessage() : "unknown"));
            }
        });
    }

}
