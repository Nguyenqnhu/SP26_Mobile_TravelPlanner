package com.example.weathertrip_sep490.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.model.SavedPromotionItem;

import java.util.ArrayList;
import java.util.List;

public class SavedPromotionsAdapter extends RecyclerView.Adapter<SavedPromotionsAdapter.Holder> {

    private final List<SavedPromotionItem> items = new ArrayList<>();

    public void submitList(List<SavedPromotionItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_promotion, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final TextView tvPromoTitle;
        private final TextView tvPoiName;
        private final TextView tvPromoTitleTop;
        private final TextView tvPromoDesc;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvPromoTitle = itemView.findViewById(R.id.tvSavedCouponPromoTitle);
            tvPoiName = itemView.findViewById(R.id.tvSavedCouponPoiName);
            tvPromoTitleTop = itemView.findViewById(R.id.tvSavedCouponPromoTitleTop);
            tvPromoDesc = itemView.findViewById(R.id.tvSavedCouponPromoDesc);
        }

        void bind(@NonNull SavedPromotionItem item) {
            String promoTitle = safe(item.getPromotionTitle(), "Ưu đãi");
            String poiName = safe(item.getPoiName(), safe(item.getAdvertisementTitle(), "Địa điểm"));
            String promoDesc = safe(item.getPromotionDescription(), "");

            tvPromoTitle.setText(promoTitle);
            tvPromoTitleTop.setText(promoTitle);
            tvPoiName.setText(poiName.isEmpty() ? " " : poiName);
            tvPromoDesc.setText(promoDesc.isEmpty() ? " " : promoDesc);
        }

        @NonNull
        private static String safe(String value, @NonNull String fallback) {
            if (value == null) return fallback;
            String v = value.trim();
            return v.isEmpty() ? fallback : v;
        }
    }
}

