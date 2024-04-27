package de.dennisguse.opentracks.services.announcement;

import static android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceAverageHeartRate;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceAverageSpeedPace;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceLapHeartRate;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceLapSpeedPace;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceTimeSkiedRecording;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceMovingTime;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceTotalDistance;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceMaxSpeedRun;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceRunAverageSpeed;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceMaxSlope;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceAveragesloperecording;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceTemperature;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceMaxSpeedRecording;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceAverageSpeedRecording;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceAverageslopeRun;
import static de.dennisguse.opentracks.settings.PreferencesUtils.shouldVoiceAnnounceTotalWaitingTime;


import android.content.Context;
import android.icu.text.MessageFormat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TtsSpan;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.Duration;
import java.util.Map;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.data.models.Distance;
import de.dennisguse.opentracks.data.models.Speed;
import de.dennisguse.opentracks.settings.UnitSystem;
import de.dennisguse.opentracks.stats.SensorStatistics;
import de.dennisguse.opentracks.stats.TrackStatistics;
import de.dennisguse.opentracks.ui.intervals.IntervalStatistics;
import de.dennisguse.opentracks.services.WeatherFetchService;


class VoiceAnnouncementUtils {
    private static Double currentMaxSlope;

    private VoiceAnnouncementUtils() {
    }

    static double calculateMaxSlope() {
        // This method should return the calculated maximum slope.
        double maxSlope = currentMaxSlope;
        if (currentMaxSlope==null){
            return 0;
        }
        return maxSlope;
    }



    private static double calculateAverageSlope(Distance totalDistance, Float altitudeGain, Float altitudeLoss) {
        double avgSlope=0;
        if (totalDistance!=null&&altitudeGain!=null&&altitudeLoss!=null){
            avgSlope=(altitudeGain+altitudeLoss)/totalDistance.toM()*100;
        }
        return  avgSlope;
    }


    static Spannable createIdle(Context context) {
        return new SpannableStringBuilder()
                .append(context.getString(R.string.voiceIdle));
    }

    static Spannable createAfterRecording(Context context, TrackStatistics trackStatistics, UnitSystem unitSystem) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        Speed maxSpeed = trackStatistics.getMaxSpeed();
        Speed avgSpeed = trackStatistics.getAverageSpeed();
        Distance totalDistance = trackStatistics.getTotalDistance();
        Float altitudeGain=trackStatistics.getTotalAltitudeGain();
        Float altitudeLoss=trackStatistics.getTotalAltitudeLoss();
        Double temperature = WeatherFetchService.fetchTempData(trackStatistics.getLatitude(),trackStatistics.getLongitude());


        Duration skiingTime = trackStatistics.getTotalTime().minus(trackStatistics.getTotalChairliftWaitingTime());
        Duration waitingTime = trackStatistics.getTotalChairliftWaitingTime();
    

        int perUnitStringId;
        int distanceId;
        int speedId;
        String unitDistanceTTS;
        String unitSpeedTTS;
        switch (unitSystem) {
            case METRIC -> {
                perUnitStringId = R.string.voice_per_kilometer;
                distanceId = R.string.voiceDistanceKilometersPlural;
                speedId = R.string.voiceSpeedKilometersPerHourPlural;
                unitDistanceTTS = "kilometer";
                unitSpeedTTS = "kilometer per hour";
            }
            case IMPERIAL_FEET, IMPERIAL_METER -> {
                perUnitStringId = R.string.voice_per_mile;
                distanceId = R.string.voiceDistanceMilesPlural;
                speedId = R.string.voiceSpeedMilesPerHourPlural;
                unitDistanceTTS = "mile";
                unitSpeedTTS = "mile per hour";
            }
            case NAUTICAL_IMPERIAL -> {
                perUnitStringId = R.string.voice_per_nautical_mile;
                distanceId = R.string.voiceDistanceNauticalMilesPlural;
                speedId = R.string.voiceSpeedMKnotsPlural;
                unitDistanceTTS = "nautical mile";
                unitSpeedTTS = "knots";
            }
            default -> throw new RuntimeException("Not implemented");
        }

        if (shouldVoiceAnnounceAverageSpeedRecording()) {

            double speedInUnit = avgSpeed.to(unitSystem);
            builder.append(" ")
                    .append(context.getString(R.string.speed));
            String template = context.getResources().getString(speedId);
            appendDecimalUnit(builder, MessageFormat.format(template, Map.of("n", speedInUnit)), speedInUnit, 1, unitSpeedTTS);
            builder.append(".");
        }

