package com.example.weathertrip_sep490.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.model.TimelineEvent;

import java.util.ArrayList;
import java.util.List;

public class TimelineEventAdapter extends RecyclerView.Adapter<TimelineEventAdapter.Holder> {

    private final List<TimelineEvent> items = new ArrayList<>();

    public void updateData(List<TimelineEvent> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_event, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(items.get(position), position == items.size() - 1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        private final TextView tvTime;
        private final View vLineTop;
        private final View vLineBottom;
        private final TextView tvTimeRange;
        private final TextView tvTempMini;
        private final ImageView ivCover;
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final View rowWeather;
        private final TextView tvWeatherTemp;
        private final TextView tvWeatherDesc;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvEventTime);
            vLineTop = itemView.findViewById(R.id.vLineTop);
            vLineBottom = itemView.findViewById(R.id.vLineBottom);
            tvTimeRange = itemView.findViewById(R.id.tvEventTimeRange);
            tvTempMini = itemView.findViewById(R.id.tvEventTempMini);
            ivCover = itemView.findViewById(R.id.imgEventCover);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvSubtitle = itemView.findViewById(R.id.tvEventSubtitle);
            rowWeather = itemView.findViewById(R.id.rowEventWeather);
            tvWeatherTemp = itemView.findViewById(R.id.tvEventWeatherTemp);
            tvWeatherDesc = itemView.findViewById(R.id.tvEventWeatherDesc);
        }

        void bind(TimelineEvent e, boolean isLast) {
            String timeRange = e.getTime();
            tvTimeRange.setText(timeRange);
            tvTime.setText(extractStartTime(timeRange));
            vLineTop.setVisibility(getAdapterPosition() == 0 ? View.INVISIBLE : View.VISIBLE);
            vLineBottom.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);

            tvTitle.setText(e.getTitle());
            if (e.getSubtitle() == null || e.getSubtitle().trim().isEmpty()) {
                tvSubtitle.setVisibility(View.GONE);
            } else {
                tvSubtitle.setVisibility(View.VISIBLE);
                tvSubtitle.setText(e.getSubtitle());
            }

            Glide.with(itemView.getContext())
                    .load(e.getImageResId())
                    .centerCrop()
                    .placeholder(R.drawable.sampleplace3)
                    .error(R.drawable.bg_image_placeholder)
                    .into(ivCover);

            boolean hasWeather = e.getWeatherTemp() != null && !e.getWeatherTemp().trim().isEmpty();
            if (hasWeather) {
                rowWeather.setVisibility(View.VISIBLE);
                tvWeatherTemp.setText(e.getWeatherTemp());
                tvWeatherDesc.setText(e.getWeatherDesc() != null ? e.getWeatherDesc() : "");
                tvTempMini.setText(e.getWeatherTemp());
            } else {
                rowWeather.setVisibility(View.GONE);
                tvTempMini.setText("");
            }
        }

        private static String extractStartTime(String timeRange) {
            if (timeRange == null) return "";
            int dash = timeRange.indexOf("-");
            if (dash <= 0) return timeRange.trim();
            return timeRange.substring(0, dash).trim();
        }
    }
}

