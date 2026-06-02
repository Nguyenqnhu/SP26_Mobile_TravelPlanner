package com.example.weathertrip_sep490.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.model.AddSegmentRequest;
import com.example.weathertrip_sep490.model.DistrictOption;
import com.example.weathertrip_sep490.model.LocationOption;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditSegmentBottomSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onSegmentUpdated();
    }

    private String tripId;
    private String segmentId;
    private String initialLocationName;
    private String initialDistrictName;
    private Listener listener;

    private AutoCompleteTextView etLocation;
    private Spinner spDistrict;
    private TextView tvStartDate;
    private TextView tvEndDate;

    private final List<String> locationItems = new ArrayList<>();
    private final Set<String> locationSet = new HashSet<>();
    private final Map<String, String> locationNameToId = new HashMap<>();
    private final Map<String, List<DistrictOption>> districtCache = new HashMap<>();

    private final List<String> districtLabels = new ArrayList<>();
    private final List<String> districtIds = new ArrayList<>();
    private ArrayAdapter<String> districtAdapter;

    private String selectedLocationId;
    private String selectedDistrictId;

    private Calendar startCal = Calendar.getInstance();
    private Calendar endCal = Calendar.getInstance();
    private Calendar tripStartCal = Calendar.getInstance();
    private Calendar tripEndCal = Calendar.getInstance();

    private boolean isSubmitting = false;
    private boolean isProgrammaticChange = false;

    public static EditSegmentBottomSheet newInstance(String tripId, String segmentId, String initialLocationName, String initialDistrictName, Listener listener) {
        EditSegmentBottomSheet fragment = new EditSegmentBottomSheet();
        fragment.tripId = tripId;
        fragment.segmentId = segmentId;
        fragment.initialLocationName = initialLocationName;
        fragment.initialDistrictName = initialDistrictName;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_edit_segment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etLocation = view.findViewById(R.id.etEditSegmentLocation);
        spDistrict = view.findViewById(R.id.spinnerEditSegmentDistrict);
        tvStartDate = view.findViewById(R.id.tvEditSegmentStart);
        tvEndDate = view.findViewById(R.id.tvEditSegmentEnd);

        view.findViewById(R.id.btnCloseEditSegment).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.rowEditSegmentStart).setOnClickListener(v -> showDatePicker(true));
        view.findViewById(R.id.rowEditSegmentEnd).setOnClickListener(v -> showDatePicker(false));
        view.findViewById(R.id.btnEditSegmentSave).setOnClickListener(v -> submit());

        // Load trip start and end date if available
        try {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("TravelGoPrefs", android.content.Context.MODE_PRIVATE);
            String tripStartIso = prefs.getString("trip_start_date_iso_" + tripId, null);
            String tripEndIso = prefs.getString("trip_end_date_iso_" + tripId, null);
            if (tripStartIso != null && !tripStartIso.isEmpty()) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                java.util.Date startDate = null;
                try {
                    startDate = sdf.parse(tripStartIso);
                } catch (Exception ignored) {
                    try {
                        startDate = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(tripStartIso);
                    } catch (Exception ignored2) {}
                }
                if (startDate != null) {
                    tripStartCal.setTime(startDate);
                    startCal.setTime(startDate);
                }
            }
            if (tripEndIso != null && !tripEndIso.isEmpty()) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                java.util.Date endDate = null;
                try {
                    endDate = sdf.parse(tripEndIso);
                } catch (Exception ignored) {
                    try {
                        endDate = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(tripEndIso);
                    } catch (Exception ignored2) {}
                }
                if (endDate != null) {
                    tripEndCal.setTime(endDate);
                    endCal.setTime(endDate);
                }
            } else {
                tripEndCal = (Calendar) tripStartCal.clone();
                tripEndCal.add(Calendar.DAY_OF_MONTH, 1);
                endCal = (Calendar) startCal.clone();
                endCal.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Override with segment specific dates if already saved
            String segDates = prefs.getString("trip_segment_dates_" + tripId, null);
            if (segDates != null && !segDates.trim().isEmpty()) {
                String[] parts = segDates.split(" - ");
                if (parts.length == 2) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    java.util.Date sd = sdf.parse(parts[0].trim());
                    java.util.Date ed = sdf.parse(parts[1].trim());
                    if (sd != null && ed != null) {
                        startCal.setTime(sd);
                        endCal.setTime(ed);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupLocationDropdown();
        setupDistrictSpinner();
        fetchLocations();
        refreshDateLabels();
    }

    private void setupLocationDropdown() {
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                locationItems
        );
        etLocation.setAdapter(locationAdapter);
        etLocation.setThreshold(0);
        etLocation.setOnClickListener(v -> etLocation.showDropDown());
        etLocation.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etLocation.showDropDown();
        });

        etLocation.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            onLocationSelected(selectedName);
        });

        etLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isProgrammaticChange) return;
                selectedLocationId = null;
                selectedDistrictId = null;
                resetDistrictSpinner();
            }
        });

        if (initialLocationName != null && !initialLocationName.isEmpty()) {
            isProgrammaticChange = true;
            etLocation.setText(initialLocationName);
            isProgrammaticChange = false;
        }
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
        if (districtAdapter != null) {
            districtAdapter.notifyDataSetChanged();
        }
        if (spDistrict != null) {
            spDistrict.setSelection(0, false);
        }
        selectedDistrictId = null;
    }

    private void fetchLocations() {
        RetrofitClient.getInstance().getUserAPI().getAllLocations().enqueue(new Callback<List<LocationOption>>() {
            @Override
            public void onResponse(@NonNull Call<List<LocationOption>> call, @NonNull Response<List<LocationOption>> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                locationItems.clear();
                locationSet.clear();
                locationNameToId.clear();
                for (LocationOption option : response.body()) {
                    if (option == null || option.getLocationName() == null) continue;
                    String name = option.getLocationName().trim();
                    String id = option.getLocationId() != null ? option.getLocationId().trim() : "";
                    if (name.isEmpty() || locationSet.contains(name)) continue;
                    locationSet.add(name);
                    locationItems.add(name);
                    if (!id.isEmpty()) {
                        locationNameToId.put(name, id);
                    }
                }
                districtAdapter.notifyDataSetChanged();

                // If initialLocationName matches one of our locations, select it!
                if (initialLocationName != null && !initialLocationName.isEmpty()) {
                    String canonical = resolveCanonicalLocationName(initialLocationName);
                    if (canonical != null) {
                        isProgrammaticChange = true;
                        etLocation.setText(canonical);
                        isProgrammaticChange = false;
                        onLocationSelected(canonical);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LocationOption>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi tải địa điểm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onLocationSelected(@NonNull String locationName) {
        selectedLocationId = locationNameToId.get(locationName);
        resetDistrictSpinner();
        if (selectedLocationId == null) return;
        fetchDistricts(selectedLocationId);
    }

    private void fetchDistricts(@NonNull String locationId) {
        String key = locationId.trim();
        if (districtCache.containsKey(key)) {
            applyDistricts(districtCache.get(key));
            return;
        }
        RetrofitClient.getInstance().getUserAPI().getDistrictsByLocation(key).enqueue(new Callback<List<DistrictOption>>() {
            @Override
            public void onResponse(@NonNull Call<List<DistrictOption>> call, @NonNull Response<List<DistrictOption>> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                districtCache.put(key, response.body());
                applyDistricts(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<DistrictOption>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi tải quận/huyện: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyDistricts(List<DistrictOption> districts) {
        districtLabels.clear();
        districtIds.clear();
        districtLabels.add("Chọn quận/huyện");
        districtIds.add(null);
        int selectedIndex = 0;
        if (districts != null) {
            for (DistrictOption d : districts) {
                if (d == null || d.getName() == null || d.getId() == null) continue;
                String dName = d.getName().trim();
                districtLabels.add(dName);
                districtIds.add(d.getId().trim());
                if (initialDistrictName != null && dName.equalsIgnoreCase(initialDistrictName.trim())) {
                    selectedIndex = districtLabels.size() - 1;
                }
            }
        }
        districtAdapter.notifyDataSetChanged();
        if (spDistrict != null) {
            int finalSelectedIndex = selectedIndex;
            spDistrict.post(() -> {
                spDistrict.setSelection(finalSelectedIndex, false);
                if (finalSelectedIndex > 0) {
                    selectedDistrictId = districtIds.get(finalSelectedIndex);
                } else {
                    selectedDistrictId = null;
                }
            });
        }
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
        try {
            dialog.getDatePicker().setMinDate(tripStartCal.getTimeInMillis());
            dialog.getDatePicker().setMaxDate(tripEndCal.getTimeInMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.show();
    }

    private void submit() {
        if (isSubmitting) return;

        String locationInput = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        if (locationInput.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn địa điểm", Toast.LENGTH_SHORT).show();
            return;
        }

        String resolvedLocation = resolveCanonicalLocationName(locationInput);
        if (resolvedLocation == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn địa điểm từ danh sách gợi ý", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedLocationId = locationNameToId.get(resolvedLocation);

        int distPos = spDistrict.getSelectedItemPosition();
        if (distPos < 1 || distPos >= districtIds.size() || districtIds.get(distPos) == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn quận/huyện", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedDistrictId = districtIds.get(distPos);

        String startIso = toApiDateTime(startCal);
        String endIso = toApiDateTime(endCal);

        isSubmitting = true;
        AddSegmentRequest request = new AddSegmentRequest(selectedLocationId, selectedDistrictId, startIso, endIso);

        RetrofitClient.getInstance().getUserAPI().updateTripSegment(tripId, segmentId, request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                isSubmitting = false;
                android.content.Context context = getContext();
                if (context == null || !isAdded()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    Toast.makeText(context, "Cập nhật chặng thất bại", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(context, "Cập nhật chặng thành công!", Toast.LENGTH_SHORT).show();
                try {
                    android.content.SharedPreferences prefs = context.getSharedPreferences("TravelGoPrefs", android.content.Context.MODE_PRIVATE);
                    String locName = etLocation.getText().toString().trim();
                    String distName = spDistrict.getSelectedItem() != null ? spDistrict.getSelectedItem().toString() : "";
                    java.text.DateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateRange = df.format(startCal.getTime()) + " - " + df.format(endCal.getTime());
                    prefs.edit()
                         .putString("trip_segment_location_" + tripId, locName)
                         .putString("trip_segment_district_" + tripId, distName)
                         .putString("trip_segment_dates_" + tripId, dateRange)
                         .apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (listener != null) {
                    listener.onSegmentUpdated();
                }
                dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                isSubmitting = false;
                android.content.Context context = getContext();
                if (context != null && isAdded()) {
                    Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
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
