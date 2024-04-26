package de.dennisguse.opentracks.ui.aggregatedStatistics.daystatistics;

import android.os.Bundle;
import android.view.View;

import de.dennisguse.opentracks.AbstractActivity;
import de.dennisguse.opentracks.databinding.StatisticsPerDayBinding;

public class DayStatisticsActivity extends AbstractActivity {

    private StatisticsPerDayBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View getRootView() {
        viewBinding = StatisticsPerDayBinding.inflate(getLayoutInflater());
        return viewBinding.getRoot();
    }

    // the following code snippet is the corresponding code in the activity for back button
    @Override
    protected void onStart() {
        super.onStart();
        viewBinding.statsBackButton.setOnClickListener(v -> finish());
    }


}
