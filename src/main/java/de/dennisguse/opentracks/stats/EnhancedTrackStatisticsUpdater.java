package de.dennisguse.opentracks.stats;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import de.dennisguse.opentracks.data.models.Speed;
import de.dennisguse.opentracks.data.models.TrackPoint;
import de.dennisguse.opentracks.stats.TrackStatistics;
import de.dennisguse.opentracks.stats.TrackStatisticsUpdater;

public class EnhancedTrackStatisticsUpdater extends TrackStatisticsUpdater {
    private static final double CHAIRLIFT_SPEED_THRESHOLD = 1.5; // meters/second
    private static final double ALTITUDE_CHANGE_FOR_CHAIRLIFT = 10.0; // meters

    public EnhancedTrackStatisticsUpdater() {
        super();
    }

    public void updateStatistics(List<TrackPoint> points, EnhancedTrackStatistics stats) {
        if (points == null || points.size() < 2) {
            return; // Not enough data to calculate anything
        }
//        EnhancedTrackStatistics stats = new EnhancedTrackStatistics();
        double totalDistance = 0.0;
        double totalSpeed = 0.0;
        double maxSpeed = 0.0;
        long totalTimeOnChairlift = 0L;
        Instant startTime = points.get(0).getTime();
        Instant endTime = points.get(points.size() - 1).getTime();
        String timeOfTheDay = startTime.atZone(ZoneOffset.UTC).toLocalTime().toString();
        
        double lat = Math.toRadians(points.get(0).getLatitude());
        double lon = Math.toRadians(points.get(0).getLongitude());
        TrackPoint top = points.get(0);
         TrackPoint startChairlift = points.get(0);
        
        TrackPoint previousPoint = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            TrackPoint currentPoint = points.get(i);

            //find the top point of the ski track
            if(Math.toRadians(currentPoint.getLatitude()) > lat || Math.toRadians(currentPoint.getLongitude()) > lon ){
                lat = Math.toRadians(currentPoint.getLatitude());
                lon = Math.toRadians(currentPoint.getLongitude());
                top = currentPoint;
            }

             if(Math.toRadians(currentPoint.getLatitude()) == lat || Math.toRadians(currentPoint.getLongitude()) == lon ){
                startChairlift = currentPoint;
            }
            
//            double speed = currentPoint.getSpeed();
            double speed = currentPoint.getSpeed().toMPS();
            totalSpeed += speed;
            if (speed > maxSpeed) {
                maxSpeed = speed;
            }

            double altitudeChange = currentPoint.getAltitude().toM() - previousPoint.getAltitude().toM(); // Extract altitude value from Altitude object
            if (speed < CHAIRLIFT_SPEED_THRESHOLD && altitudeChange > ALTITUDE_CHANGE_FOR_CHAIRLIFT) {
                // Assuming this segment is chairlift usage
                totalTimeOnChairlift += currentPoint.getTime().toEpochMilli() - previousPoint.getTime().toEpochMilli();
            } else {
                // Calculate distance for skiing segments only
                totalDistance += distanceBetweenPoints(previousPoint, currentPoint);
            }

            previousPoint = currentPoint;
        }

        Duration totalTime = Duration.between(startTime, endTime).dividedBy(1000); // convert to seconds
        double averageSpeed = totalDistance / totalTime.getSeconds();
        
        // Update statistics
        stats.setTotalTime(totalTime);
        stats.setMovingTime(totalTime.minusMillis(totalTimeOnChairlift)); // convert to seconds
        stats.setTopSpeed(maxSpeed);
        stats.setAverageSpeed(Speed.of(averageSpeed));
        // Assuming slope calculation and waiting time require additional context not provided here

        double slope = slope(points.get(0),top);
        stats.setAverageSlope(slope);

        long waitingTimeForChairlift = Duration.between(points.get(0).getTime() ,startChairlift.getTime()).dividedBy(1000).getSeconds();
        stats.setWaitingTimeForChairlift(waitingTimeForChairlift*1000);

        long totalTravelTime = Duration.between(startTime,endTime).dividedBy(1000).getSeconds();
        stats.setTotalTravelTime(totalTravelTime);

        stats.setTimeOfTheDay(timeOfTheDay);

        // Set calculated values
        stats.setTimeOnChairlift(totalTimeOnChairlift);
    }

    private double distanceBetweenPoints(TrackPoint a, TrackPoint b) {
        final double R = 6371e3; // Earth's radius in meters
        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double lon1 = Math.toRadians(a.getLongitude());
        double lon2 = Math.toRadians(b.getLongitude());

        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;

        // Haversine formula
        double aSin = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(aSin), Math.sqrt(1 - aSin));

        return R * c;
    }

     private double slope(TrackPoint a, TrackPoint b) {
        double S=0;
        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double lon1 = Math.toRadians(a.getLongitude());
        double lon2 = Math.toRadians(b.getLongitude());

        double deltaLat = Math.abs(lat2 - lat1);
        double deltaLon = Math.abs(lon2 - lon1);

        S = (deltaLat/deltaLon) * 100;


        return S;
    }

}
