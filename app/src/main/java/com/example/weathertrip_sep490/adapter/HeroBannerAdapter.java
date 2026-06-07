package com.example.weathertrip_sep490.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;

public class HeroBannerAdapter extends RecyclerView.Adapter<HeroBannerAdapter.VH> {

    public static class Slide {
        final int imageResId;
        final String title;
        final String subtitle;

        public Slide(int imageResId, String title, String subtitle) {
            this.imageResId = imageResId;
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    private final Slide[] slides;

    public HeroBannerAdapter(Slide[] slides) {
        this.slides = slides != null ? slides : new Slide[0];
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
        Slide slide = slides[position % slides.length];
        holder.image.setImageResource(slide.imageResId);
        holder.title.setText(slide.title);
        holder.subtitle.setText(slide.subtitle);
    }

    @Override
    public int getItemCount() {
        return slides.length;
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final android.widget.TextView title;
        final android.widget.TextView subtitle;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivHeroSlide);
            title = itemView.findViewById(R.id.tvHeroTitle);
            subtitle = itemView.findViewById(R.id.tvHeroSubtitle);
        }
    }
}