        if (shouldVoiceAnnounceMaxSpeedRecording()) {
            double speedInUnit = maxSpeed.to(unitSystem);
            builder.append(" ")
                    .append(context.getString(R.string.stats_max_speed));
            String template = context.getResources().getString(speedId);
            appendDecimalUnit(builder, MessageFormat.format(template, Map.of("n", speedInUnit)), speedInUnit, 1, unitSpeedTTS);
            builder.append(".");
        }

        if (shouldVoiceAnnounceAveragesloperecording()) {
            double avgSlope=calculateAverageSlope(totalDistance,altitudeGain,altitudeLoss);
            if (!Double.isNaN(avgSlope)) {
                builder.append(" ")
                        .append("Average slope")
                        .append(": ")
                        .append(String.format("%.2f%%", avgSlope))
                        .append(".");
            }
        }

        if (shouldVoiceAnnounceMaxSlope()) {
            double maxSlope = calculateMaxSlope(); // Calculate the maximum slope based on elevation data
            Log.i("MaxSlope",maxSlope+"");
            if (!Double.isNaN(maxSlope)) {
                builder.append(" ")
                        .append(context.getString(R.string.settings_announcements_max_slope))
                        .append(": ")
                        .append(String.format("%.2f%%", maxSlope)) // Format the slope value
                        .append(".");
            }
        }

        if (shouldVoiceAnnounceTimeSkiedRecording()) {
            long skiingTimeLong=skiingTime.toSeconds();
            long skiingMinutes=skiingTimeLong/60;
            long skiingSeconds=skiingTimeLong%60;

            builder.append(" ")
                    .append(context.getString(R.string.settings_announcements_time_skied_recording))
                    .append(": ");
            if (skiingMinutes>0){
                builder.append(skiingMinutes+" minutes ");
            }
            if (skiingSeconds>0){
                builder.append(skiingSeconds+" seconds ");
            }
            builder.append(".");
        }



        if (shouldVoiceAnnounceTotalWaitingTime()){
            long waitingTimeLong=waitingTime.toSeconds();
            long waitingMinutes=waitingTimeLong/60;
            long waitingSeconds=waitingTimeLong%60;
            builder.append(" ")
                    .append(context.getString(R.string.settings_announcements_total_waiting_time))
                    .append(": ");
            if (waitingMinutes>0){
                builder.append(waitingMinutes+" minutes ");
            }
            if (waitingSeconds>0){
                builder.append(waitingSeconds+" seconds ");
            }
            builder.append(".");
        }

        if(shouldVoiceAnnounceTemperature()){
             
            if (!Double.isNaN(temperature)) {
                builder.append(" ")
                        .append(context.getString(R.string.settings_announcements_temperature))
                        .append(": ")
                        .append(String.format("%.2f", temperature)) // Format the slope value
                        .append(" degree.");
            }
        }


