package com.example.weathertrip_sep490.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;

public class HeroBannerAdapter extends RecyclerView.Adapter<HeroBannerAdapter.VH> {

    private final int[] slideDrawables;

    public HeroBannerAdapter(int[] slideDrawables) {
        this.slideDrawables = slideDrawables != null ? slideDrawables : new int[0];
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hero_banner, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        int res = slideDrawables[position % slideDrawables.length];
        holder.image.setImageResource(res);
    }

    @Override
    public int getItemCount() {
        return slideDrawables.length;
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivHeroSlide);
        }
    }
}
