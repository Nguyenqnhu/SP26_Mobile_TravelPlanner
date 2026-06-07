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
import com.example.weathertrip_sep490.model.POI;

import java.util.ArrayList;
import java.util.List;

public class PoiGridAdapter extends RecyclerView.Adapter<PoiGridAdapter.ViewHolder> {

    public interface OnPoiClickListener {
        void onClick(POI poi);
    }

    private final List<POI> list = new ArrayList<>();
    private final OnPoiClickListener listener;

    public PoiGridAdapter(OnPoiClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_destination_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        POI item = list.get(position);
        holder.txtName.setText(safe(item != null ? item.getName() : null));
        holder.txtCity.setText(safe(item != null ? item.getCity() : null));
        holder.txtRating.setText("★ —");

        String url = item != null ? item.getPoiImgUrl() : null;
        if (url == null || url.trim().isEmpty()) {
            holder.imgDestination.setImageResource(R.drawable.sampleplace);
        } else {
            Glide.with(holder.imgDestination)
                    .load(url.trim())
                    .placeholder(R.drawable.sampleplace)
                    .error(R.drawable.sampleplace)
                    .centerCrop()
                    .into(holder.imgDestination);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && item != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateData(List<POI> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgDestination;
        final TextView txtName;
        final TextView txtCity;
        final TextView txtRating;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDestination = itemView.findViewById(R.id.imgDestination);
            txtName = itemView.findViewById(R.id.txtDestinationName);
            txtCity = itemView.findViewById(R.id.txtCity);
            txtRating = itemView.findViewById(R.id.txtRating);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}

