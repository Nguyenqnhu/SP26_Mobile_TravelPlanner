package com.example.weathertrip_sep490.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;

import java.util.List;

public class FilterChipAdapter extends RecyclerView.Adapter<FilterChipAdapter.ChipViewHolder> {

    public interface OnChipClickListener {
        void onChipClick(int position, String value);
    }

    private final List<String> items;
    private int selectedPosition = 0;
    private final OnChipClickListener listener;

    public FilterChipAdapter(List<String> items, OnChipClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    /** Cập nhật nhãn chip (ví dụ số lượng) mà vẫn giữ vị trí đang chọn nếu hợp lệ. */
    public void setItems(List<String> newItems) {
        items.clear();
        items.addAll(newItems);
        if (selectedPosition >= items.size()) {
            selectedPosition = Math.max(0, items.size() - 1);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_chip, parent, false);
        return new ChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        String item = items.get(position);
        holder.txtChip.setText(item);

        if (position == selectedPosition) {
            holder.txtChip.setBackgroundResource(R.drawable.bg_chip_selected);
            holder.txtChip.setTextColor(0xFFFFFFFF);
        } else {
            holder.txtChip.setBackgroundResource(R.drawable.bg_chip_unselected);
            holder.txtChip.setTextColor(0xFF111827);
        }

        holder.txtChip.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onChipClick(selectedPosition, item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        TextView txtChip;

        public ChipViewHolder(@NonNull View itemView) {
            super(itemView);
            txtChip = itemView.findViewById(R.id.txtChip);
        }
    }
}