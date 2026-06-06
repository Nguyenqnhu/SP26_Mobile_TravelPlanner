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
    
    public interface OnSegmentEditListener {
        void onEditSegment(String segmentId, String segmentLabel);
    }
    
    private OnSegmentEditListener editListener;
    private boolean showEditButtons = true;

    public void setOnSegmentEditListener(OnSegmentEditListener listener) {
        this.editListener = listener;
    }

    public void setShowEditButtons(boolean show) {
        this.showEditButtons = show;
        notifyDataSetChanged();
    }

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
            ((SegmentVH) holder).bind(s, editListener, showEditButtons);
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
        private final View btnEdit;

        SegmentVH(@NonNull View itemView) {
            super(itemView);
            tvPill = itemView.findViewById(R.id.tvSegmentPill);
            tvCity = itemView.findViewById(R.id.tvSegmentCity);
            tvTemp = itemView.findViewById(R.id.tvSegmentTemp);
            btnEdit = itemView.findViewById(R.id.btnEditSegment);
        }

        void bind(ItinerarySegmentRow s, OnSegmentEditListener listener, boolean showEdit) {
            tvPill.setText(s.getSegmentLabel());
            tvCity.setText(s.getCityName());
            
            String tempVal = s.getWeatherTemp();
            if (tempVal == null || tempVal.trim().isEmpty() || tempVal.equals("--")) {
                tvTemp.setVisibility(View.GONE);
                ImageView ivIcon = itemView.findViewById(R.id.ivSegmentWeatherIcon);
                if (ivIcon != null) ivIcon.setVisibility(View.GONE);
            } else {
                tvTemp.setVisibility(View.VISIBLE);
                tvTemp.setText(tempVal);
                ImageView ivIcon = itemView.findViewById(R.id.ivSegmentWeatherIcon);
                if (ivIcon != null) {
                    ivIcon.setVisibility(View.VISIBLE);
                    if (tempVal.toLowerCase().contains("km")) {
                        ivIcon.setImageResource(R.drawable.ic_pin);
                    } else {
                        ivIcon.setImageResource(R.drawable.ic_sun);
                    }
                }
            }

            if (btnEdit != null) {
                btnEdit.setVisibility(showEdit ? View.VISIBLE : View.GONE);
                btnEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEditSegment(s.getSegmentId(), s.getCityName());
                    }
                });
            }
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
        private final View layoutAiReason;
        private final TextView tvAiReason;

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
            layoutAiReason = itemView.findViewById(R.id.layoutAiReason);
            tvAiReason = itemView.findViewById(R.id.tvStopAiReason);
        }

        void bind(ItineraryStopRow s) {
            tvTimeStart.setText(s.getTimeStartCol());
            tvTimeEnd.setText(s.getTimeEndCol());

            tvRangeBar.setText(s.getTimeRange());
            
            String tempVal = s.getWeatherTemp();
            if (tempVal == null || tempVal.trim().isEmpty() || tempVal.equals("--")) {
                tvTempBar.setVisibility(View.GONE);
                ImageView ivIcon = itemView.findViewById(R.id.ivStopWeatherIcon);
                if (ivIcon != null) ivIcon.setVisibility(View.GONE);
            } else {
                tvTempBar.setVisibility(View.VISIBLE);
                tvTempBar.setText(tempVal);
                ImageView ivIcon = itemView.findViewById(R.id.ivStopWeatherIcon);
                if (ivIcon != null) {
                    ivIcon.setVisibility(View.VISIBLE);
                    if (tempVal.toLowerCase().contains("km")) {
                        ivIcon.setImageResource(R.drawable.ic_pin);
                    } else {
                        ivIcon.setImageResource(R.drawable.ic_sun);
                    }
                }
            }

            tvTitle.setText(s.getTitle());
            tvLocation.setText(s.getLocationLine());
            tvHours.setText(s.getOpeningHours());
            tvPrice.setText(s.getPriceText());
            tvNext.setText("Điểm tiếp theo: " + s.getNextDestination());

            if (layoutAiReason != null && tvAiReason != null) {
                if (s.getAiReason() != null && !s.getAiReason().trim().isEmpty()) {
                    layoutAiReason.setVisibility(View.VISIBLE);
                    tvAiReason.setText("Gợi ý từ AI: " + s.getAiReason().trim());
                } else {
                    layoutAiReason.setVisibility(View.GONE);
                }
            }

            String imgUrl = s.getImageUrl();
            if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                Glide.with(itemView.getContext())
                         .load(imgUrl.trim())
                         .centerCrop()
                         .placeholder(R.drawable.bg_image_placeholder)
                         .error(R.drawable.bg_image_placeholder)
                         .into(ivThumb);
            } else {
                Glide.with(itemView.getContext())
                         .load(s.getImageResId())
                         .centerCrop()
                         .placeholder(R.drawable.bg_image_placeholder)
                         .error(R.drawable.bg_image_placeholder)
                         .into(ivThumb);
            }
        }
    }
}
