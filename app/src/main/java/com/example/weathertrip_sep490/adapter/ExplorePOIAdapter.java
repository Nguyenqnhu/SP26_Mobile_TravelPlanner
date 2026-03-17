package com.example.weathertrip_sep490.adapter;



import android.content.Intent;
import android.net.Uri;
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

public class ExplorePOIAdapter extends RecyclerView.Adapter<ExplorePOIAdapter.ViewHolder> {

    private final List<POI> poiList;

    public ExplorePOIAdapter(List<POI> poiList) {
        this.poiList = poiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_explore_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        POI poi = poiList.get(position);

        holder.txtExploreName.setText(safe(poi.getName()));
        holder.txtExploreCity.setText(safe(poi.getCity()));
        holder.txtExploreAddress.setText(safe(poi.getAddress()));
        holder.txtExploreOpen.setText(
                poi.getOpeningHours() == null || poi.getOpeningHours().isEmpty()
                        ? "Chưa có giờ mở cửa"
                        : poi.getOpeningHours()
        );
        holder.txtExploreCost.setText(
                poi.getApproxCost() == null || poi.getApproxCost().isEmpty()
                        ? "Chưa cập nhật"
                        : poi.getApproxCost()
        );
        holder.txtIndoor.setText(poi.isIndoor() ? "Trong nhà" : "Ngoài trời");

        String url = poi.getPoiImgUrl();
        if (url == null || url.trim().isEmpty()) {
            holder.imgExplore.setImageResource(R.drawable.bg_image_placeholder);
        } else {
            Glide.with(holder.imgExplore)
                    .load(url.trim())
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .centerCrop()
                    .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            android.util.Log.e("GLIDE_IMG", "Explore list load failed url=" + url, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.imgExplore);
        }

        holder.itemView.setOnClickListener(v -> {
            if (poi.getGoogleMapLink() != null && !poi.getGoogleMapLink().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(poi.getGoogleMapLink()));
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return poiList.size();
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgExplore;
        TextView txtExploreName, txtExploreCity, txtExploreAddress, txtExploreOpen, txtExploreCost, txtIndoor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgExplore = itemView.findViewById(R.id.imgExplore);
            txtExploreName = itemView.findViewById(R.id.txtExploreName);
            txtExploreCity = itemView.findViewById(R.id.txtExploreCity);
            txtExploreAddress = itemView.findViewById(R.id.txtExploreAddress);
            txtExploreOpen = itemView.findViewById(R.id.txtExploreOpen);
            txtExploreCost = itemView.findViewById(R.id.txtExploreCost);
            txtIndoor = itemView.findViewById(R.id.txtIndoor);
        }
    }
}
