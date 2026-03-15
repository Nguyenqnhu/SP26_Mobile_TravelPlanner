package com.example.weathertrip_sep490.adapter;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.model.Destination;

import java.util.List;

public class DestinationGridAdapter extends RecyclerView.Adapter<DestinationGridAdapter.ViewHolder> {

    private final List<Destination> list;

    public DestinationGridAdapter(List<Destination> list) {
        this.list = list;
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
        Destination item = list.get(position);
        holder.txtName.setText(item.getName());
        holder.txtCity.setText(item.getCity());
        holder.txtRating.setText("★ " + item.getRating());
        holder.imgDestination.setImageResource(item.getImageResId());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateData(List<Destination> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDestination;
        TextView txtName, txtCity, txtRating;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setClipToOutline(true);
            }
            imgDestination = itemView.findViewById(R.id.imgDestination);
            txtName = itemView.findViewById(R.id.txtDestinationName);
            txtCity = itemView.findViewById(R.id.txtCity);
            txtRating = itemView.findViewById(R.id.txtRating);
        }
    }
}
