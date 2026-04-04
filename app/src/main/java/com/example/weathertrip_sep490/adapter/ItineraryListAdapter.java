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
import com.example.weathertrip_sep490.model.ItineraryRow;
import com.example.weathertrip_sep490.model.ItinerarySegmentRow;
import com.example.weathertrip_sep490.model.ItineraryStopRow;

import java.util.ArrayList;
import java.util.List;

public class ItineraryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SEGMENT = 0;
    private static final int TYPE_STOP = 1;

    private final List<ItineraryRow> items = new ArrayList<>();

    public void updateData(List<ItineraryRow> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public boolean isStopPosition(int position) {
        if (position < 0 || position >= items.size()) return false;
        return items.get(position) instanceof ItineraryStopRow;
    }

    @Override
    public int getItemViewType(int position) {
        ItineraryRow r = items.get(position);
        if (r instanceof ItinerarySegmentRow) return TYPE_SEGMENT;
        return TYPE_STOP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SEGMENT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_itinerary_segment, parent, false);
            return new SegmentVH(v);
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_itinerary_stop, parent, false);
        return new StopVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SegmentVH) {
            ItinerarySegmentRow s = (ItinerarySegmentRow) items.get(position);
            ((SegmentVH) holder).bind(s);
        } else if (holder instanceof StopVH) {
            ItineraryStopRow s = (ItineraryStopRow) items.get(position);
            ((StopVH) holder).bind(s);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SegmentVH extends RecyclerView.ViewHolder {
        private final TextView tvPill;
        private final TextView tvCity;
        private final TextView tvTemp;

        SegmentVH(@NonNull View itemView) {
            super(itemView);
            tvPill = itemView.findViewById(R.id.tvSegmentPill);
            tvCity = itemView.findViewById(R.id.tvSegmentCity);
            tvTemp = itemView.findViewById(R.id.tvSegmentTemp);
        }

        void bind(ItinerarySegmentRow s) {
            tvPill.setText(s.getSegmentLabel());
            tvCity.setText(s.getCityName());
            tvTemp.setText(s.getWeatherTemp());
        }
    }

    static class StopVH extends RecyclerView.ViewHolder {
        private final TextView tvTimeStart;
        private final TextView tvTimeEnd;
        private final TextView tvRangeBar;
        private final TextView tvTempBar;
        private final TextView tvTitle;
        private final TextView tvLocation;
        private final TextView tvHours;
        private final TextView tvPrice;
        private final TextView tvNext;
        private final ImageView ivThumb;

        StopVH(@NonNull View itemView) {
            super(itemView);
            tvTimeStart = itemView.findViewById(R.id.tvStopTimeStart);
            tvTimeEnd = itemView.findViewById(R.id.tvStopTimeEnd);
            tvRangeBar = itemView.findViewById(R.id.tvStopTimeRangeBar);
            tvTempBar = itemView.findViewById(R.id.tvStopTempBar);
            tvTitle = itemView.findViewById(R.id.tvStopTitle);
            tvLocation = itemView.findViewById(R.id.tvStopLocation);
            tvHours = itemView.findViewById(R.id.tvStopHours);
            tvPrice = itemView.findViewById(R.id.tvStopPrice);
            tvNext = itemView.findViewById(R.id.tvStopNext);
            ivThumb = itemView.findViewById(R.id.imgStopThumb);
        }

        void bind(ItineraryStopRow s) {
            tvTimeStart.setText(s.getTimeStartCol());
            tvTimeEnd.setText(s.getTimeEndCol());

            tvRangeBar.setText(s.getTimeRange());
            tvTempBar.setText(s.getWeatherTemp());
            tvTitle.setText(s.getTitle());
            tvLocation.setText(s.getLocationLine());
            tvHours.setText(s.getOpeningHours());
            tvPrice.setText(s.getPriceText());
            tvNext.setText("Điểm tiếp theo: " + s.getNextDestination());

            Glide.with(itemView.getContext())
                    .load(s.getImageResId())
                    .centerCrop()
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .into(ivThumb);
        }
    }
}
