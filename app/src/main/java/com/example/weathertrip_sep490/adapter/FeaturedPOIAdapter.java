package com.example.weathertrip_sep490.adapter;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.model.POI;

import java.util.List;

public class FeaturedPOIAdapter extends RecyclerView.Adapter<FeaturedPOIAdapter.ViewHolder> {

    private final List<POI> poiList;

    public FeaturedPOIAdapter(List<POI> poiList) {
        this.poiList = poiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        POI poi = poiList.get(position);

        holder.txtPlaceName.setText(poi.getName());
        holder.txtPlaceCity.setText(poi.getCity());

        String url = poi.getPoiImgUrl();
        if (url == null || url.trim().isEmpty()) {
            holder.imgPlace.setImageResource(R.drawable.bg_image_placeholder);
            return;
        }
        Glide.with(holder.imgPlace)
                .load(url.trim())
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .centerCrop()
                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        android.util.Log.e("GLIDE_IMG", "Featured load failed url=" + url, e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.imgPlace);
    }

    @Override
    public int getItemCount() {
        return poiList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace;
        TextView txtPlaceName, txtPlaceCity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.imgPlace);
            txtPlaceName = itemView.findViewById(R.id.txtPlaceName);
            txtPlaceCity = itemView.findViewById(R.id.txtPlaceCity);
        }
    }
}