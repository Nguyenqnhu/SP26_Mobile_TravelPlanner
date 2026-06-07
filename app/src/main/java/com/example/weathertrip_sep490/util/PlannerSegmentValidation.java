package com.example.weathertrip_sep490.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.weathertrip_sep490.model.TripSegmentResponse;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Kiểm tra nhẹ dữ liệu chặng trước khi gọi POST planner/.../generate (phía app). */
public final class PlannerSegmentValidation {

    private PlannerSegmentValidation() {}

    public static boolean segmentsLookValidForAi(@Nullable List<TripSegmentResponse> segs) {
        if (segs == null || segs.size() < 2) return false;
        for (TripSegmentResponse s : segs) {
            if (s == null || s.getStartDate() == null || s.getEndDate() == null) return false;
            if (dayStartMillis(s.getEndDate()) < dayStartMillis(s.getStartDate())) return false;
        }
        return true;
    }

    private static long dayStartMillis(@NonNull Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
