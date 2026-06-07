package com.example.weathertrip_sep490.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.model.Weather;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeatherDailyAdapter extends RecyclerView.Adapter<WeatherDailyAdapter.VH> {

    private final List<Weather> items = new ArrayList<>();
    private final DecimalFormat df1 = new DecimalFormat("#.#");

    public void submit(List<Weather> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather_day, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Weather w = items.get(position);
        String date = w.getDate() != null ? w.getDate() : "";
        h.tvDate.setText(date.replace("T00:00:00", ""));

        h.tvTemp.setText(String.format(Locale.getDefault(), "%s°C", df1.format(w.getMaxTemperature())));
        h.tvRain.setText(String.format(Locale.getDefault(), "%s%%", df1.format(w.getPrecipitationProbability())));
        h.tvWind.setText(String.format(Locale.getDefault(), "%s km/h", df1.format(w.getMaxWindSpeed())));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate, tvTemp, tvRain, tvWind;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvWeatherDate);
            tvTemp = itemView.findViewById(R.id.tvWeatherTemp);
            tvRain = itemView.findViewById(R.id.tvWeatherRain);
            tvWind = itemView.findViewById(R.id.tvWeatherWind);
        }
    }
}

