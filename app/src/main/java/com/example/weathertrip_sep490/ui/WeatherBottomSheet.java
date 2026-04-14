package com.example.weathertrip_sep490.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.adapter.WeatherDailyAdapter;
import com.example.weathertrip_sep490.data.OpenMeteoAPI;
import com.example.weathertrip_sep490.data.RetrofitClient;
import com.example.weathertrip_sep490.data.WeatherAPI;
import com.example.weathertrip_sep490.model.OpenMeteoDailyResponse;
import com.example.weathertrip_sep490.model.Weather;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "WeatherBottomSheet";

    private EditText etLat;
    private EditText etLng;
    private TextView tvFrom;
    private TextView tvTo;
    private View btnFetch;
    private View btnClose;
    private View progress;

    private WeatherDailyAdapter adapter;
    private final Calendar fromCal = Calendar.getInstance();
    private final Calendar toCal = Calendar.getInstance();

    private final SimpleDateFormat dfDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etLat = view.findViewById(R.id.etWeatherLat);
        etLng = view.findViewById(R.id.etWeatherLng);
        tvFrom = view.findViewById(R.id.tvWeatherFrom);
        tvTo = view.findViewById(R.id.tvWeatherTo);
        btnFetch = view.findViewById(R.id.btnFetchWeather);
        btnClose = view.findViewById(R.id.btnCloseWeather);
        progress = view.findViewById(R.id.progressWeather);

        // default: HCM + 3 days
        if (TextUtils.isEmpty(etLat.getText())) etLat.setText("10.8231");
        if (TextUtils.isEmpty(etLng.getText())) etLng.setText("106.6297");
        toCal.add(Calendar.DAY_OF_MONTH, 3);
        refreshDates();

        view.findViewById(R.id.rowWeatherFrom).setOnClickListener(v -> pickDate(true));
        view.findViewById(R.id.rowWeatherTo).setOnClickListener(v -> pickDate(false));
        btnClose.setOnClickListener(v -> dismiss());

        RecyclerView rv = view.findViewById(R.id.rvWeatherDaily);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WeatherDailyAdapter();
        rv.setAdapter(adapter);

        btnFetch.setOnClickListener(v -> fetch());
    }

    private void refreshDates() {
        tvFrom.setText(dfDisplay.format(fromCal.getTime()));
        tvTo.setText(dfDisplay.format(toCal.getTime()));
    }

    private void pickDate(boolean isFrom) {
        Calendar c = isFrom ? fromCal : toCal;
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (picker, year, month, day) -> {
                    c.set(year, month, day, 0, 0, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    enforceRangeConstraints();
                    refreshDates();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    /**
     * Backend hiện giới hạn forecast <= 7 ngày (xem OpenMeteoService).
     * Đồng thời đảm bảo to >= from.
     */
    private void enforceRangeConstraints() {
        if (toCal.before(fromCal)) {
            toCal.setTime(fromCal.getTime());
        }
        long diffMs = toCal.getTimeInMillis() - fromCal.getTimeInMillis();
        long diffDays = diffMs / (24L * 60L * 60L * 1000L);
        if (diffDays > 6) {
            Calendar capped = (Calendar) fromCal.clone();
            capped.add(Calendar.DAY_OF_MONTH, 6);
            toCal.setTime(capped.getTime());
        }
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnFetch.setEnabled(!loading);
        btnFetch.setAlpha(loading ? 0.7f : 1f);
    }

    private void fetch() {
        String latStr = etLat.getText() != null ? etLat.getText().toString().trim() : "";
        String lngStr = etLng.getText() != null ? etLng.getText().toString().trim() : "";
        if (latStr.isEmpty() || lngStr.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập latitude và longitude", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate numeric input (accept both "." and "," as decimal separators in UI)
        Double lat = tryParseDoubleFlexible(latStr);
        Double lng = tryParseDoubleFlexible(lngStr);
        if (lat == null || lng == null) {
            Toast.makeText(requireContext(), "Latitude/Longitude không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        enforceRangeConstraints();

        String from = toIsoUtc(fromCal);
        String to = toIsoUtc(toCal);
        String startDate = dfQueryDate(fromCal);
        String endDate = dfQueryDate(toCal);

        setLoading(true);
        WeatherAPI api = RetrofitClient.getInstance().getWeatherAPI();
        // Format cố định theo Locale.US để luôn dùng dấu "." cho phần thập phân.
        // Tránh trường hợp server parse sai và đẩy sang Open-Meteo bị "Given: 823099".
        String latQuery = String.format(Locale.US, "%.6f", lat);
        String lngQuery = String.format(Locale.US, "%.6f", lng);
        fetchOnce(api, latQuery, lngQuery, from, to, startDate, endDate, true);
    }

    private void fetchOnce(
            @NonNull WeatherAPI api,
            @NonNull String latitude,
            @NonNull String longitude,
            @NonNull String from,
            @NonNull String to,
            @NonNull String startDate,
            @NonNull String endDate,
            boolean allowIntegerRetry
    ) {
        api.getDailyWeather(latitude, longitude, from, to).enqueue(new Callback<List<Weather>>() {
            @Override
            public void onResponse(@NonNull Call<List<Weather>> call, @NonNull Response<List<Weather>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setLoading(false);
                    adapter.submit(response.body());
                } else {
                    String details = "";
                    try {
                        ResponseBody eb = response.errorBody();
                        if (eb != null) {
                            String raw = eb.string();
                            if (raw != null) details = raw.trim();
                        }
                    } catch (Exception ignored) {}
                    Log.e(TAG, "getDailyWeather failed code=" + response.code()
                            + " lat=" + latitude + " lng=" + longitude + " from=" + from + " to=" + to
                            + " body=" + details);

                    // BE đang lỗi parse lat/lng -> Open-Meteo trả 400 "Latitude must be in range..."
                    // Không sửa BE, nên fallback gọi thẳng Open-Meteo để user vẫn xem được thời tiết.
                    if (response.code() == 400 && isLatitudeRangeError(details)) {
                        if (allowIntegerRetry) {
                            Double latValue = tryParseDoubleFlexible(latitude);
                            Double lngValue = tryParseDoubleFlexible(longitude);
                            String latInt = String.valueOf(Math.round(latValue != null ? latValue : 0d));
                            String lngInt = String.valueOf(Math.round(lngValue != null ? lngValue : 0d));
                            Log.w(TAG, "Retry BE weather with integer coords lat=" + latInt + ", lng=" + lngInt);
                            fetchOnce(api, latInt, lngInt, from, to, startDate, endDate, false);
                            return;
                        }
                        Log.w(TAG, "Fallback to Open-Meteo direct call due to latitude parse issue.");
                        fetchFromOpenMeteo(latitude, longitude, startDate, endDate);
                        return;
                    }

                    setLoading(false);
                    Toast.makeText(
                            requireContext(),
                            "Không lấy được thời tiết (" + response.code() + ")" + (details.isEmpty() ? "" : (": " + details)),
                            Toast.LENGTH_LONG
                    ).show();
                    adapter.submit(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Weather>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + (t.getMessage() != null ? t.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchFromOpenMeteo(
            @NonNull String latitude,
            @NonNull String longitude,
            @NonNull String startDate,
            @NonNull String endDate
    ) {
        OpenMeteoAPI api = new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenMeteoAPI.class);

        api.getDaily(
                latitude,
                longitude,
                "temperature_2m_max,precipitation_probability_max,wind_speed_10m_max",
                startDate,
                endDate,
                "auto"
        ).enqueue(new Callback<OpenMeteoDailyResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenMeteoDailyResponse> call, @NonNull Response<OpenMeteoDailyResponse> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null || response.body().daily == null) {
                    Toast.makeText(requireContext(), "Không lấy được thời tiết (" + response.code() + ")", Toast.LENGTH_LONG).show();
                    adapter.submit(new ArrayList<>());
                    return;
                }
                OpenMeteoDailyResponse.Daily d = response.body().daily;
                List<Weather> out = new ArrayList<>();
                int n = d.time != null ? d.time.size() : 0;
                for (int i = 0; i < n; i++) {
                    Weather w = new Weather();
                    w.setDate(d.time.get(i));
                    if (d.temperatureMax != null && i < d.temperatureMax.size() && d.temperatureMax.get(i) != null) {
                        w.setMaxTemperature(d.temperatureMax.get(i));
                    }
                    if (d.precipitationProbabilityMax != null && i < d.precipitationProbabilityMax.size() && d.precipitationProbabilityMax.get(i) != null) {
                        w.setPrecipitationProbability(d.precipitationProbabilityMax.get(i));
                    }
                    if (d.windSpeedMax != null && i < d.windSpeedMax.size() && d.windSpeedMax.get(i) != null) {
                        w.setMaxWindSpeed(d.windSpeedMax.get(i));
                    }
                    out.add(w);
                }
                Toast.makeText(requireContext(), "BE lỗi parse toạ độ, đã lấy từ Open‑Meteo trực tiếp.", Toast.LENGTH_SHORT).show();
                adapter.submit(out);
            }

            @Override
            public void onFailure(@NonNull Call<OpenMeteoDailyResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), "Lỗi Open‑Meteo: " + (t.getMessage() != null ? t.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Nullable
    private static Double tryParseDoubleFlexible(@NonNull String s) {
        try {
            return Double.parseDouble(s.trim().replace(',', '.'));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isLatitudeRangeError(@NonNull String details) {
        String d = details.toLowerCase(Locale.US);
        return d.contains("latitude must be in range")
                || d.contains("\"latitude must be in range")
                || d.contains("given:");
    }

    private static String dfQueryDate(@NonNull Calendar calendar) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(calendar.getTime());
    }

    private static String toIsoUtc(@NonNull Calendar calendar) {
        Calendar c = (Calendar) calendar.clone();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));
        return iso.format(c.getTime());
    }
}

