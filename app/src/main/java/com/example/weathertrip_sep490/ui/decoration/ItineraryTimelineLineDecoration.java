package com.example.weathertrip_sep490.ui.decoration;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.adapter.ItineraryListAdapter;

/**
 * Vẽ một đường dọc liên tục thẳng xuống, thẳng hàng với vòng marker trên các dòng stop.
 * Bao trùm cả các hàng segment ở giữa để không bị “đứt đoạn”.
 */
public class ItineraryTimelineLineDecoration extends RecyclerView.ItemDecoration {

    private final ItineraryListAdapter adapter;
    private final float cxOffsetPx;
    private final float halfW;
    private final Paint paint;

    public ItineraryTimelineLineDecoration(@NonNull ItineraryListAdapter adapter, @NonNull Resources res) {
        this.adapter = adapter;
        float d = res.getDisplayMetrics().density;
        float w = Math.max(2f, 2f * d);
        this.halfW = w / 2f;
        this.cxOffsetPx = 11f * d;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFFD1D5DB);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        RecyclerView.LayoutManager lm = parent.getLayoutManager();
        if (!(lm instanceof LinearLayoutManager)) return;
        LinearLayoutManager llm = (LinearLayoutManager) lm;

        int first = llm.findFirstVisibleItemPosition();
        int last = llm.findLastVisibleItemPosition();
        if (first == RecyclerView.NO_POSITION || last == RecyclerView.NO_POSITION) return;

        float cx = -1f;
        for (int pos = first; pos <= last; pos++) {
            if (!adapter.isStopPosition(pos)) continue;
            View child = llm.findViewByPosition(pos);
            if (child != null) {
                cx = child.getLeft() + cxOffsetPx;
                break;
            }
        }
        if (cx < 0f) return;

        float top = Float.MAX_VALUE;
        float bottom = Float.MIN_VALUE;
        for (int pos = first; pos <= last; pos++) {
            View child = llm.findViewByPosition(pos);
            if (child == null) continue;
            top = Math.min(top, child.getTop());
            bottom = Math.max(bottom, child.getBottom());
        }
        if (bottom <= top) return;

        c.drawRect(cx - halfW, top, cx + halfW, bottom, paint);
    }
}