        return builder;
    }

    private static void resetRunData(TrackStatistics trackStatistics){
        double averageSlope= calculateAverageSlope(trackStatistics.getDistanceRun(),trackStatistics.getAltitudeRun(),0f);
        if (currentMaxSlope==null || Double.isNaN(currentMaxSlope) || currentMaxSlope<averageSlope){
            currentMaxSlope=averageSlope;
        }
        trackStatistics.setMaximumSpeedPerRun(0);
        trackStatistics.setDistanceRun(Distance.of(0));
        trackStatistics.setAltitudeRun(0f);
        trackStatistics.setTimeRun(Duration.ofSeconds(0));
    }

    static Spannable createRunStatistics(Context context, TrackStatistics trackStatistics, UnitSystem unitSystem) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        Speed averageMovingSpeed = trackStatistics.getAverageMovingSpeed();
        Float maxSpeed = trackStatistics.getMaximumSpeedPerRun();
        double averageSlope= calculateAverageSlope(trackStatistics.getDistanceRun(),trackStatistics.getAltitudeRun(),0f);

        Distance runDistance = trackStatistics.getDistanceRun();
        Duration runTime = trackStatistics.getTimeRun();
        
        if(runDistance!=null&& runTime!=null){
            averageMovingSpeed = Speed.of(runDistance,runTime);
        }



        resetRunData(trackStatistics);

        int speedId;
        String unitSpeedTTS;
        switch (unitSystem) {
            case METRIC -> {
                speedId = R.string.voiceSpeedKilometersPerHourPlural;
                unitSpeedTTS = "kilometer per hour";
            }
            case IMPERIAL_FEET, IMPERIAL_METER -> {
                speedId = R.string.voiceSpeedMilesPerHourPlural;
                unitSpeedTTS = "mile per hour";
            }
            case NAUTICAL_IMPERIAL -> {
                speedId = R.string.voiceSpeedMKnotsPlural;
                unitSpeedTTS = "knots";
            }
            default -> throw new RuntimeException("Not implemented");
        }


        //Announce Average Speed
        if (shouldVoiceAnnounceRunAverageSpeed()) {
            double speedInUnit = averageMovingSpeed.to(unitSystem);
            builder.append(" ")
                    .append(context.getString(R.string.speed));
            String template = context.getResources().getString(speedId);
            appendDecimalUnit(builder, MessageFormat.format(template, Map.of("n", speedInUnit)), speedInUnit, 1, unitSpeedTTS);
            builder.append(".");
        }

        if (shouldVoiceAnnounceMaxSpeedRun()&&maxSpeed!=null) {
            double speedInUnit = maxSpeed;
            builder.append(" ")
                    .append(context.getString(R.string.stats_max_speed));
            String template = context.getResources().getString(speedId);
            appendDecimalUnit(builder, MessageFormat.format(template, Map.of("n", speedInUnit)), speedInUnit, 1, unitSpeedTTS);
            builder.append(".");
        }
        if (shouldVoiceAnnounceAverageslopeRun()) {
            double avgSlope = averageSlope;
            if (!Double.isNaN(avgSlope)) {
                builder.append(" ")
                        .append("Average slope")
                        .append(": ")
                        .append(String.format("%.2f%%", avgSlope))
                        .append(".");
            }
        }
        return builder;
    }

    static Spannable createStatistics(Context context, TrackStatistics trackStatistics, UnitSystem unitSystem, boolean isReportSpeed, @Nullable IntervalStatistics.Interval currentInterval, @Nullable SensorStatistics sensorStatistics) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        Distance totalDistance = trackStatistics.getTotalDistance();
        Speed averageMovingSpeed = trackStatistics.getAverageMovingSpeed();

        Speed currentDistancePerTime = currentInterval != null ? currentInterval.getSpeed() : null;

        int perUnitStringId;
        int distanceId;
        int speedId;
        String unitDistanceTTS;
        String unitSpeedTTS;
        switch (unitSystem) {
            case METRIC -> {
                perUnitStringId = R.string.voice_per_kilometer;
                distanceId = R.string.voiceDistanceKilometersPlural;
                speedId = R.string.voiceSpeedKilometersPerHourPlural;
                unitDistanceTTS = "kilometer";
                unitSpeedTTS = "kilometer per hour";
            }
            case IMPERIAL_FEET, IMPERIAL_METER -> {
                perUnitStringId = R.string.voice_per_mile;
                distanceId = R.string.voiceDistanceMilesPlural;
                speedId = R.string.voiceSpeedMilesPerHourPlural;
                unitDistanceTTS = "mile";
                unitSpeedTTS = "mile per hour";
            }
            case NAUTICAL_IMPERIAL -> {
                perUnitStringId = R.string.voice_per_nautical_mile;
                distanceId = R.string.voiceDistanceNauticalMilesPlural;
                speedId = R.string.voiceSpeedMKnotsPlural;
                unitDistanceTTS = "nautical mile";
                unitSpeedTTS = "knots";
            }
            default -> throw new RuntimeException("Not implemented");
        }

        double distanceInUnit = totalDistance.toKM_Miles(unitSystem);

        if (shouldVoiceAnnounceTotalDistance()) {
            builder.append(context.getString(R.string.total_distance));
            // Units should always be english singular for TTS.
            // See https://developer.android.com/reference/android/text/style/TtsSpan?hl=en#TYPE_MEASURE
            String template = context.getResources().getString(distanceId);
            appendDecimalUnit(builder, MessageFormat.format(template, Map.of("n", distanceInUnit)), distanceInUnit, 1, unitDistanceTTS);
            // Punctuation helps introduce natural pauses in TTS
            builder.append(".");
        }
        if (totalDistance.isZero()) {
            return builder;
        }

        // Announce time
        Duration movingTime = trackStatistics.getMovingTime();
        if (shouldVoiceAnnounceMovingTime() && !movingTime.isZero()) {
            appendDuration(context, builder, movingTime);
            builder.append(".");
        }

        if (isReportSpeed) {
            if (shouldVoiceAnnounceAverageSpeedPace()) {
                double speedInUnit = averageMovingSpeed.to(unitSystem);
                builder.append(" ")
                        .append(context.getString(R.string.speed));
                String template = context.getResources().getString(speedId);
                appendDecimalUnit(builder, MessageFormat.format(template, Map.of("n", speedInUnit)), speedInUnit, 1, unitSpeedTTS);
                builder.append(".");
            }

            if (shouldVoiceAnnounceLapSpeedPace() && currentDistancePerTime != null) {
                double currentDistancePerTimeInUnit = currentDistancePerTime.to(unitSystem);
                if (currentDistancePerTimeInUnit > 0) {
                    builder.append(" ")
                            .append(context.getString(R.string.lap_speed));
                    String template = context.getResources().getString(speedId);
                    appendDecimalUnit(builder, MessageFormat.format(template, Map.of("n", currentDistancePerTimeInUnit)), currentDistancePerTimeInUnit, 1, unitSpeedTTS);
                    builder.append(".");
                }
            }
        } else {
            if (shouldVoiceAnnounceAverageSpeedPace()) {
                Duration time = averageMovingSpeed.toPace(unitSystem);
                builder.append(" ")
                        .append(context.getString(R.string.pace));
                appendDuration(context, builder, time);
                builder.append(" ")
                        .append(context.getString(perUnitStringId))
                        .append(".");
            }


            if (shouldVoiceAnnounceLapSpeedPace() && currentDistancePerTime != null) {
                Duration currentTime = currentDistancePerTime.toPace(unitSystem);
                builder.append(" ")
                        .append(context.getString(R.string.lap_time));
                appendDuration(context, builder, currentTime);
                builder.append(" ")
                        .append(context.getString(perUnitStringId))
                        .append(".");
            }
        }

        if (shouldVoiceAnnounceAverageHeartRate() && sensorStatistics != null && sensorStatistics.hasHeartRate()) {
            int averageHeartRate = Math.round(sensorStatistics.avgHeartRate().getBPM());

            builder.append(" ")
                    .append(context.getString(R.string.average_heart_rate));
            appendCardinal(builder, context.getString(R.string.sensor_state_heart_rate_value, averageHeartRate), averageHeartRate);
            builder.append(".");
        }
        if (shouldVoiceAnnounceLapHeartRate() && currentInterval != null && currentInterval.hasAverageHeartRate()) {
            int currentHeartRate = Math.round(currentInterval.getAverageHeartRate().getBPM());

            builder.append(" ")
                    .append(context.getString(R.string.current_heart_rate));
            appendCardinal(builder, context.getString(R.string.sensor_state_heart_rate_value, currentHeartRate), currentHeartRate);
            builder.append(".");
        }


        return builder;
    }

    private static void appendDuration(@NonNull Context context, @NonNull SpannableStringBuilder builder, @NonNull Duration duration) {
        int hours = (int) (duration.toHours());
        int minutes = (int) (duration.toMinutes() % 60);
        int seconds = (int) (duration.getSeconds() % 60);

        if (hours > 0) {
            String template = context.getResources().getString(R.string.voiceHoursPlural);
            appendDecimalUnit(builder, MessageFormat.format(template, Map.of("n", hours)), hours, 0, "hour");
        }
        if (minutes > 0) {
            String template = context.getResources().getString(R.string.voiceMinutesPlural);
            appendDecimalUnit(builder, MessageFormat.format(template, Map.of("n", minutes)), minutes, 0, "minute");
        }
        if (seconds > 0 || duration.isZero()) {
            String template = context.getResources().getString(R.string.voiceSecondsPlural);
            appendDecimalUnit(builder, MessageFormat.format(template, Map.of("n", seconds)), seconds, 0, "second");
        }
    }

    /**
     * Speaks as: 98.14 [UNIT] - ninety eight point one four [UNIT with correct plural form]
     *
     * @param number    The number to speak
     * @param precision The number of decimal places to announce
     */
    private static void appendDecimalUnit(@NonNull SpannableStringBuilder builder, @NonNull String localizedText, double number, int precision, @NonNull String unit) {
        TtsSpan.MeasureBuilder measureBuilder = new TtsSpan.MeasureBuilder()
                .setUnit(unit);

        // Round before extracting integral and decimal parts
        double roundedNumber = Math.round(Math.pow(10, precision) * number) / Math.pow(10.0, precision);
        long integerPart = (long) roundedNumber;

        if (precision == 0 || (roundedNumber - integerPart) == 0) {
            measureBuilder.setNumber((long) number);
        } else {
            // Extract the decimal part
            String fractionalPart = String.format("%." + precision + "f", (roundedNumber - integerPart)).substring(2);
            measureBuilder.setIntegerPart(integerPart)
                    .setFractionalPart(fractionalPart);
        }

        builder.append(" ")
                .append(localizedText, measureBuilder.build(), SPAN_INCLUSIVE_EXCLUSIVE);
    }

    /**
     * Speaks as: 98 - ninety eight
     */
    private static void appendCardinal(@NonNull SpannableStringBuilder builder, @NonNull String localizedText, long number) {
        builder.append(" ")
                .append(localizedText, new TtsSpan.CardinalBuilder().setNumber(number).build(), SPAN_INCLUSIVE_EXCLUSIVE);
    }
}

