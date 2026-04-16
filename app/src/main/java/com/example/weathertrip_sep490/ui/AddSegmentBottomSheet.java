package com.example.weathertrip_sep490.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.AddSegmentRequest;
import com.example.weathertrip_sep490.model.LocationOption;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddSegmentBottomSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onSegmentAddedAndReadyForAi(
                @NonNull String tripId,
                @NonNull String locationName,
                @NonNull String startDate,
                @NonNull String endDate,
                double latitude,
                double longitude
        );
    }

    private static final String ARG_TRIP_ID = "arg_trip_id";
    private static final String ARG_TRIP_TITLE = "arg_trip_title";
    private static final String ARG_START_ISO = "arg_start_iso";
    private static final String ARG_END_ISO = "arg_end_iso";

    private Listener listener;
    private Spinner spLocations;
    private EditText etInsertAt;
    private TextView tvStartDate;
    private TextView tvEndDate;

    private final List<LocationOption> allLocations = new ArrayList<>();
    private final Calendar startCal = Calendar.getInstance();
    private final Calendar endCal = Calendar.getInstance();
    private boolean isSubmitting = false;

    public static AddSegmentBottomSheet newInstance(
            @NonNull String tripId,
            @NonNull String tripTitle,
            @NonNull String startIso,
            @NonNull String endIso
    ) {
        AddSegmentBottomSheet f = new AddSegmentBottomSheet();
        Bundle b = new Bundle();
        b.putString(ARG_TRIP_ID, tripId);
        b.putString(ARG_TRIP_TITLE, tripTitle);
        b.putString(ARG_START_ISO, startIso);
        b.putString(ARG_END_ISO, endIso);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_segment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spLocations = view.findViewById(R.id.spinnerSegmentLocation);
        etInsertAt = view.findViewById(R.id.etSegmentInsertAt);
        tvStartDate = view.findViewById(R.id.tvSegmentStartDate);
        tvEndDate = view.findViewById(R.id.tvSegmentEndDate);
        TextView tvTitle = view.findViewById(R.id.tvAddSegmentTripTitle);

        String tripTitle = getArguments() != null ? getArguments().getString(ARG_TRIP_TITLE, "") : "";
        tvTitle.setText("Trip: " + tripTitle);

        String startIso = getArguments() != null ? getArguments().getString(ARG_START_ISO, "") : "";
        String endIso = getArguments() != null ? getArguments().getString(ARG_END_ISO, "") : "";
        parseIsoToCalendar(startIso, startCal);
        parseIsoToCalendar(endIso, endCal);
        refreshDateLabels();

        view.findViewById(R.id.btnCloseAddSegment).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.rowSegmentStartDate).setOnClickListener(v -> showDatePicker(true));
        view.findViewById(R.id.rowSegmentEndDate).setOnClickListener(v -> showDatePicker(false));
        view.findViewById(R.id.btnAddSegment).setOnClickListener(v -> submit());

        loadLocations();
    }

    private void loadLocations() {
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.getAllLocations().enqueue(new Callback<List<LocationOption>>() {
            @Override
            public void onResponse(@NonNull Call<List<LocationOption>> call, @NonNull Response<List<LocationOption>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), "Không tải được danh sách location", Toast.LENGTH_SHORT).show();
                    bindLocationSpinner(Collections.emptyList());
                    return;
                }
                allLocations.clear();
                allLocations.addAll(response.body());
                bindLocationSpinner(allLocations);
            }

            @Override
            public void onFailure(@NonNull Call<List<LocationOption>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng khi tải location", Toast.LENGTH_SHORT).show();
                bindLocationSpinner(Collections.emptyList());
            }
        });
    }

    private void bindLocationSpinner(@NonNull List<LocationOption> items) {
        ArrayAdapter<LocationOption> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLocations.setAdapter(adapter);
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = isStart ? startCal : endCal;
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (picker, year, month, dayOfMonth) -> {
                    cal.set(year, month, dayOfMonth, 0, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    if (endCal.before(startCal)) {
                        endCal.setTime(startCal.getTime());
                    }
                    refreshDateLabels();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void refreshDateLabels() {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvStartDate.setText(df.format(startCal.getTime()));
        tvEndDate.setText(df.format(endCal.getTime()));
        tvStartDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_800));
        tvEndDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_800));
    }

    private void submit() {
        if (isSubmitting) return;

        String tripId = getArguments() != null ? getArguments().getString(ARG_TRIP_ID, "") : "";
        if (TextUtils.isEmpty(tripId)) {
            Toast.makeText(requireContext(), "Thiếu tripId để add segment", Toast.LENGTH_SHORT).show();
            return;
        }
        if (allLocations.isEmpty() || spLocations.getSelectedItem() == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn location", Toast.LENGTH_SHORT).show();
            return;
        }

        String insertAtRaw = etInsertAt.getText() != null ? etInsertAt.getText().toString().trim() : "";
        if (insertAtRaw.isEmpty()) {
            etInsertAt.setError("Nhập số chặng");
            return;
        }
        int insertAt;
        try {
            insertAt = Integer.parseInt(insertAtRaw);
            if (insertAt < 0) throw new IllegalArgumentException();
        } catch (Exception ex) {
            etInsertAt.setError("insertAt phải là số >= 0");
            return;
        }

        LocationOption selected = (LocationOption) spLocations.getSelectedItem();
        String start = toDateOnly(startCal);
        String end = toDateOnly(endCal);
        List<AddSegmentRequest> req = new ArrayList<>();
        req.add(new AddSegmentRequest(selected.getLocationId(), start, end));

        isSubmitting = true;
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.addTripSegments(tripId, insertAt, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                isSubmitting = false;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đã add segment. AI đang tạo itinerary...", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onSegmentAddedAndReadyForAi(
                                tripId,
                                selected.getLocationName() != null ? selected.getLocationName() : "",
                                start,
                                end,
                                selected.getLatitude(),
                                selected.getLongitude()
                        );
                    }
                    dismiss();
                    return;
                }
                String details = "";
                try {
                    ResponseBody eb = response.errorBody();
                    if (eb != null) details = eb.string();
                } catch (Exception ignored) {}

                // BE hiện có case đã insert DB thành công nhưng fail ở bước map response DTO
                // -> trả 400 "Error mapping types". Với case này coi như add segment thành công logic.
                if (response.code() == 400 && looksLikeResponseMappingFailure(details)) {
                    Toast.makeText(
                            requireContext(),
                            "Segment đã được lưu (BE lỗi mapping response). Tiếp tục tạo itinerary...",
                            Toast.LENGTH_LONG
                    ).show();
                    if (listener != null) {
                        listener.onSegmentAddedAndReadyForAi(
                                tripId,
                                selected.getLocationName() != null ? selected.getLocationName() : "",
                                start,
                                end,
                                selected.getLatitude(),
                                selected.getLongitude()
                        );
                    }
                    dismiss();
                    return;
                }

                Toast.makeText(
                        requireContext(),
                        "Add segment thất bại (" + response.code() + ")" + (details.isEmpty() ? "" : ": " + details),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                isSubmitting = false;
                Toast.makeText(requireContext(), "Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void parseIsoToCalendar(@NonNull String iso, @NonNull Calendar out) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            out.setTime(f.parse(iso));
            out.set(Calendar.HOUR_OF_DAY, 0);
            out.set(Calendar.MINUTE, 0);
            out.set(Calendar.SECOND, 0);
            out.set(Calendar.MILLISECOND, 0);
        } catch (Exception ignored) { }
    }

    private static String toDateOnly(@NonNull Calendar calendar) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(calendar.getTime());
    }

    private static boolean looksLikeResponseMappingFailure(@Nullable String details) {
        if (details == null) return false;
        String d = details.toLowerCase(Locale.US);
        return d.contains("error mapping types")
                || d.contains("automapper")
                || d.contains("missing type map configuration");
    }
}

