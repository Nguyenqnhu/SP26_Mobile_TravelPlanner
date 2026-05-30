package com.example.weathertrip_sep490.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.model.DistrictOption;
import com.example.weathertrip_sep490.model.LocationOption;
import com.example.weathertrip_sep490.model.TripCreateRequest;
import com.example.weathertrip_sep490.model.TripResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.text.Normalizer;
import java.util.Map;
import java.util.HashMap;
import android.text.Editable;
import android.text.TextWatcher;

import okhttp3.ResponseBody;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTripBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "CreateTripBottomSheet";

    /** index 0 = 1 chiều, 1 = khứ hồi */
    public static final int TRIP_TYPE_ONE_WAY = 0;

    public interface Listener {
        void onTripCreated(
                @NonNull String tripId,
                @NonNull String tripTitle,
                @NonNull String startPoint,
                @NonNull String destination,
                boolean roundTrip,
                @NonNull String startDateDisplay,
                @NonNull String endDateDisplay,
                @NonNull String startDateIso,
                @NonNull String endDateIso
        );
    }

    private Listener listener;

    private EditText etTripTitle;
    private AutoCompleteTextView etStartPoint;
    private Spinner spStartDistrict;
    private AutoCompleteTextView etDestination;
    private Spinner spEndDistrict;
    private View layoutColumnReturnDate;
    private TextView tvStartDate;
    private TextView tvEndDate;

    private Calendar startCal;
    private Calendar endCal;

    private boolean isSubmitting = false;
    private final List<String> locationItems = new java.util.ArrayList<>();
    private final Set<String> locationSet = new HashSet<>();
    private boolean hasLoadedLocations = false;
    private final List<LocationOption> locationOptions = new java.util.ArrayList<>();
    private final Map<String, String> locationNameToId = new HashMap<>();

    private String selectedStartLocationId;
    private String selectedEndLocationId;
    private String selectedStartDistrictId;
    private String selectedEndDistrictId;

    private final Map<String, List<DistrictOption>> districtCacheByLocationId = new HashMap<>();
    private final List<String> startDistrictLabels = new java.util.ArrayList<>();
    private final List<String> startDistrictIds = new java.util.ArrayList<>();
    private final List<String> endDistrictLabels = new java.util.ArrayList<>();
    private final List<String> endDistrictIds = new java.util.ArrayList<>();
    private ArrayAdapter<String> startDistrictAdapter;
    private ArrayAdapter<String> endDistrictAdapter;

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
        return inflater.inflate(R.layout.bottom_sheet_create_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTripTitle = view.findViewById(R.id.etCreateTripTitle);
        etStartPoint = view.findViewById(R.id.etCreateTripStartPoint);
        spStartDistrict = view.findViewById(R.id.spinnerCreateTripStartDistrict);
        etDestination = view.findViewById(R.id.etCreateTripDestination);
        spEndDistrict = view.findViewById(R.id.spinnerCreateTripEndDistrict);
        layoutColumnReturnDate = view.findViewById(R.id.layoutColumnReturnDate);
        tvStartDate = view.findViewById(R.id.tvCreateTripStartDate);
        tvEndDate = view.findViewById(R.id.tvCreateTripEndDate);

        View rowStart = view.findViewById(R.id.rowStartDate);
        View rowEnd = view.findViewById(R.id.rowEndDate);

        startCal = Calendar.getInstance();
        endCal = Calendar.getInstance();
        endCal.add(Calendar.DAY_OF_MONTH, 3);

        // Enable end date selection to match 4 API parameters requirement
        applyTripTypeUi(true);

        view.findViewById(R.id.btnCloseCreateTrip).setOnClickListener(v -> dismiss());

        rowStart.setOnClickListener(v -> showDatePicker(true));
        rowEnd.setOnClickListener(v -> {
            showDatePicker(false);
        });

        view.findViewById(R.id.btnCreateTripWithAi).setOnClickListener(v -> submit());

        setupLocationDropdowns();
        setupDistrictSpinners();
        fetchLocations();
        refreshDateLabels();
    }

    private void setupLocationDropdowns() {
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                locationItems
        );
        etStartPoint.setAdapter(locationAdapter);
        etDestination.setAdapter(locationAdapter);
        // 0 = cho phép xổ dropdown ngay cả khi chưa gõ ký tự (AutoCompleteTextView mặc định threshold=2)
        etStartPoint.setThreshold(0);
        etDestination.setThreshold(0);

        etStartPoint.setOnClickListener(v -> etStartPoint.showDropDown());
        etDestination.setOnClickListener(v -> etDestination.showDropDown());
        etStartPoint.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etStartPoint.showDropDown();
        });
        etDestination.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etDestination.showDropDown();
        });

        etStartPoint.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            onStartLocationSelected(selectedName);
        });
        etDestination.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            onEndLocationSelected(selectedName);
        });

        // Nếu user gõ tay (không click item), vẫn cố gắng map sang canonical location
        // và reset district khi location thay đổi.
        etStartPoint.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                selectedStartLocationId = null;
                selectedStartDistrictId = null;
                resetStartDistrictSpinner();
            }
        });
        etDestination.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                selectedEndLocationId = null;
                selectedEndDistrictId = null;
                resetEndDistrictSpinner();
            }
        });
    }

    private void setupDistrictSpinners() {
        startDistrictAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                startDistrictLabels
        );
        startDistrictAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStartDistrict.setAdapter(startDistrictAdapter);

        endDistrictAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                endDistrictLabels
        );
        endDistrictAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEndDistrict.setAdapter(endDistrictAdapter);

        spStartDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= startDistrictIds.size()) {
                    selectedStartDistrictId = null;
                    return;
                }
                selectedStartDistrictId = startDistrictIds.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedStartDistrictId = null;
            }
        });
        spEndDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= endDistrictIds.size()) {
                    selectedEndDistrictId = null;
                    return;
                }
                selectedEndDistrictId = endDistrictIds.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedEndDistrictId = null;
            }
        });

        resetStartDistrictSpinner();
        resetEndDistrictSpinner();
    }

    private void resetStartDistrictSpinner() {
        startDistrictLabels.clear();
        startDistrictIds.clear();
        startDistrictLabels.add("Chọn quận/huyện");
        startDistrictIds.add(null);
        startDistrictAdapter.notifyDataSetChanged();
        spStartDistrict.setSelection(0, false);
        selectedStartDistrictId = null;
    }

    private void resetEndDistrictSpinner() {
        endDistrictLabels.clear();
        endDistrictIds.clear();
        endDistrictLabels.add("Chọn quận/huyện");
        endDistrictIds.add(null);
        endDistrictAdapter.notifyDataSetChanged();
        spEndDistrict.setSelection(0, false);
        selectedEndDistrictId = null;
    }

    private void fetchLocations() {
        RetrofitClient.getInstance().getUserAPI().getAllLocations().enqueue(new Callback<List<LocationOption>>() {
            @Override
            public void onResponse(@NonNull Call<List<LocationOption>> call, @NonNull Response<List<LocationOption>> response) {
                hasLoadedLocations = true;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), "Không tải được danh sách địa điểm", Toast.LENGTH_SHORT).show();
                    return;
                }
                locationItems.clear();
                locationSet.clear();
                locationOptions.clear();
                locationNameToId.clear();
                for (LocationOption option : response.body()) {
                    if (option == null || option.getLocationName() == null) {
                        continue;
                    }
                    String name = option.getLocationName().trim();
                    String locationId = option.getLocationId() != null ? option.getLocationId().trim() : "";
                    if (name.isEmpty() || locationSet.contains(name)) {
                        continue;
                    }
                    locationSet.add(name);
                    locationItems.add(name);
                    locationOptions.add(option);
                    if (!locationId.isEmpty()) {
                        locationNameToId.put(name, locationId);
                    }
                }
                if (etStartPoint.getAdapter() instanceof ArrayAdapter) {
                    ((ArrayAdapter<?>) etStartPoint.getAdapter()).notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LocationOption>> call, @NonNull Throwable t) {
                hasLoadedLocations = false;
                Toast.makeText(requireContext(), "Lỗi tải địa điểm: " + (t.getMessage() != null ? t.getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onStartLocationSelected(@NonNull String locationName) {
        selectedStartLocationId = locationNameToId.get(locationName);
        resetStartDistrictSpinner();
        if (selectedStartLocationId == null || selectedStartLocationId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "LocationId không hợp lệ cho điểm bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }
        fetchDistrictsForLocation(selectedStartLocationId, true);
    }

    private void onEndLocationSelected(@NonNull String locationName) {
        selectedEndLocationId = locationNameToId.get(locationName);
        resetEndDistrictSpinner();
        if (selectedEndLocationId == null || selectedEndLocationId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "LocationId không hợp lệ cho điểm đến", Toast.LENGTH_SHORT).show();
            return;
        }
        fetchDistrictsForLocation(selectedEndLocationId, false);
    }

    private void fetchDistrictsForLocation(@NonNull String locationId, boolean isStart) {
        String key = locationId.trim();
        if (districtCacheByLocationId.containsKey(key)) {
            applyDistricts(key, districtCacheByLocationId.get(key), isStart);
            return;
        }
        RetrofitClient.getInstance().getUserAPI().getDistrictsByLocation(key).enqueue(new Callback<List<DistrictOption>>() {
            @Override
            public void onResponse(@NonNull Call<List<DistrictOption>> call, @NonNull Response<List<DistrictOption>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), "Không tải được danh sách quận/huyện", Toast.LENGTH_SHORT).show();
                    return;
                }
                districtCacheByLocationId.put(key, response.body());
                applyDistricts(key, response.body(), isStart);
            }

            @Override
            public void onFailure(@NonNull Call<List<DistrictOption>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi tải quận/huyện: " + (t.getMessage() != null ? t.getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyDistricts(@NonNull String locationId, @Nullable List<DistrictOption> districts, boolean isStart) {
        List<String> labels = isStart ? startDistrictLabels : endDistrictLabels;
        List<String> ids = isStart ? startDistrictIds : endDistrictIds;
        Spinner sp = isStart ? spStartDistrict : spEndDistrict;
        ArrayAdapter<String> adapter = isStart ? startDistrictAdapter : endDistrictAdapter;

        labels.clear();
        ids.clear();
        labels.add("Chọn quận/huyện");
        ids.add(null);
        if (districts != null) {
            for (DistrictOption d : districts) {
                if (d == null || d.getName() == null || d.getId() == null) continue;
                String name = d.getName().trim();
                String id = d.getId().trim();
                if (name.isEmpty() || id.isEmpty()) continue;
                labels.add(name);
                ids.add(id);
            }
        }
        adapter.notifyDataSetChanged();
        sp.setSelection(0, false);
        if (isStart) {
            selectedStartDistrictId = null;
        } else {
            selectedEndDistrictId = null;
        }
        if (labels.size() <= 1) {
            Toast.makeText(requireContext(), "Không có quận/huyện cho location này", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyTripTypeUi(boolean roundTrip) {
        layoutColumnReturnDate.setVisibility(roundTrip ? View.VISIBLE : View.GONE);
    }

    private void refreshDateLabels() {
        java.text.DateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvStartDate.setText(df.format(startCal.getTime()));
        tvStartDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_800));
        tvEndDate.setText(df.format(endCal.getTime()));
        tvEndDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_800));
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = isStart ? startCal : endCal;
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (picker, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, dayOfMonth);
                    if (isStart) {
                        startCal = picked;
                        if (endCal.before(startCal)) {
                            endCal = (Calendar) startCal.clone();
                            endCal.add(Calendar.DAY_OF_MONTH, 1);
                        }
                    } else {
                        if (picked.before(startCal)) {
                            Toast.makeText(requireContext(), "Ngày về phải sau hoặc bằng ngày đi", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        endCal = picked;
                    }
                    refreshDateLabels();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void submit() {
        if (isSubmitting) return;
        String tripTitle = etTripTitle.getText() != null ? etTripTitle.getText().toString().trim() : "";
        String startPointInput = etStartPoint.getText() != null ? etStartPoint.getText().toString().trim() : "";
        String destinationInput = etDestination.getText() != null ? etDestination.getText().toString().trim() : "";
        if (tripTitle.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên chuyến đi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startPointInput.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập điểm bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destinationInput.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập điểm đến", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!hasLoadedLocations || locationItems.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa tải được danh sách địa điểm. Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
            return;
        }
        String startPoint = resolveCanonicalLocationName(startPointInput);
        String destination = resolveCanonicalLocationName(destinationInput);
        if (startPoint == null) {
            Toast.makeText(requireContext(), "Điểm bắt đầu không hợp lệ, vui lòng chọn từ danh sách", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination == null) {
            Toast.makeText(requireContext(), "Điểm đến không hợp lệ, vui lòng chọn từ danh sách", Toast.LENGTH_SHORT).show();
            return;
        }
        final String resolvedStartPoint = startPoint;
        final String resolvedDestination = destination;

        // Ensure locationId is derived from canonical name (BE requires district IDs by location)
        selectedStartLocationId = locationNameToId.get(resolvedStartPoint);
        selectedEndLocationId = locationNameToId.get(resolvedDestination);

        if (selectedStartLocationId == null || selectedStartLocationId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn điểm bắt đầu từ danh sách", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedEndLocationId == null || selectedEndLocationId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn điểm đến từ danh sách", Toast.LENGTH_SHORT).show();
            return;
        }

        int startDistPos = spStartDistrict.getSelectedItemPosition();
        if (startDistPos < 1 || startDistPos >= startDistrictIds.size()
                || startDistrictIds.get(startDistPos) == null
                || startDistrictIds.get(startDistPos).trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn quận/huyện điểm bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedStartDistrictId = startDistrictIds.get(startDistPos).trim();

        int endDistPos = spEndDistrict.getSelectedItemPosition();
        if (endDistPos < 1 || endDistPos >= endDistrictIds.size()
                || endDistrictIds.get(endDistPos) == null
                || endDistrictIds.get(endDistPos).trim().isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn quận/huyện điểm đến", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedEndDistrictId = endDistrictIds.get(endDistPos).trim();

        boolean roundTrip = false;
        java.text.DateFormat dfDisplay = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startStr = dfDisplay.format(startCal.getTime());
        String endStr = dfDisplay.format(endCal.getTime());

        // one-way only: endDate will be computed as startDate + 1 day for BE compatibility

        // Gửi DateTime theo ngày local đã chọn, tránh lệch ngày do đổi múi giờ UTC.
        // Gửi enum dạng string để tránh lệch mapping số giữa các bản BE deploy.
        String typeQuery = "OneWay";
        String startIso = toApiDateTime(startCal);
        String endIso = toApiDateTime(endCal);
        TripCreateRequest body = new TripCreateRequest(
                tripTitle,
                resolvedStartPoint,
                selectedStartDistrictId,
                resolvedDestination,
                selectedEndDistrictId,
                startIso,
                endIso
        );

        isSubmitting = true;
        Log.d(TAG, "createTrip type=" + typeQuery + " title=" + tripTitle
                + " startLocation=" + resolvedStartPoint + " endLocation=" + resolvedDestination
                + " startDate=" + startIso + " endDate=" + endIso);
        Call<TripResponse> call = RetrofitClient.getInstance().getUserAPI().createTrip(typeQuery, body);
        call.enqueue(new Callback<TripResponse>() {
            @Override
            public void onResponse(@NonNull Call<TripResponse> call, @NonNull Response<TripResponse> response) {
                isSubmitting = false;
                if (!response.isSuccessful()) {
                    String details = "";
                    try {
                        ResponseBody eb = response.errorBody();
                        if (eb != null) {
                            String raw = eb.string();
                            if (raw != null) {
                                details = raw.trim();
                            }
                        }
                    } catch (Exception ignored) {}
                    Log.e(TAG, "createTrip failed code=" + response.code() + " body=" + details);
                    Toast.makeText(
                            requireContext(),
                            "Tạo chuyến đi thất bại (" + response.code() + ")" + (details.isEmpty() ? "" : (": " + details)),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
                // Response body có thể null (ví dụ: 204/empty body)
                TripResponse resp = response.body();
                String createdTripId = resp != null ? resp.getTripId() : null;
                if (createdTripId == null || createdTripId.trim().isEmpty()) {
                    Toast.makeText(requireContext(), "Create trip thành công nhưng thiếu tripId", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(requireContext(), "Đã tạo chuyến đi", Toast.LENGTH_SHORT).show();

                // Save selected start and end district / location names to SharedPreferences
                try {
                    android.content.SharedPreferences prefs = requireContext().getSharedPreferences("TravelGoPrefs", android.content.Context.MODE_PRIVATE);
                    String startDist = spStartDistrict.getSelectedItem() != null ? spStartDistrict.getSelectedItem().toString() : "";
                    String endDist = spEndDistrict.getSelectedItem() != null ? spEndDistrict.getSelectedItem().toString() : "";
                    prefs.edit()
                         .putString("trip_start_location_" + createdTripId.trim(), resolvedStartPoint)
                         .putString("trip_start_district_" + createdTripId.trim(), startDist)
                         .putString("trip_end_location_" + createdTripId.trim(), resolvedDestination)
                         .putString("trip_end_district_" + createdTripId.trim(), endDist)
                         .putString("trip_start_date_iso_" + createdTripId.trim(), startIso)
                         .putString("trip_end_date_iso_" + createdTripId.trim(), endIso)
                         .apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (listener != null) {
                    listener.onTripCreated(
                            createdTripId.trim(),
                            tripTitle,
                            resolvedStartPoint,
                            resolvedDestination,
                            roundTrip,
                            startStr,
                            endStr,
                            startIso,
                            endIso
                    );
                }
                dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<TripResponse> call, @NonNull Throwable t) {
                isSubmitting = false;
                Toast.makeText(requireContext(), "Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : "unknown"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static String toApiDateTime(@NonNull Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        java.text.SimpleDateFormat apiFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        return apiFormat.format(c.getTime());
    }

    @Nullable
    private String resolveCanonicalLocationName(@NonNull String rawInput) {
        String input = rawInput.trim();
        if (input.isEmpty()) return null;
        if (locationSet.contains(input)) return input;
        String normalizedInput = normalizeForLookup(input);
        for (String candidate : locationItems) {
            if (candidate == null) continue;
            if (normalizeForLookup(candidate).equals(normalizedInput)) {
                return candidate;
            }
        }
        return null;
    }

    @NonNull
    private static String normalizeForLookup(@NonNull String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
