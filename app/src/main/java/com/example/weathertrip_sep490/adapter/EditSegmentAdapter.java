package com.example.weathertrip_sep490.adapter;

import com.example.weathertrip_sep490.util.AppToast;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.model.AddSegmentRequest;
import com.example.weathertrip_sep490.model.DistrictOption;
import com.example.weathertrip_sep490.model.TripSegmentResponse;
import com.google.android.material.button.MaterialButton;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

public class EditSegmentAdapter extends RecyclerView.Adapter<EditSegmentAdapter.SegmentViewHolder> {

    private final Context context;
    private final String tripId;
    private final List<TripSegmentResponse> segments = new ArrayList<>();
    
    // Global locations loaded from activity
    private final List<String> locationItems = new ArrayList<>();
    private final Set<String> locationSet = new HashSet<>();
    private final Map<String, String> locationNameToId = new HashMap<>();

    // Cache of districts per locationId to prevent double network calls
    private final Map<String, List<DistrictOption>> districtCache = new HashMap<>();

    // Trip bounds for date pickers
    private Calendar tripStartCal = Calendar.getInstance();
    private Calendar tripEndCal = Calendar.getInstance();

    public interface OnSegmentUpdatedListener {
        void onSegmentUpdated(String segmentId, String newLocationName, String newDistrictName, String newDates);
    }
    
    private final OnSegmentUpdatedListener updateListener;

    public EditSegmentAdapter(Context context, String tripId, OnSegmentUpdatedListener listener) {
        this.context = context;
        this.tripId = tripId;
        this.updateListener = listener;
        loadTripBounds();
    }

