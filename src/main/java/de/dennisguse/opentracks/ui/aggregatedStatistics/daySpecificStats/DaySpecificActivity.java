package de.dennisguse.opentracks.ui.aggregatedStatistics.daySpecificStats;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import de.dennisguse.opentracks.AbstractActivity;
import de.dennisguse.opentracks.data.ContentProviderUtils;
import de.dennisguse.opentracks.data.TrackPointIterator;
import de.dennisguse.opentracks.data.models.Track;
import de.dennisguse.opentracks.data.models.TrackPoint;
import de.dennisguse.opentracks.data.models.TrackSegment;
import de.dennisguse.opentracks.databinding.DaySpecificActivityBinding;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Date;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;

public class DaySpecificActivity extends AbstractActivity {

    private DaySpecificActivityBinding viewBinding;
    private static final String TAG = DaySpecificActivity.class.getSimpleName();
    public static final String EXTRA_TRACK_DATE = "track_date";
    private String activityDate;
    private ContentProviderUtils contentProviderUtils;
    private Track.Id trackId;
    private List<TrackSegment> trackSegments;
    private DaySpecificAdapter dataAdapter;

    private final String fallBackDate = "2024-03-09";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackSegments = new ArrayList<>();
        contentProviderUtils = new ContentProviderUtils(this);
        handleIntent(getIntent());
        updateTrackSegments();
        setSupportActionBar(viewBinding.bottomAppBarLayout.bottomAppBar);

        dataAdapter = new DaySpecificAdapter(this, viewBinding.segmentList);
        dataAdapter.swapData(trackSegments);
        viewBinding.segmentList.setAdapter(dataAdapter);
        viewBinding.segmentListToolbar.setTitle(activityDate);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTrackSegments();
        dataAdapter.swapData(trackSegments);
    }

    public void updateTrackSegments() {
        if (trackId == null) {
            return;
        }
        try (TrackPointIterator trackPointIterator = contentProviderUtils.getTrackPointLocationIterator(trackId, null)) {
            TrackSegment currentSegment = null;
            while (trackPointIterator.hasNext()) {
                TrackPoint nextPoint = trackPointIterator.next();

                switch (nextPoint.getType()) {
                    case SEGMENT_START_AUTOMATIC:
                    case SEGMENT_START_MANUAL:
                        if (currentSegment != null) {
                            trackSegments.add(currentSegment);
                        }
                        currentSegment = new TrackSegment(nextPoint.getTime());
                        break;

                    case SEGMENT_END_MANUAL:
                        trackSegments.add(currentSegment);
                        currentSegment = null;

                    case TRACKPOINT:
                        if (currentSegment != null) {
                            currentSegment.addTrackPoint(nextPoint);
                        }
                        break;

                    default:
                        Log.d(TAG, "No Action for TrackPoint IDLE/SENSORPOINT while recording segments");
                }
            }
        }
    }

    /**
     * This method is responsible for displaying a toast message indicating that no tracks were found
     * for the specific date of the activity. It finishes the current activity and displays the toast
     * message with information about the date and a suggestion to import a GPX file from Moodle.
     */
    private void showNoTracksFoundToast() {
        finish();
        Toast.makeText(DaySpecificActivity.this, "No Tracks found for date: " + activityDate + "\n Please import GPX file from Moodle", Toast.LENGTH_LONG).show();
    }
    /**
     * Converts a string representation of a date to a Date object.
     *
     * @param dateString A string representing the date in the format "yyyy-MM-dd".
     * @return A Date object representing the parsed date.
     */
    private Date getDateFromString(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(dateString, formatter);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Handles the intent received by the activity, extracting the date of the track from it.
     * If the date is not provided in the intent extras, it falls back to a default date.
     * Then, it retrieves the track for the given date from the content provider.
     * If no track is found, it displays a toast message indicating no tracks were found.
     * Otherwise, it sets the trackId for further use.
     *
     * @param intent The Intent containing the date information.
     */
    private void handleIntent(Intent intent) {
        activityDate = intent.getStringExtra(EXTRA_TRACK_DATE);
        if (activityDate == null) {
            Log.e(TAG, DaySpecificActivity.class.getSimpleName() + " needs EXTRA_TRACK_ID.");

            // None provided, we will assume a specific date on our own
            activityDate = this.fallBackDate;
        }

        Date dayOfActivity = getDateFromString(activityDate);
        Track track = contentProviderUtils.getTrack(dayOfActivity);
        if (track == null) {
            showNoTracksFoundToast();
        } else {
            trackId = track.getId();
        }
    }

    /**
     * Inflates the layout for the activity using view binding and returns the root view.
     *
     * @return The root view of the inflated layout.
     */
    @Override
    protected View getRootView() {
        viewBinding = DaySpecificActivityBinding.inflate(getLayoutInflater());
        return viewBinding.getRoot();
    }
}