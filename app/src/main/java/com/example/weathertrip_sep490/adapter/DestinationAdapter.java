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

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {

    private final List<Destination> list;

    public DestinationAdapter(List<Destination> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_destination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination item = list.get(position);
        holder.txtTag.setText("#Địa điểm");
        holder.txtName.setText(item.getName() + ", " + item.getCity());
        holder.txtRating.setText(item.getRating() + " ★");
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
        TextView txtTag, txtName, txtRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setClipToOutline(true);
            }
            imgDestination = itemView.findViewById(R.id.imgDestination);
            txtTag = itemView.findViewById(R.id.txtTag);
            txtName = itemView.findViewById(R.id.txtDestinationName);
            txtRating = itemView.findViewById(R.id.txtRating);
        }
    }
}
