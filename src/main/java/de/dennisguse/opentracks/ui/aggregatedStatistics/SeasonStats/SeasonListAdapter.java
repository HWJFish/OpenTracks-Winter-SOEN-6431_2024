package de.dennisguse.opentracks.ui.aggregatedStatistics.SeasonStats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import de.dennisguse.opentracks.R;

public class SeasonListAdapter extends ArrayAdapter<DummySeason> {

    public SeasonListAdapter(Context context, ArrayList<DummySeason> seasonArrayList){
        super(context, R.layout.season_list_item, seasonArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        DummySeason season = getItem(position);
        if (convertView==null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.season_list_item, parent, false);
        }

        TextView seasonName = convertView.findViewById(R.id.seasonName);
        seasonName.setText(season.seasonName);

        return convertView;
    }
}
