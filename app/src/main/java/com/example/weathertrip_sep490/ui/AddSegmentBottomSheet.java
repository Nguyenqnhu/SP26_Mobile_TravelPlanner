package com.example.weathertrip_sep490.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.UserAPI;
import com.example.weathertrip_sep490.model.AddSegmentRequest;
import com.example.weathertrip_sep490.model.DistrictOption;
import com.example.weathertrip_sep490.model.LocationOption;
import com.example.weathertrip_sep490.model.TripSegmentResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddSegmentBottomSheet extends BottomSheetDialogFragment {
    private static final String TAG = "AddSegmentBottomSheet";

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
    /** Số chặng hiện có (từ GET planner). Giá trị âm: chưa biết, bỏ qua kiểm tra insertAt tối đa. */
    private static final String ARG_CURRENT_SEGMENT_COUNT = "arg_current_segment_count";

    private Listener listener;
    private Spinner spLocations;
    private Spinner spDistrict;
    private EditText etInsertAt;
    private TextView tvStartDate;
    private TextView tvEndDate;

    private final List<LocationOption> allLocations = new ArrayList<>();
    private final List<String> districtLabels = new ArrayList<>();
    private final List<String> districtIds = new ArrayList<>();
    private ArrayAdapter<String> districtAdapter;
    private String selectedDistrictId;
    private String selectedLocationId;
    private final Map<String, List<DistrictOption>> districtCacheByLocationId = new HashMap<>();
    /** Biên ngày của trip (chỉ đọc — dùng cho DatePicker min/max). */
    private final Calendar tripStartBound = Calendar.getInstance();
    private final Calendar tripEndBound = Calendar.getInstance();
    /** Ngày bắt đầu / kết thúc của chặng đang thêm. */
    private final Calendar segStartCal = Calendar.getInstance();
    private final Calendar segEndCal = Calendar.getInstance();
    private boolean isSubmitting = false;
    private int currentSegmentCount = -1;

    public static AddSegmentBottomSheet newInstance(
            @NonNull String tripId,
            @NonNull String tripTitle,
            @NonNull String startIso,
            @NonNull String endIso
    ) {
        return newInstance(tripId, tripTitle, startIso, endIso, -1);
    }

    public static AddSegmentBottomSheet newInstance(
            @NonNull String tripId,
            @NonNull String tripTitle,
            @NonNull String startIso,
            @NonNull String endIso,
            int currentSegmentCount
    ) {
        AddSegmentBottomSheet f = new AddSegmentBottomSheet();
        Bundle b = new Bundle();
        b.putString(ARG_TRIP_ID, tripId);
        b.putString(ARG_TRIP_TITLE, tripTitle);
        b.putString(ARG_START_ISO, startIso);
        b.putString(ARG_END_ISO, endIso);
        b.putInt(ARG_CURRENT_SEGMENT_COUNT, currentSegmentCount);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof Listener) {
            listener = (Listener) getParentFragment();
        } else if (context instanceof Listener) {
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
        spDistrict = view.findViewById(R.id.spinnerSegmentDistrict);
        etInsertAt = view.findViewById(R.id.etSegmentInsertAt);
        tvStartDate = view.findViewById(R.id.tvSegmentStartDate);
        tvEndDate = view.findViewById(R.id.tvSegmentEndDate);
        TextView tvTitle = view.findViewById(R.id.tvAddSegmentTripTitle);

        String tripTitle = getArguments() != null ? getArguments().getString(ARG_TRIP_TITLE, "") : "";
        tvTitle.setText("Trip: " + tripTitle);

        String startIso = getArguments() != null ? getArguments().getString(ARG_START_ISO, "") : "";
        String endIso = getArguments() != null ? getArguments().getString(ARG_END_ISO, "") : "";
        currentSegmentCount = getArguments() != null ? getArguments().getInt(ARG_CURRENT_SEGMENT_COUNT, -1) : -1;

        parseIsoToCalendar(startIso, tripStartBound);
        parseIsoToCalendar(endIso, tripEndBound);
        if (tripEndBound.before(tripStartBound)) {
            tripEndBound.setTime(tripStartBound.getTime());
        }
        // Mặc định: chặng mới = 1 ngày (ngày đầu trip), tránh mặc định trùng cả khoảng trip dễ chồng ngày với các chặng đã tách.
        segStartCal.setTime(tripStartBound.getTime());
        segEndCal.setTime(tripStartBound.getTime());
        refreshDateLabels();

        if (currentSegmentCount >= 0) {
            etInsertAt.setHint("1 - " + (currentSegmentCount + 1));
        }

        view.findViewById(R.id.btnCloseAddSegment).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.rowSegmentStartDate).setOnClickListener(v -> showDatePicker(true));
        view.findViewById(R.id.rowSegmentEndDate).setOnClickListener(v -> showDatePicker(false));
        view.findViewById(R.id.btnAddSegment).setOnClickListener(v -> submit());

        setupDistrictSpinner();
        loadLocations();
    }

    private void setupDistrictSpinner() {
        districtAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                districtLabels
        );
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDistrict.setAdapter(districtAdapter);
        spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= districtIds.size()) {
                    selectedDistrictId = null;
                    return;
                }
                selectedDistrictId = districtIds.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDistrictId = null;
            }
        });
        resetDistrictSpinner();
    }

    private void resetDistrictSpinner() {
        districtLabels.clear();
        districtIds.clear();
        districtLabels.add("Chọn quận/huyện");
        districtIds.add(null);
        districtAdapter.notifyDataSetChanged();
        spDistrict.setSelection(0, false);
        selectedDistrictId = null;
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

        spLocations.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                LocationOption selected = (LocationOption) spLocations.getSelectedItem();
                selectedLocationId = selected != null ? selected.getLocationId() : null;
                resetDistrictSpinner();

                // Auto-load districts for the selected location so the dropdown is always ready.
                if (selectedLocationId != null && !selectedLocationId.trim().isEmpty()) {
                    fetchDistrictsForLocation(selectedLocationId.trim());
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Spinner không luôn gọi onItemSelected cho mục mặc định — load district cho location đầu tiên.
        spLocations.post(() -> {
            LocationOption selected = (LocationOption) spLocations.getSelectedItem();
            if (selected == null || selected.getLocationId() == null) return;
            selectedLocationId = selected.getLocationId().trim();
            if (selectedLocationId.isEmpty()) return;
            fetchDistrictsForLocation(selectedLocationId);
        });
    }

    private void fetchDistrictsForLocation(@NonNull String locationId) {
        String key = locationId.trim();
        if (districtCacheByLocationId.containsKey(key)) {
            applyDistricts(districtCacheByLocationId.get(key));
            return;
        }
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        Log.d(TAG, "fetchDistricts locationId=" + key);
        api.getDistrictsByLocation(key).enqueue(new Callback<List<DistrictOption>>() {
            @Override
            public void onResponse(@NonNull Call<List<DistrictOption>> call, @NonNull Response<List<DistrictOption>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    String details = "";
                    try {
                        ResponseBody eb = response.errorBody();
                        if (eb != null) details = eb.string();
                    } catch (Exception ignored) {}
                    Log.e(TAG, "getDistricts failed code=" + response.code() + " body=" + details);
                    Toast.makeText(requireContext(), "Không tải được danh sách quận/huyện (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                districtCacheByLocationId.put(key, response.body());
                applyDistricts(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<DistrictOption>> call, @NonNull Throwable t) {
                Log.e(TAG, "getDistricts network error", t);
                Toast.makeText(requireContext(), "Lỗi mạng khi tải quận/huyện", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyDistricts(@Nullable List<DistrictOption> districts) {
        districtLabels.clear();
        districtIds.clear();
        districtLabels.add("Chọn quận/huyện");
        districtIds.add(null);
        if (districts != null) {
            for (DistrictOption d : districts) {
                if (d == null || d.getName() == null || d.getId() == null) continue;
                String name = d.getName().trim();
                String id = d.getId().trim();
                if (name.isEmpty() || id.isEmpty()) continue;
                districtLabels.add(name);
                districtIds.add(id);
            }
        }
        districtAdapter.notifyDataSetChanged();
        spDistrict.setSelection(0, false);
        selectedDistrictId = null;
        if (districtLabels.size() <= 1) {
            Toast.makeText(requireContext(), "Không có quận/huyện cho location này", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = isStart ? segStartCal : segEndCal;
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (picker, year, month, dayOfMonth) -> {
                    cal.set(year, month, dayOfMonth, 0, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    if (segEndCal.before(segStartCal)) {
                        segEndCal.setTime(segStartCal.getTime());
                    }
                    refreshDateLabels();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        android.widget.DatePicker dp = dialog.getDatePicker();
        dp.setMinDate(startOfDayMillis(isStart ? tripStartBound : segStartCal));
        dp.setMaxDate(startOfDayMillis(tripEndBound));
        dialog.show();
    }

    private void refreshDateLabels() {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvStartDate.setText(df.format(segStartCal.getTime()));
        tvEndDate.setText(df.format(segEndCal.getTime()));
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

        int distPos = spDistrict.getSelectedItemPosition();
        if (distPos < 1 || distPos >= districtIds.size()
                || districtIds.get(distPos) == null
                || districtIds.get(distPos).trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn quận/huyện", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedDistrictId = districtIds.get(distPos).trim();

        String insertAtRaw = etInsertAt.getText() != null ? etInsertAt.getText().toString().trim() : "";
        if (insertAtRaw.isEmpty()) {
            etInsertAt.setError("Nhập số chặng");
            return;
        }
        int insertAt;
        try {
            insertAt = Integer.parseInt(insertAtRaw);
            // BE TripSegmentService: 1 <= insertAt <= existing.Count + 1
            if (insertAt < 1) throw new IllegalArgumentException();
            if (currentSegmentCount >= 0 && insertAt > currentSegmentCount + 1) {
                etInsertAt.setError("Tối đa " + (currentSegmentCount + 1) + " (sau chặng cuối)");
                return;
            }
        } catch (Exception ex) {
            etInsertAt.setError("Nhập vị trí từ 1 đến " + (currentSegmentCount >= 0 ? String.valueOf(currentSegmentCount + 1) : "số chặng hiện tại + 1"));
            return;
        }

        LocationOption selected = (LocationOption) spLocations.getSelectedItem();
        String start = toApiDateOnly(segStartCal);
        String end = toApiDateOnly(segEndCal);
        List<AddSegmentRequest> req = new ArrayList<>();
        req.add(new AddSegmentRequest(selected.getLocationId(), selectedDistrictId, start, end));


        isSubmitting = true;
        UserAPI api = RetrofitClient.getInstance().getUserAPI();
        api.addTripSegments(tripId, insertAt, req).enqueue(new Callback<List<TripSegmentResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<TripSegmentResponse>> call, @NonNull Response<List<TripSegmentResponse>> response) {
                isSubmitting = false;
                if (response.isSuccessful()) {
                    Double distanceKm = null;
                    List<TripSegmentResponse> body = response.body();
                    if (body != null && !body.isEmpty()) {
                        distanceKm = body.get(0).getDistanceKm();
                    }
                    Toast.makeText(requireContext(), distanceKm != null ? "Đã add segment · " + String.format(Locale.getDefault(), "%.1f km", distanceKm) : "Đã add segment", Toast.LENGTH_SHORT).show();
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
                Log.e(TAG, "addTripSegments failed code=" + response.code() + " body=" + details);
                Toast.makeText(
                        requireContext(),
                        buildAddSegmentErrorMessage(response.code(), details),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(@NonNull Call<List<TripSegmentResponse>> call, @NonNull Throwable t) {
                isSubmitting = false;
                String rawMessage = t.getMessage() != null ? t.getMessage().trim() : "";
                boolean looksLikeDateParse = rawMessage.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z?");
                String msg;
                if (looksLikeDateParse) {
                    msg = "Lỗi parse dữ liệu ngày từ server, vui lòng thử lại";
                } else {
                    msg = "Lỗi mạng: " + (rawMessage.isEmpty() ? "unknown" : rawMessage);
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void parseIsoToCalendar(@NonNull String iso, @NonNull Calendar out) {
        if (iso.trim().isEmpty()) return;
        String[] patterns = new String[]{
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(pattern, Locale.US);
                f.setLenient(false);
                Date parsed = f.parse(iso.trim());
                if (parsed == null) continue;
                out.setTime(parsed);
                out.set(Calendar.HOUR_OF_DAY, 0);
                out.set(Calendar.MINUTE, 0);
                out.set(Calendar.SECOND, 0);
                out.set(Calendar.MILLISECOND, 0);
                return;
            } catch (Exception ignored) {
            }
        }
    }

    /** Gửi date-only để tránh lệch múi giờ khi BE parse DateTime (giảm risk start/end đảo ngày). */
    private static String toApiDateOnly(@NonNull Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        f.setLenient(false);
        return f.format(c.getTime());
    }

    private static long startOfDayMillis(@NonNull Calendar c) {
        Calendar x = (Calendar) c.clone();
        x.set(Calendar.HOUR_OF_DAY, 0);
        x.set(Calendar.MINUTE, 0);
        x.set(Calendar.SECOND, 0);
        x.set(Calendar.MILLISECOND, 0);
        return x.getTimeInMillis();
    }

    @NonNull
    private static String buildAddSegmentErrorMessage(int code, @Nullable String details) {
        String raw = details == null ? "" : details.trim();
        if (raw.contains("Response status code does not indicate success: 422")) {
            return "Không thể thêm chặng: server lỗi tính quãng đường (Mapbox 422). "
                    + "Đây là lỗi backend format tọa độ, không phải do bạn nhập.";
        }
        if (raw.isEmpty()) {
            return "Không thể thêm chặng (" + code + ")";
        }
        return "Không thể thêm chặng (" + code + "): " + raw;
    }
}

