package com.example.weathertrip_sep490.adapter;

import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.model.Trip;
import com.example.weathertrip_sep490.model.TripStatus;

import java.util.ArrayList;
import java.util.List;

public class TripCardAdapter extends RecyclerView.Adapter<TripCardAdapter.Holder> {

    public interface TripCardListener {
        void onReplan(Trip trip);

        void onViewDetails(Trip trip);

        void onReview(Trip trip);
    }

    private final List<Trip> items = new ArrayList<>();
    private final TripCardListener listener;

    public TripCardAdapter(TripCardListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Trip> trips) {
        items.clear();
        if (trips != null) {
            items.addAll(trips);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_card, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Trip trip = items.get(position);
        holder.bind(trip, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        private final ImageView imgTrip;
        private final TextView tvBadge;
        private final TextView tvCity;
        private final TextView tvStartDate;

        private final TextView tvCost;
        private final TextView tvType;
        private final TextView btnReplan;
        private final TextView btnDetails;


        Holder(@NonNull View itemView) {
            super(itemView);
            imgTrip = itemView.findViewById(R.id.imgTripCover);
            tvBadge = itemView.findViewById(R.id.tvTripStatusBadge);
            tvCity = itemView.findViewById(R.id.tvTripCity);
            tvStartDate = itemView.findViewById(R.id.tvTripDateRange);
            tvCost = itemView.findViewById(R.id.tvTripCost);
            tvType = itemView.findViewById(R.id.tvTripType);
            btnReplan = itemView.findViewById(R.id.btnTripReplan);
            btnDetails = itemView.findViewById(R.id.btnTripDetails);

        }

        void bind(Trip trip, TripCardListener listener) {
            Glide.with(itemView.getContext())
                    .load(trip.getImageResId())
                    .centerCrop()
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(imgTrip);

            tvCity.setText(trip.getTripTitle());

            ParsedDates parsed = parseDates(trip.getDateRange());
            tvStartDate.setText(parsed.startDate + " - " + parsed.endDate);

            tvType.setText("Loại: " + parsed.tripTypeLabel);

            String from = trip.getStartPoint() != null ? trip.getStartPoint().trim() : "";
            String to = trip.getDestination() != null ? trip.getDestination().trim() : "";
            if (from.isEmpty() && to.isEmpty()) {
                tvCost.setText("—");
            } else if (from.isEmpty()) {
                tvCost.setText("Đến: " + to);
            } else if (to.isEmpty()) {
                tvCost.setText("Đi: " + from);
            } else {
                tvCost.setText(from + " → " + to);
            }
            tvCost.setVisibility(View.VISIBLE);

            applyStatusBadge(trip.getStatus());

            btnReplan.setOnClickListener(v -> {
                if (listener != null) listener.onReplan(trip);
            });
            btnDetails.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetails(trip);
            });
        }

        private void applyStatusBadge(TripStatus status) {
            int bgColor;
            int textColor;
            int borderColor;
            String label;
            switch (status) {
                case UPCOMING:
                    label = "Sắp tới";
                    bgColor = Color.parseColor("#F59E0B");
                    textColor = Color.parseColor("#FFFFFF");
                    borderColor = Color.parseColor("#D97706");
                    break;
                case ONGOING:
                    label = "Đang diễn ra";
                    bgColor = Color.parseColor("#EF4444");
                    textColor = Color.parseColor("#FFFFFF");
                    borderColor = Color.parseColor("#DC2626");
                    break;
                case COMPLETED:
                default:
                    label = "Đã hoàn thành";
                    bgColor = Color.parseColor("#10B981");
                    textColor = Color.parseColor("#FFFFFF");
                    borderColor = Color.parseColor("#059669");
                    break;
            }
            tvBadge.setText(label);
            tvBadge.setTextColor(textColor);
            GradientDrawable d = new GradientDrawable();
            d.setCornerRadius(dp(14));
            d.setColor(bgColor);
            d.setStroke((int) dp(1), borderColor);
            tvBadge.setBackground(d);
        }

        private float dp(float v) {
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, v, itemView.getContext().getResources().getDisplayMetrics());
        }

        private static class ParsedDates {
            String startDate;
            String endDate;
            String tripTypeLabel; // "1 chiều" hoặc "Khứ hồi"
        }

        private static ParsedDates parseDates(@Nullable String dateRange) {
            String s = dateRange != null ? dateRange.trim() : "";
            ParsedDates out = new ParsedDates();

            if (s.contains("1 chiều")) {
                out.tripTypeLabel = "1 chiều";
                // Format hiện tại: "dd/MM/yyyy · 1 chiều"
                out.startDate = s.replace("· 1 chiều", "").replace(" · 1 chiều", "").trim();
                if (out.startDate.isEmpty()) out.startDate = "—";
                // Với 1 chiều popup chỉ có 1 ngày => coi "ngày đến" trùng ngày đi
                out.endDate = out.startDate;
                return out;
            }

            out.tripTypeLabel = "Khứ hồi";
            // Format round-trip: "dd/MM/yyyy - dd/MM/yyyy"
            String[] parts = s.split("\\s+-\\s+", 2);
            out.startDate = parts.length >= 1 ? parts[0].trim() : "—";
            out.endDate = parts.length >= 2 ? parts[1].trim() : "—";
            return out;
        }
    }
}
