package com.example.weathertrip_sep490.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Patterns;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.AddParticipantRequest;
import com.example.weathertrip_sep490.model.InviteLinkResponse;
import com.example.weathertrip_sep490.model.InviteQrResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripInviteDialogFragment extends BottomSheetDialogFragment {

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
    private EditText etEmail;
    private ImageView ivQr;
    private MaterialButton btnCopy;
    private MaterialButton btnQr;
    private MaterialButton btnEmail;
    private ImageView btnCloseIcon;
    private String inviteUrl = "";
    private boolean qrLoaded = false;
    private boolean qrLoading = false;
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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bs = (BottomSheetDialog) d;
            View bottomSheet = bs.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet == null) return;
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);

            // đảm bảo wrapContent, không bị ép chiều cao
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
            lp.height = CoordinatorLayout.LayoutParams.WRAP_CONTENT;
            bottomSheet.setLayoutParams(lp);
        });
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tripId = getArguments() != null ? getArguments().getString(ARG_TRIP_ID, "") : "";
        tripTitle = getArguments() != null ? getArguments().getString(ARG_TRIP_TITLE, "") : "";

        tvLink = view.findViewById(R.id.tvInviteLink);
        tvStatus = view.findViewById(R.id.tvInviteStatus);
        etEmail = view.findViewById(R.id.etInviteEmail);
        ivQr = view.findViewById(R.id.ivInviteQr);
        btnCopy = view.findViewById(R.id.btnInviteCopy);
        btnQr = view.findViewById(R.id.btnInviteQr);
        btnEmail = view.findViewById(R.id.btnInviteEmail);
        btnCloseIcon = view.findViewById(R.id.btnInviteCloseIcon);

        ((TextView) view.findViewById(R.id.tvInviteDialogTitle)).setText("Mời bạn bè");
        ((TextView) view.findViewById(R.id.tvInviteDialogSubtitle)).setText("Chia sẻ link mời hoặc gửi email để mời bạn vào chuyến đi");

        btnCopy.setEnabled(false);
        btnQr.setEnabled(false);
        btnEmail.setEnabled(false);

        btnCloseIcon.setOnClickListener(v -> dismiss());
        btnCopy.setOnClickListener(v -> copyLink());
        btnQr.setOnClickListener(v -> showQr());
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
                btnEmail.setEnabled(true);
                tvStatus.setText("Sẵn sàng để mời bạn bè");
            }

            @Override
            public void onFailure(@NonNull Call<InviteLinkResponse> call, @NonNull Throwable t) {
                tvLink.setText("Lỗi mạng khi tải invite link");
                tvStatus.setText(t.getMessage() != null ? t.getMessage() : "unknown");
            }
        });
    }

    private void loadQr() {
        if (qrLoading) return;
        qrLoading = true;
        btnQr.setEnabled(false);
        tvStatus.setText("Đang tải mã QR...");
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getTripInviteQr(tripId).enqueue(new Callback<InviteQrResponse>() {
            @Override
            public void onResponse(@NonNull Call<InviteQrResponse> call, @NonNull Response<InviteQrResponse> response) {
                qrLoading = false;
                btnQr.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    tvStatus.setText("Không tải được QR");
                    return;
                }
                String base64 = response.body().getQrCode();
                if (base64 != null && !base64.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ivQr.setImageBitmap(bmp);
                        ivQr.setVisibility(View.VISIBLE);
                        qrLoaded = true;
                    } catch (Exception e) {
                        tvStatus.setText("QR không hợp lệ");
                    }
                }
                tvStatus.setText("Sẵn sàng để mời bạn bè");
            }

            @Override
            public void onFailure(@NonNull Call<InviteQrResponse> call, @NonNull Throwable t) {
                qrLoading = false;
                btnQr.setEnabled(true);
                tvStatus.setText("Lỗi mạng khi tải QR");
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
        if (qrLoaded) {
            ivQr.setVisibility(ivQr.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            tvStatus.setText(ivQr.getVisibility() == View.VISIBLE ? "Đã hiển thị mã QR" : "Đã ẩn mã QR");
            return;
        }
        loadQr();
    }

    private void inviteByEmail() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return;
        }
        btnEmail.setEnabled(false);
        tvStatus.setText("Đang gửi lời mời...");
        AddParticipantRequest request = new AddParticipantRequest(null, email, null, true);
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.addTripParticipant(tripId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                btnEmail.setEnabled(true);
                if (!response.isSuccessful()) {
                    String details = "";
                    try {
                        okhttp3.ResponseBody eb = response.errorBody();
                        if (eb != null) details = eb.string();
                    } catch (Exception ignored) { }
                    tvStatus.setText("Gửi mời thất bại: " + response.code() + (details.isEmpty() ? "" : " · " + details));
                    return;
                }
                etEmail.setText("");
                tvStatus.setText("Đã gửi lời mời thành công");
                Toast.makeText(requireContext(), "Đã mời người dùng vào chuyến đi", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                btnEmail.setEnabled(true);
                tvStatus.setText("Lỗi mạng khi gửi mời: " + (t.getMessage() != null ? t.getMessage() : "unknown"));
            }
        });
    }

}
