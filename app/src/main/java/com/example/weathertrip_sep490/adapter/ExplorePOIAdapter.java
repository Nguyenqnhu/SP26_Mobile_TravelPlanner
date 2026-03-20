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
import com.example.weathertrip_sep490.ui.ExplorePOIDetailActivity;

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
        holder.txtExploreOpen.setText(buildOpenHoursText(poi));
        holder.txtExploreCost.setText(
                poi.getApproxCost() == null || poi.getApproxCost().isEmpty()
                        ? "Chưa cập nhật"
                        : poi.getApproxCost()
        );

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
            String id = poi.getId();
            if (id == null || id.trim().isEmpty()) return;
            Intent intent = new Intent(v.getContext(), ExplorePOIDetailActivity.class);
            intent.putExtra(ExplorePOIDetailActivity.EXTRA_POI_ID, id.trim());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return poiList.size();
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    private String buildOpenHoursText(POI poi) {
        if (poi == null) return "Chưa có giờ mở cửa";
        if (poi.isIs24Hours()) return "Mở cửa 24/7";

        String open = safe(poi.getOpenHour()).trim();
        String close = safe(poi.getCloseHour()).trim();

        // Nếu backend trả "HH:mm:ss" thì lấy "HH:mm"
        if (open.length() >= 5) open = open.substring(0, 5);
        if (close.length() >= 5) close = close.substring(0, 5);

        if (open.isEmpty() || close.isEmpty()) return "Chưa có giờ mở cửa";
        return open + "-" + close;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgExplore;
        TextView txtExploreName, txtExploreCity, txtExploreOpen, txtExploreCost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgExplore = itemView.findViewById(R.id.imgExplore);
            txtExploreName = itemView.findViewById(R.id.txtExploreName);
            txtExploreCity = itemView.findViewById(R.id.txtExploreCity);
            txtExploreOpen = itemView.findViewById(R.id.txtExploreOpen);
            txtExploreCost = itemView.findViewById(R.id.txtExploreCost);
        }
    }
}
