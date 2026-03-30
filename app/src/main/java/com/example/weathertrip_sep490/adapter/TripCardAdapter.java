package com.example.weathertrip_sep490.adapter;

import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
        private final TextView tvDates;
        private final TextView tvCost;
        private final TextView btnReplan;
        private final TextView btnDetails;
        private final TextView btnReview;

        Holder(@NonNull View itemView) {
            super(itemView);
            imgTrip = itemView.findViewById(R.id.imgTripCover);
            tvBadge = itemView.findViewById(R.id.tvTripStatusBadge);
            tvCity = itemView.findViewById(R.id.tvTripCity);
            tvDates = itemView.findViewById(R.id.tvTripDates);
            tvCost = itemView.findViewById(R.id.tvTripCost);
            btnReplan = itemView.findViewById(R.id.btnTripReplan);
            btnDetails = itemView.findViewById(R.id.btnTripDetails);
            btnReview = itemView.findViewById(R.id.btnTripReview);
        }

        void bind(Trip trip, TripCardListener listener) {
            Glide.with(itemView.getContext())
                    .load(trip.getImageResId())
                    .centerCrop()
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(imgTrip);

            tvCity.setText(trip.getCity());
            tvDates.setText(trip.getDateRange());

            String cost = trip.getCostDisplay();
            if (cost != null && !cost.trim().isEmpty()) {
                tvCost.setVisibility(View.VISIBLE);
                tvCost.setText(cost);
            } else {
                tvCost.setVisibility(View.GONE);
            }

            applyStatusBadge(trip.getStatus());

            btnReplan.setOnClickListener(v -> {
                if (listener != null) listener.onReplan(trip);
            });
            btnDetails.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetails(trip);
            });
            btnReview.setOnClickListener(v -> {
                if (listener != null) listener.onReview(trip);
            });
        }

        private void applyStatusBadge(TripStatus status) {
            int bg;
            int fg;
            String label;
            switch (status) {
                case UPCOMING:
                    label = "Sắp tới";
                    bg = R.color.orange_pastel;
                    fg = R.color.slate_800;
                    break;
                case ONGOING:
                    label = "Đang diễn ra";
                    bg = R.color.light_green_pastel;
                    fg = R.color.emerald_700;
                    break;
                case COMPLETED:
                default:
                    label = "Đã hoàn thành";
                    bg = R.color.slate_500;
                    fg = R.color.white;
                    break;
            }
            tvBadge.setText(label);
            tvBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), fg));
            GradientDrawable d = new GradientDrawable();
            d.setCornerRadius(dp(14));
            d.setColor(ContextCompat.getColor(itemView.getContext(), bg));
            tvBadge.setBackground(d);
        }

        private float dp(float v) {
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, v, itemView.getContext().getResources().getDisplayMetrics());
        }
    }
}
