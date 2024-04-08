package de.dennisguse.opentracks.data.models;

import androidx.annotation.NonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class TrackSegment {

    private static final String TAG = TrackSegment.class.getSimpleName();
    private TrackPoint.Id id;
    @NonNull
    private final Instant time;

    private List<TrackPoint> trackPoints;

    public TrackSegment(@NonNull Instant time) {
        this.time = time;
        trackPoints = new ArrayList<>();
    }

    public void addTrackPoint(TrackPoint point) {
        this.trackPoints.add(point);
    }

    public int getTrackPointCount() {
        return  trackPoints.size();
    }

    public Boolean hasTrackPoints() {
        return  !trackPoints.isEmpty();
    }

    /**
     * Returns the initial elevation of the track.
     * If the track contains at least one point, the elevation of the first point is returned.
     * If the track is empty, returns 0.
     * @return The initial elevation in meters.
     */
    public double getInitialElevation() {
        Optional<TrackPoint> firstPoint = trackPoints.stream().findFirst();
        if (firstPoint.isPresent()) {
            TrackPoint point = firstPoint.get();
            return point.getAltitude().toM();
        }
        return 0;
    }

    /**
     * Calculates the displacement of the track based on altitude gain and loss.
     * @return The total displacement in meters.
     */
    public long getDisplacement()
    {
        long displacement = 0;
        for (TrackPoint point: trackPoints)
        {
            if (point.hasAltitudeGain())
            {
                displacement += point.getAltitudeGain();
            }

            if (point.hasAltitudeLoss()) {
                displacement += point.getAltitudeLoss();
            }
        }

        return displacement;
    }


    /**
     * Calculates the total distance covered by the track.
     * @return The total distance covered as a Distance object.
     *         Returns null if the trackPoints list is null or empty.
     */
    public Distance getDistance() {
        if (trackPoints == null) {
            return null;
        }
        TrackPoint first = trackPoints.get(0);
        TrackPoint last = trackPoints.get(trackPoints.size() - 1);
        return last.distanceToPrevious(first);
    }

    /**
     * Calculates the total time duration of the track.
     * @return The total time duration as a Duration object.
     *         Returns null if the trackPoints list is null or empty.
     */
    public Duration getTotalTime(){

        if(trackPoints == null){
            return null;
        }
        TrackPoint startTime = trackPoints.get(0);
        TrackPoint endTime = trackPoints.get(trackPoints.size() - 1);

        return Duration.between(startTime.getTime(), endTime.getTime());
    }

    /**
     * Calculates the average speed of the track.
     * @return The average speed in meters per second (m/s).
     *         Returns NaN if the total time is zero (indicating division by zero).
     */
    public double getSpeed(){
        // in m/s
        double totalDistance = getDistance().toM();
        long totalTime = getTotalTime().toSeconds();
        return totalDistance/totalTime;
    }

    /**
     * Calculates the average distance between consecutive track points in the segment.
     *
     * @return The average distance as a Distance object.
     */
    public Distance getAvgDistance() {
        if (trackPoints.isEmpty()) {
            return Distance.of(0);
        }


        double totalDistance = 0;


        for (int i = 1; i < trackPoints.size(); i++) {
            TrackPoint previousPoint = trackPoints.get(i - 1);
            TrackPoint currentPoint = trackPoints.get(i);


            Distance distanceBetweenPoints = currentPoint.distanceToPrevious(previousPoint);


            totalDistance += distanceBetweenPoints.toM();
        }

        // Calculate the average distance by dividing the total distance by the number of segments
        double averageDistance = totalDistance / (trackPoints.size() - 1);


        return Distance.of(averageDistance);
    }

    /**
     * Calculates the average speed
     * @return The average speed in meters per second (m/s).
     *         Returns NaN if the total time is zero (indicating division by zero).
     */
    public double getAverageSpeed() {
        Distance distance = getDistance();
        Duration totalTime = getTotalTime();

        if (distance == null || totalTime == null || totalTime.isZero()) {
            return Double.NaN;
        }

        double totalDistance = distance.toM();
        long totalTimeSeconds = totalTime.getSeconds();
        return totalDistance / totalTimeSeconds;
    }

}