package com.example.weathertrip_sep490.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.model.AdvertisementItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class AdsFeedAdapter extends RecyclerView.Adapter<AdsFeedAdapter.Holder> {

    public interface Listener {
        void onSaveClicked(@NonNull AdvertisementItem item);
        void onItemClicked(@NonNull AdvertisementItem item);
    }

    private final List<AdvertisementItem> items = new ArrayList<>();
    private final Listener listener;
    private final Set<String> expandedIds = new HashSet<>();
    private final Set<String> savedPromotionIds = new HashSet<>();

    public AdsFeedAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void updateData(List<AdvertisementItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void setPromotionSaved(@NonNull String promotionId, boolean saved) {
        String key = promotionId.trim();
        if (key.isEmpty()) return;
        if (saved) {
            savedPromotionIds.add(key);
        } else {
            savedPromotionIds.remove(key);
        }
        notifyDataSetChanged();
    }

    public void setSavedPromotionIds(@NonNull Collection<String> promotionIds) {
        savedPromotionIds.clear();
        for (String id : promotionIds) {
            if (id == null) continue;
            String key = id.trim();
            if (!key.isEmpty()) savedPromotionIds.add(key);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ads_feed_post, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(items.get(position), listener, expandedIds);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ImageView imgPartnerAvatar;
        private final TextView tvTitle;
        private final TextView tvPoiName;
        private final TextView tvPartnerName;
        private final TextView tvPartnerTime;
        private final TextView tvCouponPoiName;
        private final TextView tvCouponPromoTitle;
        private final TextView tvCouponPromoBadge;
        private final TextView tvCouponPromoDesc;
        private final View layoutCoupon;
        private final ImageView imgCover;
        private final TextView tvContent;
        private final TextView tvSeeMore;
        private final AppCompatImageButton btnSaveIcon;

        Holder(@NonNull View itemView) {
            super(itemView);
            imgPartnerAvatar = itemView.findViewById(R.id.imgAdsPartnerAvatar);
            tvTitle = itemView.findViewById(R.id.tvAdsTitle);
            tvPoiName = itemView.findViewById(R.id.tvAdsPoiName);
            tvPartnerName = itemView.findViewById(R.id.tvAdsPartnerName);
            tvPartnerTime = itemView.findViewById(R.id.tvAdsPartnerRole);
            tvCouponPoiName = itemView.findViewById(R.id.tvCouponPoiName);
            tvCouponPromoTitle = itemView.findViewById(R.id.tvCouponPromoTitle);
            tvCouponPromoBadge = itemView.findViewById(R.id.tvCouponPromoBadge);
            tvCouponPromoDesc = itemView.findViewById(R.id.tvCouponPromoDesc);
            layoutCoupon = itemView.findViewById(R.id.layoutAdsCoupon);
            imgCover = itemView.findViewById(R.id.imgAdsCover);
            tvContent = itemView.findViewById(R.id.tvAdsContent);
            tvSeeMore = itemView.findViewById(R.id.tvAdsSeeMore);
            btnSaveIcon = itemView.findViewById(R.id.btnAdsSaveIcon);
        }

        void bind(
                @NonNull AdvertisementItem item,
                @NonNull Listener listener,
                @NonNull Set<String> expandedIds
        ) {
            tvPartnerName.setText(textOrDefault(item.getPartnerName(), "Đối tác"));
            tvPartnerTime.setText(formatRelativeTime(item.getCreatedAt()));

            String title = textOrDefault(item.getTitle(), "Bài viết ưu đãi");
            tvTitle.setText(title);

            String poiName = textOrDefault(item.getPoiName(), "");
            tvPoiName.setText(poiName.isEmpty() ? " " : poiName);

            Glide.with(itemView.getContext())
                    .load(item.getPartnerAvatarUrl())
                    .placeholder(R.drawable.sample_avatar)
                    .error(R.drawable.sample_avatar)
                    .centerCrop()
                    .into(imgPartnerAvatar);

            boolean hasPromotion = item.getPromotion() != null
                    && item.getPromotion().getTitle() != null
                    && !item.getPromotion().getTitle().trim().isEmpty();
            if (hasPromotion) {
                String promoTitle = textOrDefault(item.getPromotion().getTitle(), "Ưu đãi");
                String promoDesc = textOrDefault(item.getPromotion().getDescription(), "");
                tvCouponPromoTitle.setText(promoTitle);
                tvCouponPromoBadge.setText(promoTitle);
                tvCouponPoiName.setText(poiName.isEmpty() ? "Địa điểm" : poiName);
                tvCouponPromoDesc.setText(promoDesc.isEmpty() ? " " : promoDesc);
                layoutCoupon.setVisibility(View.VISIBLE);
            } else {
                layoutCoupon.setVisibility(View.GONE);
            }

            String content = textOrDefault(item.getContent(), "");
            boolean isExpanded = item.getAdId() != null && expandedIds.contains(item.getAdId());
            tvContent.setText(content.isEmpty() ? " " : content);
            tvContent.setMaxLines(isExpanded ? 20 : 2);
            boolean shouldShowToggle = content.length() > 120;
            tvSeeMore.setVisibility(shouldShowToggle ? View.VISIBLE : View.GONE);
            tvSeeMore.setText(isExpanded ? "Thu gọn" : "Xem thêm");

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .centerCrop()
                    .into(imgCover);

            imgCover.setOnClickListener(v -> {
                String imageUrl = item.getImageUrl();
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    android.app.Dialog dialog = new android.app.Dialog(itemView.getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                    dialog.setContentView(R.layout.dialog_fullscreen_image);
                    ImageView imgFullscreen = dialog.findViewById(R.id.imgFullscreen);
                    View btnClose = dialog.findViewById(R.id.btnFullscreenClose);

                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.bg_image_placeholder)
                            .error(R.drawable.bg_image_placeholder)
                            .into(imgFullscreen);

                    btnClose.setOnClickListener(v1 -> dialog.dismiss());
                    imgFullscreen.setOnClickListener(v1 -> dialog.dismiss());
                    dialog.show();
                }
            });

            String promotionId = item.getPromotion() != null ? item.getPromotion().getPromotionId() : null;
            boolean canSave = promotionId != null && !promotionId.trim().isEmpty();
            AdsFeedAdapter adapter = null;
            RecyclerView.Adapter<?> binding = getBindingAdapter();
            if (binding instanceof AdsFeedAdapter) {
                adapter = (AdsFeedAdapter) binding;
            }
            boolean isSaved = canSave && adapter != null && adapter.savedPromotionIds.contains(promotionId.trim());
            btnSaveIcon.setImageResource(isSaved ? R.drawable.saved_button : R.drawable.not_save_button);
            btnSaveIcon.setEnabled(canSave);
            btnSaveIcon.setAlpha(canSave ? 1f : 0.35f);
            btnSaveIcon.setFocusable(true);
            btnSaveIcon.setClickable(true);

            View.OnClickListener saveClick = v -> {
                if (!canSave) return;
                String key = promotionId.trim();
                RecyclerView.Adapter<?> current = getBindingAdapter();
                if (!(current instanceof AdsFeedAdapter)) return;
                AdsFeedAdapter a = (AdsFeedAdapter) current;
                if (!a.savedPromotionIds.contains(key)) {
                    a.savedPromotionIds.add(key); // optimistic UI
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        a.notifyItemChanged(pos);
                    }
                }
                listener.onSaveClicked(item);
            };
            btnSaveIcon.setOnClickListener(saveClick);
            itemView.setOnClickListener(v -> listener.onItemClicked(item));

            tvSeeMore.setOnClickListener(v -> {
                if (item.getAdId() == null) return;
                if (expandedIds.contains(item.getAdId())) {
                    expandedIds.remove(item.getAdId());
                } else {
                    expandedIds.add(item.getAdId());
                }
                // refresh item
                RecyclerView.Adapter<?> a = getBindingAdapter();
                if (a != null) a.notifyItemChanged(getBindingAdapterPosition());
            });
        }

        @NonNull
        private static String textOrDefault(String value, @NonNull String fallback) {
            if (value == null || value.trim().isEmpty()) return fallback;
            return value.trim();
        }

        @NonNull
        private static String formatRelativeTime(String isoUtc) {
            if (isoUtc == null || isoUtc.trim().isEmpty()) return "Vừa xong";
            Date created = parseIsoUtc(isoUtc.trim());
            if (created == null) return "Vừa xong";

            long diffMs = Math.max(0L, System.currentTimeMillis() - created.getTime());
            long minutes = diffMs / (60 * 1000);
            if (minutes < 1) return "Vừa xong";
            if (minutes < 60) return minutes + " phút trước";
            long hours = minutes / 60;
            if (hours < 24) return hours + " giờ trước";
            long days = hours / 24;
            return days + " ngày trước";
        }

        private static Date parseIsoUtc(String value) {
            String[] patterns = new String[]{
                    "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'"
            };
            for (String pattern : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                    sdf.setLenient(false);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return sdf.parse(value);
                } catch (ParseException ignored) {
                }
            }
            return null;
        }
    }

}
