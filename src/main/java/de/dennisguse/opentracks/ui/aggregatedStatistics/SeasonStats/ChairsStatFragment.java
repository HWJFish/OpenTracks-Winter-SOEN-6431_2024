package de.dennisguse.opentracks.ui.aggregatedStatistics.SeasonStats;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.dennisguse.opentracks.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChairsStatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChairsStatFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_SEASON_NAME = "seasonName";
    private static final String ARG_SEASON_days = "days";
    private static final String ARG_SEASON_tallestChair = "tallestChair";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static String mSeasonName;
    private static String mDays;
    private static String mTallestChair;

    public ChairsStatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChairsStatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChairsStatFragment newInstance(String param1, String param2) {
        ChairsStatFragment fragment = new ChairsStatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_SEASON_NAME, mSeasonName);
        args.putString(ARG_SEASON_days, mDays);
        args.putString(ARG_SEASON_tallestChair, mTallestChair);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chairs_stat, container, false);
        Bundle args = getArguments();
        if (args != null) {
            mSeasonName = args.getString(ARG_SEASON_NAME);
            mDays = args.getString(ARG_SEASON_days);
            mTallestChair = args.getString(ARG_SEASON_tallestChair);
        }

        TextView seasonName = view.findViewById(R.id.seasonNameTextView);
        TextView days = view.findViewById(R.id.days_tv);
        TextView tallestChair = view.findViewById(R.id.tallestChair_tv);

        if (mSeasonName != null) {
            seasonName.setText(mSeasonName);
            days.setText(mDays);
            tallestChair.setText(mTallestChair);

        }
        return view;
    }

    public void setArguments(Bundle args) {
        this.setArguments(args);
    }
}