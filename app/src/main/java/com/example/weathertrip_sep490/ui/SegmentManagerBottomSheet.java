package com.example.weathertrip_sep490.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.PlannerGenerateResponse;
import com.example.weathertrip_sep490.model.PlannerTripResponse;
import com.example.weathertrip_sep490.model.TripSegmentResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SegmentManagerBottomSheet extends BottomSheetDialogFragment implements AddSegmentBottomSheet.Listener {

    public interface Listener {
        void onGenerateCompleted(@NonNull String tripId);
    }

    private static final String ARG_TRIP_ID = "arg_trip_id";
    private static final String ARG_TRIP_TITLE = "arg_trip_title";
    private static final String ARG_START_POINT = "arg_start_point";
    private static final String ARG_DESTINATION = "arg_destination";
    private static final String ARG_START_ISO = "arg_start_iso";
    private static final String ARG_END_ISO = "arg_end_iso";

    private String tripId;
    private String tripTitle;
    private String startPoint;
    private String destination;
    private String startIso;
    private String endIso;

    private LinearLayout containerSegmentList;
    private LinearLayout layoutLoading;
    private TextView tvSubtitle;
    private MaterialButton btnAdd;
    private MaterialButton btnSave;
    private MaterialButton btnGenerate;

    private Listener listener;
    private int lastKnownSegmentCount = 0;

    public static SegmentManagerBottomSheet newInstance(
            @NonNull String tripId,
            @NonNull String tripTitle,
            @NonNull String startPoint,
            @NonNull String destination,
            @NonNull String startIso,
            @NonNull String endIso
    ) {
        SegmentManagerBottomSheet f = new SegmentManagerBottomSheet();
        Bundle b = new Bundle();
        b.putString(ARG_TRIP_ID, tripId);
        b.putString(ARG_TRIP_TITLE, tripTitle);
        b.putString(ARG_START_POINT, startPoint);
        b.putString(ARG_DESTINATION, destination);
        b.putString(ARG_START_ISO, startIso);
        b.putString(ARG_END_ISO, endIso);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof Listener) listener = (Listener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_segment_manager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tripId = getArguments() != null ? getArguments().getString(ARG_TRIP_ID, "") : "";
        tripTitle = getArguments() != null ? getArguments().getString(ARG_TRIP_TITLE, "") : "";
        startPoint = getArguments() != null ? getArguments().getString(ARG_START_POINT, "") : "";
        destination = getArguments() != null ? getArguments().getString(ARG_DESTINATION, "") : "";
        startIso = getArguments() != null ? getArguments().getString(ARG_START_ISO, "") : "";
        endIso = getArguments() != null ? getArguments().getString(ARG_END_ISO, "") : "";

        tvSubtitle = view.findViewById(R.id.tvSegmentManagerTitle);
        containerSegmentList = view.findViewById(R.id.containerSegmentList);
        layoutLoading = view.findViewById(R.id.layoutSegmentLoading);
        btnAdd = view.findViewById(R.id.btnSegmentManagerAdd);
        btnSave = view.findViewById(R.id.btnSegmentManagerSave);
        btnGenerate = view.findViewById(R.id.btnSegmentManagerGenerate);

        tvSubtitle.setText(tripTitle + " · " + startPoint + " → " + destination);

        btnAdd.setOnClickListener(v -> openAddSegment());
        btnSave.setOnClickListener(v -> Toast.makeText(requireContext(), "Đã lưu segment", Toast.LENGTH_SHORT).show());
        btnGenerate.setOnClickListener(v -> generateAi());

        renderIntroState();
        reloadPlanner();
    }

    private void openAddSegment() {
        if (TextUtils.isEmpty(tripId)) {
            Toast.makeText(requireContext(), "Thiếu tripId", Toast.LENGTH_SHORT).show();
            return;
        }
        AddSegmentBottomSheet sheet = AddSegmentBottomSheet.newInstance(tripId, tripTitle, startIso, endIso);
        sheet.show(getChildFragmentManager(), "AddSegmentBottomSheet");
    }

    private void renderIntroState() {
        showLoading(false);
        containerSegmentList.removeAllViews();
        lastKnownSegmentCount = 0;

        TextView tv = new TextView(requireContext());
        tv.setText(startPoint + " → " + destination);
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_800));
        tv.setTextSize(16f);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setPadding(8, 12, 8, 12);
        containerSegmentList.addView(tv);
    }

    private void reloadPlanner() {
        if (TextUtils.isEmpty(tripId)) return;
        showLoading(true);
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getPlanner(tripId).enqueue(new Callback<PlannerTripResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlannerTripResponse> call, @NonNull Response<PlannerTripResponse> response) {
                showLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    renderIntroState();
                    return;
                }
                renderSegments(response.body().getSegments());
            }

            @Override
            public void onFailure(@NonNull Call<PlannerTripResponse> call, @NonNull Throwable t) {
                showLoading(false);
                renderIntroState();
            }
        });
    }

    private void renderSegments(@Nullable List<TripSegmentResponse> segments) {
        containerSegmentList.removeAllViews();
        if (segments == null || segments.isEmpty()) {
            renderIntroState();
            return;
        }

        int index = 1;
        for (TripSegmentResponse seg : segments) {
            if (seg == null) continue;
            String range = formatDate(seg.getStartDate()) + " - " + formatDate(seg.getEndDate());
            String km = seg.getDistanceKm() != null ? (" · " + String.format(Locale.getDefault(), "%.1f km", seg.getDistanceKm())) : "";
            addRow(index++, range + km, "Segment đã lưu · order=" + seg.getOrderIndex());
        }
        lastKnownSegmentCount = index - 1;
    }

    private void addRow(int number, String subtitle, String note) {
        MaterialButton card = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) card.getLayoutParams();
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);
        card.setCornerRadius(18);
        card.setAllCaps(false);
        card.setStrokeWidth(1);
        card.setStrokeColorResource(R.color.green_primary);
        card.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
        card.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_800));
        card.setTextSize(14f);
        card.setPadding(18, 16, 18, 16);
        card.setGravity(android.view.Gravity.START);
        card.setText(number + ". " + subtitle + "\n" + note);
        containerSegmentList.addView(card);
    }

    private String formatDate(@Nullable Date date) {
        if (date == null) return "--";
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    private void generateAi() {
        if (TextUtils.isEmpty(tripId)) return;
        if (lastKnownSegmentCount < 2) {
            Toast.makeText(requireContext(), "Cần tối thiểu 2 segment để tạo lịch trình AI", Toast.LENGTH_LONG).show();
            return;
        }
        btnGenerate.setEnabled(false);
        showLoading(true);
        android.util.Log.d("SegmentManagerBottomSheet", "generate tripId=" + tripId);
        Toast.makeText(requireContext(), "Đang tạo lịch trình chi tiết...", Toast.LENGTH_SHORT).show();
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.generatePlanner(tripId).enqueue(new Callback<PlannerGenerateResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlannerGenerateResponse> call, @NonNull Response<PlannerGenerateResponse> response) {
                btnGenerate.setEnabled(true);
                showLoading(false);
                if (!response.isSuccessful()) {
                    String details = "";
                    try {
                        okhttp3.ResponseBody eb = response.errorBody();
                        if (eb != null) details = eb.string();
                    } catch (Exception ignored) {}
                    android.util.Log.e("SegmentManagerBottomSheet", "generate failed code=" + response.code() + " body=" + details + " tripId=" + tripId);
                    Toast.makeText(requireContext(), buildGenerateErrorMessage(response.code(), details), Toast.LENGTH_LONG).show();
                    return;
                }
                PlannerGenerateResponse body = response.body();
                String generatedTripId = body != null && body.getTripId() != null && !body.getTripId().trim().isEmpty()
                        ? body.getTripId().trim()
                        : tripId;
                Toast.makeText(requireContext(), "AI đã tạo lịch trình thành công", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onGenerateCompleted(generatedTripId);
                dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<PlannerGenerateResponse> call, @NonNull Throwable t) {
                btnGenerate.setEnabled(true);
                showLoading(false);
                android.util.Log.e("SegmentManagerBottomSheet", "generate network fail tripId=" + tripId + " msg=" + t.getMessage(), t);
                Toast.makeText(requireContext(), "Lỗi mạng khi generate: " + (t.getMessage() != null ? t.getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onSegmentAddedAndReadyForAi(@NonNull String tripId, @NonNull String locationName, @NonNull String segmentStartDate, @NonNull String segmentEndDate, double latitude, double longitude) {
        reloadPlanner();
    }

    @NonNull
    private String buildGenerateErrorMessage(int code, @Nullable String rawError) {
        if (rawError == null || rawError.trim().isEmpty()) {
            return "Generate thất bại (" + code + ")";
        }
        try {
            JSONObject obj = new JSONObject(rawError);
            String message = obj.optString("message", "").trim();
            String detail = obj.optString("detail", "").trim();
            if (!detail.isEmpty()) {
                return "Generate thất bại (" + code + "): " + detail;
            }
            if (!message.isEmpty()) {
                return "Generate thất bại (" + code + "): " + message;
            }
        } catch (Exception ignored) {
        }
        return "Generate thất bại (" + code + "): " + rawError;
    }
}
