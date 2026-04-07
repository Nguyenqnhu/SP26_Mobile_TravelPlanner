package com.example.weathertrip_sep490.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.weathertrip_sep490.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.Locale;

public class CreateTripBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "CreateTripBottomSheet";

    /** index 0 = 1 chiều, 1 = khứ hồi */
    public static final int TRIP_TYPE_ONE_WAY = 0;
    public static final int TRIP_TYPE_ROUND_TRIP = 1;

    public interface Listener {
        void onCreateTripWithAi(
                @NonNull String tripTitle,
                @NonNull String startPoint,
                @NonNull String destination,
                boolean roundTrip,
                @NonNull String startDateDisplay,
                @NonNull String endDateDisplay
        );
    }

    private Listener listener;

    private EditText etTripTitle;
    private EditText etStartPoint;
    private EditText etDestination;
    private Spinner spinnerTripType;
    private View layoutColumnReturnDate;
    private TextView tvStartDate;
    private TextView tvEndDate;

    private Calendar startCal;
    private Calendar endCal;

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
        etDestination = view.findViewById(R.id.etCreateTripDestination);
        spinnerTripType = view.findViewById(R.id.spinnerTripType);
        layoutColumnReturnDate = view.findViewById(R.id.layoutColumnReturnDate);
        tvStartDate = view.findViewById(R.id.tvCreateTripStartDate);
        tvEndDate = view.findViewById(R.id.tvCreateTripEndDate);

        View rowStart = view.findViewById(R.id.rowStartDate);
        View rowEnd = view.findViewById(R.id.rowEndDate);

        startCal = Calendar.getInstance();
        endCal = Calendar.getInstance();
        endCal.add(Calendar.DAY_OF_MONTH, 3);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.trip_type_choices,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTripType.setAdapter(typeAdapter);
        spinnerTripType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                applyTripTypeUi(position == TRIP_TYPE_ROUND_TRIP);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerTripType.setSelection(TRIP_TYPE_ROUND_TRIP);

        view.findViewById(R.id.btnCloseCreateTrip).setOnClickListener(v -> dismiss());

        rowStart.setOnClickListener(v -> showDatePicker(true));
        rowEnd.setOnClickListener(v -> {
            if (spinnerTripType.getSelectedItemPosition() != TRIP_TYPE_ROUND_TRIP) {
                return;
            }
            showDatePicker(false);
        });

        view.findViewById(R.id.btnCreateTripWithAi).setOnClickListener(v -> submit());

        refreshDateLabels();
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
        if (!isStart && spinnerTripType.getSelectedItemPosition() != TRIP_TYPE_ROUND_TRIP) {
            return;
        }
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
        String tripTitle = etTripTitle.getText() != null ? etTripTitle.getText().toString().trim() : "";
        String startPoint = etStartPoint.getText() != null ? etStartPoint.getText().toString().trim() : "";
        String destination = etDestination.getText() != null ? etDestination.getText().toString().trim() : "";
        if (tripTitle.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên chuyến đi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startPoint.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập điểm bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập điểm đến", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean roundTrip = spinnerTripType.getSelectedItemPosition() == TRIP_TYPE_ROUND_TRIP;
        java.text.DateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startStr = df.format(startCal.getTime());
        String endStr = roundTrip ? df.format(endCal.getTime()) : "";

        if (roundTrip) {
            if (endCal.before(startCal)) {
                Toast.makeText(requireContext(), "Ngày về phải sau hoặc bằng ngày đi", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (listener != null) {
            listener.onCreateTripWithAi(tripTitle, startPoint, destination, roundTrip, startStr, endStr);
        }
        dismiss();
    }
}
