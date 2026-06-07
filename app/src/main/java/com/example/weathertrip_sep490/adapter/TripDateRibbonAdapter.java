package com.example.weathertrip_sep490.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.model.TripRibbonDay;

import java.util.ArrayList;
import java.util.List;

public class TripDateRibbonAdapter extends RecyclerView.Adapter<TripDateRibbonAdapter.VH> {

    public interface OnDaySelectedListener {
        void onDaySelected(int position, @NonNull TripRibbonDay day);
    }

    private final List<TripRibbonDay> days = new ArrayList<>();
    private int selectedPosition = 0;
    private final OnDaySelectedListener listener;

    public TripDateRibbonAdapter(@NonNull OnDaySelectedListener listener) {
        this.listener = listener;
    }

    public void setDays(@NonNull List<TripRibbonDay> items) {
        days.clear();
        days.addAll(items);
        if (selectedPosition >= days.size()) {
            selectedPosition = Math.max(0, days.size() - 1);
        }
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        if (position < 0 || position >= days.size() || position == selectedPosition) return;
        int old = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(old);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_ribbon_day, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TripRibbonDay d = days.get(position);
        holder.tvWeekday.setText(d.getWeekdayShort());
        holder.tvNum.setText(String.valueOf(d.getDayOfMonth()));

        boolean sel = position == selectedPosition;
        holder.vCircle.setBackgroundResource(
                sel ? R.drawable.bg_ribbon_day_selected : R.drawable.bg_ribbon_day_unselected);
        int numColor = ContextCompat.getColor(
                holder.itemView.getContext(),
                sel ? R.color.white : R.color.itinerary_header_green);
        holder.tvNum.setTextColor(numColor);
        int wdColor = ContextCompat.getColor(
                holder.itemView.getContext(),
                sel ? R.color.itinerary_header_green : R.color.slate_500);
        holder.tvWeekday.setTextColor(wdColor);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            setSelectedPosition(pos);
            listener.onDaySelected(pos, days.get(pos));
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    @NonNull
    public TripRibbonDay getDayAt(int index) {
        return days.get(index);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvWeekday;
        final TextView tvNum;
        final View vCircle;

        VH(@NonNull View itemView) {
            super(itemView);
            tvWeekday = itemView.findViewById(R.id.tvRibbonWeekday);
            tvNum = itemView.findViewById(R.id.tvRibbonDayNum);
            vCircle = itemView.findViewById(R.id.vRibbonDayCircle);
        }
    }
}