    private void loadTripBounds() {
        try {
            SharedPreferences prefs = context.getSharedPreferences("TravelGoPrefs", Context.MODE_PRIVATE);
            String tripStartIso = prefs.getString("trip_start_date_iso_" + tripId, null);
            String tripEndIso = prefs.getString("trip_end_date_iso_" + tripId, null);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            if (tripStartIso != null && !tripStartIso.isEmpty()) {
                Date startDate = null;
                try {
                    startDate = sdf.parse(tripStartIso);
                } catch (Exception e1) {
                    try {
                        startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(tripStartIso);
                    } catch (Exception ignored) {}
                }
                if (startDate != null) {
                    tripStartCal.setTime(startDate);
                }
            }
            if (tripEndIso != null && !tripEndIso.isEmpty()) {
                Date endDate = null;
                try {
                    endDate = sdf.parse(tripEndIso);
                } catch (Exception e1) {
                    try {
                        endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(tripEndIso);
                    } catch (Exception ignored) {}
                }
                if (endDate != null) {
                    tripEndCal.setTime(endDate);
                }
            } else {
                tripEndCal = (Calendar) tripStartCal.clone();
                tripEndCal.add(Calendar.DAY_OF_MONTH, 7);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLocations(List<com.example.weathertrip_sep490.model.LocationOption> locations) {
        locationItems.clear();
        locationSet.clear();
        locationNameToId.clear();
        if (locations != null) {
            for (com.example.weathertrip_sep490.model.LocationOption opt : locations) {
                if (opt == null || opt.getLocationName() == null) continue;
                String name = opt.getLocationName().trim();
                String id = opt.getLocationId() != null ? opt.getLocationId().trim() : "";
                if (name.isEmpty() || locationSet.contains(name)) continue;
                locationSet.add(name);
                locationItems.add(name);
                if (!id.isEmpty()) {
                    locationNameToId.put(name, id);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(List<TripSegmentResponse> newSegments) {
        segments.clear();
        if (newSegments != null) {
            segments.addAll(newSegments);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SegmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_segment_card, parent, false);
        return new SegmentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SegmentViewHolder holder, int position) {
        holder.bind(segments.get(position));
    }

    @Override
    public int getItemCount() {
        return segments.size();
    }

    class SegmentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final AutoCompleteTextView etLocation;
        private final Spinner spDistrict;
        private final View rowStart;
        private final View rowEnd;
        private final TextView tvStart;
        private final TextView tvEnd;
        private final MaterialButton btnSave;

        private Calendar startCal = Calendar.getInstance();
        private Calendar endCal = Calendar.getInstance();

        private String currentSegmentId;
        private String selectedLocationId;
        private String selectedDistrictId;

        private final List<String> districtLabels = new ArrayList<>();
        private final List<String> districtIds = new ArrayList<>();
        private ArrayAdapter<String> districtSpinnerAdapter;

        private boolean isProgrammaticChange = false;
        private boolean isSubmitting = false;

        SegmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSegmentTitle);
            etLocation = itemView.findViewById(R.id.etSegmentLocation);
            spDistrict = itemView.findViewById(R.id.spSegmentDistrict);
            rowStart = itemView.findViewById(R.id.rowSegmentStart);
            rowEnd = itemView.findViewById(R.id.rowSegmentEnd);
            tvStart = itemView.findViewById(R.id.tvSegmentStart);
            tvEnd = itemView.findViewById(R.id.tvSegmentEnd);
            btnSave = itemView.findViewById(R.id.btnSegmentSave);

            setupLocationAutoComplete();
            setupDistrictSpinner();
            setupDatePickers();
            setupSaveButton();
        }

        private void setupLocationAutoComplete() {
            ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, locationItems);
            etLocation.setAdapter(locationAdapter);
            etLocation.setOnItemClickListener((parent, view, position, id) -> {
                if (isProgrammaticChange) return;
                String selected = (String) parent.getItemAtPosition(position);
                onLocationSelected(selected);
            });
            etLocation.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (isProgrammaticChange) return;
                    String input = s.toString().trim();
                    if (locationSet.contains(input)) {
                        onLocationSelected(input);
                    }
                }
            });
        }

        private void setupDistrictSpinner() {
            districtSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, districtLabels);
            districtSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDistrict.setAdapter(districtSpinnerAdapter);

            spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position > 0 && position < districtIds.size()) {
                        selectedDistrictId = districtIds.get(position);
                    } else {
                        selectedDistrictId = null;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedDistrictId = null;
                }
            });
        }

        private void resetDistrictSpinner() {
            districtLabels.clear();
            districtIds.clear();
            districtLabels.add("Chọn quận/huyện");
            districtIds.add(null);
            districtSpinnerAdapter.notifyDataSetChanged();
            spDistrict.setSelection(0);
            selectedDistrictId = null;
        }

        private void setupDatePickers() {
            rowStart.setOnClickListener(v -> showDatePicker(true));
            rowEnd.setOnClickListener(v -> showDatePicker(false));
        }

        private void showDatePicker(boolean isStart) {
            Calendar cal = isStart ? startCal : endCal;
            DatePickerDialog dialog = new DatePickerDialog(
                    context,
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
                                AppToast.show(context, "Ngày về phải sau hoặc bằng ngày đi");
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

        private void refreshDateLabels() {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvStart.setText(df.format(startCal.getTime()));
            tvEnd.setText(df.format(endCal.getTime()));
        }

        private void setupSaveButton() {
            btnSave.setOnClickListener(v -> submit());
        }

        private void submit() {
            if (isSubmitting) return;

            String locationInput = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
            if (locationInput.isEmpty()) {
                AppToast.show(context, "Vui lòng chọn địa điểm");
                return;
            }

            String resolvedLocation = resolveCanonicalLocationName(locationInput);
            if (resolvedLocation == null) {
                AppToast.show(context, "Vui lòng chọn địa điểm từ danh sách gợi ý");
                return;
            }
            selectedLocationId = locationNameToId.get(resolvedLocation);

            int distPos = spDistrict.getSelectedItemPosition();
            if (distPos < 1 || distPos >= districtIds.size() || districtIds.get(distPos) == null) {
                AppToast.show(context, "Vui lòng chọn quận/huyện");
                return;
            }
            selectedDistrictId = districtIds.get(distPos);

            String startIso = toApiDateTime(startCal);
            String endIso = toApiDateTime(endCal);

            isSubmitting = true;
            btnSave.setEnabled(false);
            btnSave.setText("Đang cập nhật...");

            AddSegmentRequest request = new AddSegmentRequest(selectedLocationId, selectedDistrictId, startIso, endIso);

            RetrofitClient.getInstance().getUserAPI().updateTripSegment(tripId, currentSegmentId, request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    isSubmitting = false;
                    btnSave.setEnabled(true);
                    btnSave.setText("Cập nhật chặng này");

                    if (!response.isSuccessful()) {
                        AppToast.show(context, "Cập nhật chặng thất bại");
                        return;
                    }
                    AppToast.show(context, "Cập nhật chặng thành công!");
                    
                    String locName = etLocation.getText().toString().trim();
                    String distName = spDistrict.getSelectedItem() != null ? spDistrict.getSelectedItem().toString() : "";
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateRange = df.format(startCal.getTime()) + " - " + df.format(endCal.getTime());

                    try {
                        SharedPreferences prefs = context.getSharedPreferences("TravelGoPrefs", Context.MODE_PRIVATE);
                        prefs.edit()
                             .putString("trip_segment_location_" + tripId + "_" + currentSegmentId, locName)
                             .putString("trip_segment_district_" + tripId + "_" + currentSegmentId, distName)
                             .putString("trip_segment_dates_" + tripId + "_" + currentSegmentId, dateRange)
                             .apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (updateListener != null) {
                        updateListener.onSegmentUpdated(currentSegmentId, locName, distName, dateRange);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    isSubmitting = false;
                    btnSave.setEnabled(true);
                    btnSave.setText("Cập nhật chặng này");
                    AppToast.show(context, "Lỗi mạng: " + t.getMessage());
                }
            });
        }

        void bind(TripSegmentResponse segment) {
            currentSegmentId = segment.getSegmentId();
            tvTitle.setText("Chặng dừng chân #" + segment.getOrderIndex());

            if (segment.getStartDate() != null) {
                startCal.setTime(segment.getStartDate());
            }
            if (segment.getEndDate() != null) {
                endCal.setTime(segment.getEndDate());
            }
            refreshDateLabels();

            // Load segment-specific location and district from SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("TravelGoPrefs", Context.MODE_PRIVATE);
            String initialLocation = prefs.getString("trip_segment_location_" + tripId + "_" + currentSegmentId, null);
            String initialDistrict = prefs.getString("trip_segment_district_" + tripId + "_" + currentSegmentId, null);

            isProgrammaticChange = true;
            if (initialLocation != null && !initialLocation.isEmpty()) {
                etLocation.setText(initialLocation);
            } else {
                etLocation.setText("");
            }
            isProgrammaticChange = false;

            resetDistrictSpinner();

            if (initialLocation != null && !initialLocation.trim().isEmpty()) {
                String canonical = resolveCanonicalLocationName(initialLocation);
                if (canonical != null) {
                    selectedLocationId = locationNameToId.get(canonical);
                    if (selectedLocationId != null) {
                        fetchDistricts(selectedLocationId, initialDistrict);
                    }
                }
            }
        }

        private void onLocationSelected(@NonNull String locationName) {
            selectedLocationId = locationNameToId.get(locationName);
            resetDistrictSpinner();
            if (selectedLocationId == null) return;
            fetchDistricts(selectedLocationId, null);
        }

        private void fetchDistricts(@NonNull String locationId, @Nullable String targetDistrictName) {
            String key = locationId.trim();
            if (districtCache.containsKey(key)) {
                applyDistricts(districtCache.get(key), targetDistrictName);
                return;
            }
            RetrofitClient.getInstance().getUserAPI().getDistrictsByLocation(key).enqueue(new Callback<List<DistrictOption>>() {
                @Override
                public void onResponse(@NonNull Call<List<DistrictOption>> call, @NonNull Response<List<DistrictOption>> response) {
                    if (!response.isSuccessful() || response.body() == null) return;
                    districtCache.put(key, response.body());
                    applyDistricts(response.body(), targetDistrictName);
                }

                @Override
                public void onFailure(@NonNull Call<List<DistrictOption>> call, @NonNull Throwable t) {
                    AppToast.show(context, "Lỗi tải quận/huyện: " + t.getMessage());
                }
            });
        }

        private void applyDistricts(List<DistrictOption> districts, @Nullable String targetDistrictName) {
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
                    if (targetDistrictName != null && dName.equalsIgnoreCase(targetDistrictName.trim())) {
                        selectedIndex = districtLabels.size() - 1;
                    }
                }
            }
            districtSpinnerAdapter.notifyDataSetChanged();
            
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
    }

    private static String toApiDateTime(@NonNull Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        return apiFormat.format(c.getTime());
    }

    @NonNull
    private static String normalizeForLookup(@NonNull String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
