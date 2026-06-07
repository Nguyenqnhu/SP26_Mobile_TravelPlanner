package com.example.weathertrip_sep490.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;

import java.util.ArrayList;
import java.util.List;

public class TripDayBandAdapter extends RecyclerView.Adapter<TripDayBandAdapter.VH> {

    public interface OnDayClickListener {
        void onDayClick(int position, String label);
    }

    private final List<String> items = new ArrayList<>();
    private int selectedPosition = 0;
    private final OnDayClickListener listener;

    public TripDayBandAdapter(@NonNull List<String> items, @NonNull OnDayClickListener listener) {
        this.items.addAll(items);
        this.listener = listener;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        if (position < 0 || position >= items.size() || position == selectedPosition) return;
        int old = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(old);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_day_pill, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String label = items.get(position);
        holder.tv.setText(label);

        if (position == selectedPosition) {
            holder.tv.setBackgroundResource(R.drawable.bg_chip_selected);
            holder.tv.setTextColor(0xFFFFFFFF);
        } else {
            holder.tv.setBackgroundResource(R.drawable.bg_chip_unselected);
            holder.tv.setTextColor(0xFF111827);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            setSelectedPosition(pos);
            listener.onDayClick(pos, label);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tv;

        VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvDayPill);
        }
    }
}

