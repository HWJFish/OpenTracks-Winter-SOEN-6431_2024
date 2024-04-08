package de.dennisguse.opentracks.ui.aggregatedStatistics.SeasonStats;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.dennisguse.opentracks.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RunsStatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RunsStatFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String ARG_SEASON_NAME = "seasonName";
    private static final String ARG_SEASON_days = "days";
    private static final String ARG_SEASON_runs = "runs";
    private static final String ARG_SEASON_resort = "resort";
    private static final String ARG_SEASON_vertical_m = "vertical_m";
    private static final String ARG_SEASON_distance = "distance";
    private static final String ARG_SEASON_active = "active";
    private static final String ARG_SEASON_max_Speed = "max_Speed";
    private static final String ARG_SEASON_max_alt = "max_alt";
    private static final String ARG_SEASON_tallestRun = "tallestRun";
    private static final String ARG_SEASON_longestRun = "longestRun";

    // String variable to hold the season name
    private String mSeasonName;
    private String mActive;
    private String mDays;
    private String mRuns;
    private String mResort;
    private String mVertical_m;
    private String mMax_alt;
    private String mTallestRun;
    private String mDistance;
    private String mMax_speed;
    private String  mLongestRun;

    public RunsStatFragment() {
        // Required empty public constructor
    }

    public static RunsStatFragment newInstance(String seasonName, String days, String runs, String resort, String vertical_m, String distance, String active, String max_speed, String max_alt, String tallestRun, String longestRun) {
        RunsStatFragment fragment = new RunsStatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEASON_NAME, seasonName);
        args.putString(ARG_SEASON_days, days);
        args.putString(ARG_SEASON_runs, runs);
        args.putString(ARG_SEASON_resort, resort);
        args.putString(ARG_SEASON_vertical_m, vertical_m);
        args.putString(ARG_SEASON_distance, distance);
        args.putString(ARG_SEASON_max_Speed, max_speed);
        args.putString(ARG_SEASON_longestRun, longestRun);
        args.putString(ARG_SEASON_active, active);
        args.putString(ARG_SEASON_max_alt, max_alt);
        args.putString(ARG_SEASON_tallestRun, tallestRun);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_runs_stat, container, false);

        // Retrieve the season name argument
        Bundle args = getArguments();
        if (args != null) {
            mSeasonName = args.getString(ARG_SEASON_NAME);
            mActive = args.getString(ARG_SEASON_active);
            mDays = args.getString(ARG_SEASON_days);
            mRuns = args.getString(ARG_SEASON_runs);
            mResort = args.getString(ARG_SEASON_resort);
            mVertical_m = args.getString(ARG_SEASON_vertical_m);
            mMax_alt = args.getString(ARG_SEASON_max_alt);
            mTallestRun = args.getString(ARG_SEASON_tallestRun);
            mDistance = args.getString(ARG_SEASON_distance);
            mMax_speed = args.getString(ARG_SEASON_max_Speed);
            mLongestRun = args.getString(ARG_SEASON_runs);
        }

        // Update the TextView with the retrieved season name
        TextView seasonNameTextView = view.findViewById(R.id.seasonNameTextView);
        TextView daysTextView = view.findViewById(R.id.days_tv);
        TextView runsTextView = view.findViewById(R.id.runs_tv);
        TextView resortTextView = view.findViewById(R.id.resort_tv);
        TextView verticalMTextView = view.findViewById(R.id.vertical_m_tv);
        TextView distanceTextView = view.findViewById(R.id.distance_km_tv);
        TextView activeTextView = view.findViewById(R.id.active_tv);
        TextView maxSpeedTextView = view.findViewById(R.id.max_speed_tv);
        TextView maxAltTextView = view.findViewById(R.id.max_alt_tv);
        TextView tallestRunTextView = view.findViewById(R.id.tallestRun_tv);
        TextView longestRunTextView = view.findViewById(R.id.longestRun_tv);
        if (seasonNameTextView != null && mSeasonName != null) {
            seasonNameTextView.setText(mSeasonName);
            daysTextView.setText(mDays);
            runsTextView.setText(mRuns);
            resortTextView.setText(mResort);
            verticalMTextView.setText(mVertical_m);
            distanceTextView.setText(mDistance);
            activeTextView.setText(mActive);
            maxSpeedTextView.setText(mMax_speed);
            maxAltTextView.setText(mMax_alt);
            tallestRunTextView.setText(mTallestRun);
            longestRunTextView.setText(mLongestRun);
        }

        return view;
    }

}