package de.dennisguse.opentracks.services;

import android.content.Context;

import de.dennisguse.opentracks.data.ContentProviderUtils;
import de.dennisguse.opentracks.data.TrackPointIterator;
import de.dennisguse.opentracks.data.models.*;

/**
 * Calculates the total distance skied along a track.
 * This class uses GPS track points to calculate the total distance skied.
 */
public class TrackDistanceSkied {
    private final ContentProviderUtils contentProviderUtils;
    private final Track.Id trackId;
    private double totalDistanceSkied;
    private TrackPoint prevTrackPoint;

    /**
     * Constructs a TrackDistanceSkied object.
     *
     * @param tid The ID of the track for which to calculate distance skied.
     * @param c   The context of the application.
     */
    public TrackDistanceSkied(Track.Id tid, Context c) {
        trackId = tid;
        contentProviderUtils = new ContentProviderUtils(c);
        totalDistanceSkied = 0.0;
        prevTrackPoint = null;
    }

    /**
     * Calculates the total distance skied along the track.
     *
     * @return The total distance skied in kilometers.
     */
    public double distanceSkied() {
        try (TrackPointIterator tpi = contentProviderUtils.getTrackPointLocationIterator(trackId, null)) {
            while (tpi.hasNext()) {
                TrackPoint currentTrackPoint = tpi.next();

                if (prevTrackPoint != null) {
                    double distance = calculateDistance(prevTrackPoint, currentTrackPoint);
                    totalDistanceSkied += distance;
                }

                prevTrackPoint = currentTrackPoint;
            }
        }
        return totalDistanceSkied;
    }

    /**
     * Calculates the distance between two track points.
     *
     * @param prevTrackPoint    The previous track point.
     * @param currentTrackPoint The current track point.
     * @return The distance between the two track points in kilometers.
     */
    private double calculateDistance(TrackPoint prevTrackPoint, TrackPoint currentTrackPoint) {
        final int R = 6371; // Radius of the Earth

        double lat1 = Math.toRadians(prevTrackPoint.getLatitude());
        double lon1 = Math.toRadians(prevTrackPoint.getLongitude());
        double alt1 = prevTrackPoint.getAltitude().toM();;
        double lat2 = Math.toRadians(currentTrackPoint.getLatitude());
        double lon2 = Math.toRadians(currentTrackPoint.getLongitude());
        double alt2 = currentTrackPoint.getAltitude().toM();

        double latDistance = lat2 - lat1;
        double lonDistance = lon2 - lon1;
        double altDistance = alt2 - alt1;

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(altDistance / 2) * Math.sin(altDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Distance in kilometers

        return distance;
    }
}
