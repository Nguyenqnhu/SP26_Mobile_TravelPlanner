package com.example.weathertrip_sep490.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.model.AdvertisementItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AdsPostDetailBottomSheet extends BottomSheetDialogFragment {

    private static final int COLLAPSED_MAX_LINES = 4;
    private static final int EXPANDED_MAX_LINES = 40;

    public static AdsPostDetailBottomSheet newInstance(@NonNull AdvertisementItem item) {
        AdsPostDetailBottomSheet f = new AdsPostDetailBottomSheet();
        Bundle b = new Bundle();

        b.putString("title", safe(item.getTitle()));
        b.putString("content", safe(item.getContent()));
        b.putString("imageUrl", safe(item.getImageUrl()));
        b.putString("poiName", safe(item.getPoiName()));
        b.putString("partnerName", safe(item.getPartnerName()));
        b.putString("partnerAvatarUrl", safe(item.getPartnerAvatarUrl()));
        b.putDouble("matchScore", item.getMatchScore());
        b.putDouble("matchPercentage", item.getMatchPercentage());
        b.putString("promotionTitle", item.getPromotion() != null ? safe(item.getPromotion().getTitle()) : "");
        b.putString("promotionDescription", item.getPromotion() != null ? safe(item.getPromotion().getDescription()) : "");
        f.setArguments(b);
        return f;
    }

    private static String safe(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_ads_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle b = getArguments() != null ? getArguments() : new Bundle();

        String poiName = b.getString("poiName", "");
        String title = b.getString("title", "");
        String content = b.getString("content", "");
        String imageUrl = b.getString("imageUrl", "");
        String partnerName = b.getString("partnerName", "");
        String promotionTitle = b.getString("promotionTitle", "");
        String promotionDescription = b.getString("promotionDescription", "");

        ((TextView) view.findViewById(R.id.tvAdsDetailPoiName)).setText(!poiName.isEmpty() ? poiName : "Chi tiết quảng cáo");
        ((TextView) view.findViewById(R.id.tvAdsDetailPartner)).setText(partnerName.isEmpty() ? "Đối tác" : partnerName);
        ((TextView) view.findViewById(R.id.tvAdsDetailTitle)).setText(title.isEmpty() ? "Tiêu đề quảng cáo" : title);
        TextView tvContent = view.findViewById(R.id.tvAdsDetailContent);
        TextView tvSeeMore = view.findViewById(R.id.tvAdsDetailSeeMore);

        String safeContent = content.isEmpty() ? "Không có nội dung mô tả." : content;
        tvContent.setText(safeContent);

        final boolean[] expanded = new boolean[]{false};
        tvContent.setMaxLines(COLLAPSED_MAX_LINES);
        boolean shouldShowToggle = safeContent.length() > 180;
        tvSeeMore.setVisibility(shouldShowToggle ? View.VISIBLE : View.GONE);
        tvSeeMore.setText("Xem thêm");
        tvSeeMore.setOnClickListener(v -> {
            expanded[0] = !expanded[0];
            tvContent.setMaxLines(expanded[0] ? EXPANDED_MAX_LINES : COLLAPSED_MAX_LINES);
            tvSeeMore.setText(expanded[0] ? "Thu gọn" : "Xem thêm");
        });

        String promo = promotionTitle.isEmpty() ? "Không có mã giảm" : (promotionTitle + (promotionDescription.isEmpty() ? "" : (" - " + promotionDescription)));
        ((TextView) view.findViewById(R.id.tvAdsDetailPromotion)).setText(promo);



        ImageView img = view.findViewById(R.id.imgAdsDetail);
        Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .centerCrop()
                .into(img);
    }
}
