package de.dennisguse.opentracks.stats;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import de.dennisguse.opentracks.data.models.Speed;
import de.dennisguse.opentracks.data.models.TrackPoint;
import de.dennisguse.opentracks.stats.TrackStatistics;
import de.dennisguse.opentracks.stats.TrackStatisticsUpdater;

public class EnhancedTrackStatistics extends TrackStatistics {
    private double averageSlope; // In percentage
    private double topSpeed; // In meters/second
    private Speed averageSpeed; // In meters/second
    private long timeOnChairlift; // In milliseconds
    private long waitingTimeForChairlift; // In milliseconds
    private long totalTravelTime; //In seconds
    private String timeOfTheDay; // String


    // Constructor
    public EnhancedTrackStatistics() {
        super();
        this.averageSlope = 0.0;
        this.topSpeed = 0.0;
        this.averageSpeed = Speed.of(0);
        this.timeOnChairlift = 0L;
        this.waitingTimeForChairlift = 0L;
    }

    // Getters and setters
    public double getAverageSlope() {
        return averageSlope;
    }

    public void setAverageSlope(double averageSlope) {
        this.averageSlope = averageSlope;
    }

    public double getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(double topSpeed) {
        this.topSpeed = topSpeed;
    }

    public Speed getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(Speed averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public long getTimeOnChairlift() {
        return timeOnChairlift;
    }

    public void setTimeOnChairlift(long timeOnChairlift) {
        this.timeOnChairlift = timeOnChairlift;
    }

    public long getWaitingTimeForChairlift() {
        return waitingTimeForChairlift;
    }

    public void setWaitingTimeForChairlift(long waitingTimeForChairlift) {
        this.waitingTimeForChairlift = waitingTimeForChairlift;
    }

     public long getTotalTravelTime() { return totalTravelTime; }

    public void setTotalTravelTime(long totalTravelTime) {
        this.totalTravelTime = totalTravelTime;
    }

    public String getTimeOfTheDay() { return timeOfTheDay;}

    public void setTimeOfTheDay(String timeOfTheDay) {
        this.timeOfTheDay = timeOfTheDay;
    }
}
