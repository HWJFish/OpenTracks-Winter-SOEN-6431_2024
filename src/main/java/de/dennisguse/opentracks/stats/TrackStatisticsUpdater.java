/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package de.dennisguse.opentracks.stats;

import androidx.annotation.NonNull;

import java.time.Duration;
import java.util.List;

 import de.dennisguse.opentracks.data.models.Distance;
 import de.dennisguse.opentracks.data.models.HeartRate;
 import de.dennisguse.opentracks.data.models.Speed;
 import de.dennisguse.opentracks.data.models.TrackPoint;
 import de.dennisguse.opentracks.data.models.Run;

/**
 * Updater for {@link TrackStatistics}.
 * For updating track {@link TrackStatistics} as new {@link TrackPoint}s are added.
 * NOTE: Some of the locations represent pause/resume separator.
 * NOTE: Has still support for segments (at the moment unused).
 *
 * @author Sandor Dornbush
 * @author Rodrigo Damazio
 */
public class TrackStatisticsUpdater {

    private static final String TAG = TrackStatisticsUpdater.class.getSimpleName();

     private final TrackStatistics trackStatistics;
     private SessionManager sessionManager; // Added field for SessionManager

    private float averageHeartRateBPM;
    private Duration totalHeartRateDuration = Duration.ZERO;

    // The current segment's statistics
    private final TrackStatistics currentSegment;
    // Current segment's last trackPoint
    private TrackPoint lastTrackPoint;

    public TrackStatisticsUpdater() {
        this(new TrackStatistics());
    }

    /**
     * Creates a new{@link TrackStatisticsUpdater} with a {@link TrackStatisticsUpdater} already existed.
     *
     * @param trackStatistics a {@link TrackStatisticsUpdater}
     */
    public TrackStatisticsUpdater(TrackStatistics trackStatistics) {
        this.trackStatistics = trackStatistics;
        this.currentSegment = new TrackStatistics();

        resetAverageHeartRate();
    }

     public TrackStatisticsUpdater(TrackStatisticsUpdater toCopy) {
         this.currentSegment = new TrackStatistics(toCopy.currentSegment);
         this.trackStatistics = new TrackStatistics(toCopy.trackStatistics);
         this.sessionManager = toCopy.sessionManager;
         this.lastTrackPoint = toCopy.lastTrackPoint;
         resetAverageHeartRate();
     }

    public TrackStatistics getTrackStatistics() {
        // Take a snapshot - we don't want anyone messing with our trackStatistics
        TrackStatistics stats = new TrackStatistics(trackStatistics);
        stats.merge(currentSegment);
        return stats;
    }

     public void addTrackPoints(List<TrackPoint> trackPoints) {
         trackPoints.stream().forEachOrdered(this::addTrackPoint);
         List<Run> runs = RunAnalyzer.identifyRuns(sessionManager.getSessionId(), trackPoints); // Identify runs
         RunAnalyzer.calculateMaxSpeedPerRun(runs); // Calculate max speed for each run
         RunAnalyzer.calculateAvgSpeedStatistics(runs); // Calculate avg speed for each run
         // Add runs to the session
         for (Run run : runs) {
             currentSegment.setMaximumSpeedPerRun((float) run.getMaxSpeed());
             currentSegment.setAverageSpeedPerRun(run.getAverageSpeed());
         }
     }

    /**
     *
     */
    public void addTrackPoint(TrackPoint trackPoint) {
        if (trackPoint.isSegmentManualStart()) {
            reset(trackPoint);
        }

        if (!currentSegment.isInitialized()) {
            currentSegment.setStartTime(trackPoint.getTime());
        }

        // Always update time
        currentSegment.setStopTime(trackPoint.getTime());
        currentSegment.setTotalTime(Duration.between(currentSegment.getStartTime(), trackPoint.getTime()));

        // Process sensor data: barometer
        if (trackPoint.hasAltitudeGain()) {
            currentSegment.addTotalAltitudeGain(trackPoint.getAltitudeGain());
        }

        if (trackPoint.hasAltitudeLoss()) {
            currentSegment.addTotalAltitudeLoss(trackPoint.getAltitudeLoss());
        }

        // this function will always be called for all trackpoints to check if it is waiting for chairlift
        // and also modify values for the check according to current trackpoint.
        if (isWaitingForChairlift(trackPoint)){
            Duration passedDuration = Duration.between(lastTrackPoint.getTime(), trackPoint.getTime());
            currentSegment.setTotalChairliftWaitingTime(currentSegment.getTotalChairliftWaitingTime().plus(passedDuration));
        }

        //Update absolute (GPS-based) altitude
        if (trackPoint.hasAltitude()) {
            currentSegment.updateAltitudeExtremities(trackPoint.getAltitude());
        }

        // Update heart rate
        if (trackPoint.hasHeartRate() && lastTrackPoint != null) {
            Duration trackPointDuration = Duration.between(lastTrackPoint.getTime(), trackPoint.getTime());
            Duration newTotalDuration = totalHeartRateDuration.plus(trackPointDuration);

            averageHeartRateBPM = (totalHeartRateDuration.toMillis() * averageHeartRateBPM + trackPointDuration.toMillis() * trackPoint.getHeartRate().getBPM()) / newTotalDuration.toMillis();
            totalHeartRateDuration = newTotalDuration;

            currentSegment.setAverageHeartRate(HeartRate.of(averageHeartRateBPM));
        }

        {
            // Update total distance
            Distance movingDistance = null;
            if (trackPoint.hasSensorDistance()) {
                movingDistance = trackPoint.getSensorDistance();
            } else if (lastTrackPoint != null
                    && lastTrackPoint.hasLocation()
                    && trackPoint.hasLocation()) {
                // GPS-based distance/speed
                movingDistance = trackPoint.distanceToPrevious(lastTrackPoint);
            }
            if (movingDistance != null) {
                currentSegment.setIdle(false);
                currentSegment.addTotalDistance(movingDistance);
            }

            if (!currentSegment.isIdle() && !trackPoint.isSegmentManualStart()) {
                if (lastTrackPoint != null) {
                    currentSegment.addMovingTime(trackPoint, lastTrackPoint);
                }
            }

            if (trackPoint.getType() == TrackPoint.Type.IDLE) {
                currentSegment.setIdle(true);
            }

            if (trackPoint.hasSpeed()) {
                updateSpeed(trackPoint);
            }

            // Update current Slope= Change in Distance / Change in Altitude
            if (movingDistance != null) {
                updateSlopePercent(trackPoint, movingDistance);
            }
        }

        if (trackPoint.isSegmentManualEnd()) {
            reset(trackPoint);
            return;
        }

        lastTrackPoint = trackPoint;
    }

