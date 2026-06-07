package com.example.weathertrip_sep490.ui;

/**
 * @deprecated Replaced by Map-based SelectRouteActivity
 */
@Deprecated
public class AddSegmentBottomSheet {
    public interface Listener {
        void onSegmentAddedAndReadyForAi(String tripId, String locationName, String segmentStartDate, String segmentEndDate, double latitude, double longitude);
    }
}