    /**
     * This function can be used to check if current trackpoint is of type waiting for chairlift.
     * It returns true if it is waiting at lower end of the track for more than 5 trackpoints.
     * else it increments the counter if it is still at lower end of track.
     * */
    private boolean isWaitingForChairlift(TrackPoint trackPoint) {
        // indicates altitude difference allowed in case of small change elevation change while waiting in queue for chairlift
        final float minimumAltitudeChangeAllowed = 0.2f;
        final float minimumDeviationAllowedFromLowestAltitude = 1f;
        final int thresholdTrackpointsForNoMovement=5;

        float altitudeGain = trackPoint.hasAltitudeGain()? trackPoint.getAltitudeGain(): 0f;
        float altitudeLoss = trackPoint.hasAltitudeLoss()? trackPoint.getAltitudeLoss(): 0f;
        float currentAltitudeChange = Math.max(altitudeGain, altitudeLoss);


        if (currentAltitudeChange<=minimumAltitudeChangeAllowed
                && trackPoint.getAltitude()!=null
                && Math.abs(currentSegment.getMinAltitude()-trackPoint.getAltitude().toM())<=minimumDeviationAllowedFromLowestAltitude){
            currentSegment.incrementEndOfRunCounter();
            if (currentSegment.getEndOfRunCounter()>=thresholdTrackpointsForNoMovement){
                return true;
            }
        } else {
            currentSegment.resetEndOfRunCounter();
            return false;
        }

        return false;
    }

    private void reset(TrackPoint trackPoint) {
        if (currentSegment.isInitialized()) {
            trackStatistics.merge(currentSegment);
        }
        currentSegment.reset(trackPoint.getTime());

        lastTrackPoint = null;
        resetAverageHeartRate();
    }

    private void resetAverageHeartRate() {
        averageHeartRateBPM = 0.0f;
        totalHeartRateDuration = Duration.ZERO;
    }

    /**
     * Updates a speed reading while assuming the user is moving.
     */
    private void updateSpeed(@NonNull TrackPoint trackPoint) {
        Speed currentSpeed = trackPoint.getSpeed();
        if (currentSpeed.greaterThan(currentSegment.getMaxSpeed())) {
            currentSegment.setMaxSpeed(currentSpeed);
        }
    }

    /**
     * Updates the slope percent assuming the user has moved
     */
    private void updateSlopePercent(@NonNull TrackPoint trackPoint, Distance distanceMoved) {
        Float altituteChanged = null;

        // absolute (GPS-based) altitude
        if (altituteChanged == null && trackPoint.hasAltitude() && lastTrackPoint != null) {
            altituteChanged = (float) (trackPoint.getAltitude().toM() - lastTrackPoint.getAltitude().toM());
        }

        if (altituteChanged == null) {
            if (trackPoint.hasAltitudeGain()) {
                altituteChanged = trackPoint.getAltitudeGain();
            } else if (trackPoint.hasAltitudeLoss()) {
                altituteChanged = -trackPoint.getAltitudeLoss();
            }
        }

        if (altituteChanged == null) {
            return;
        }

        // Slope = Change in Distance / Change in Altitude
        Float slopePercentChangedBetweenPoints = (float) ((altituteChanged / distanceMoved.toM()) * 100);
        Float prevAggregatedSlopePercent = currentSegment.hasSlope() ? currentSegment.getSlopePercent() : 0;
        Float aggregatedSlopePercent = prevAggregatedSlopePercent + slopePercentChangedBetweenPoints;
        currentSegment.setSlopePercent(aggregatedSlopePercent);
    }

     @NonNull
     @Override
     public String toString() {
         return "TrackStatisticsUpdater{" +
                 "trackStatistics=" + trackStatistics +
                 '}';
     }
 }