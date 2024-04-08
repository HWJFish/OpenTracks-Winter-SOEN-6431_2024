package de.dennisguse.opentracks;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class CalendarActivity extends AppCompatActivity {

    private Date selectedDate;
    private Calendar calendar = Calendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setSelectedDate(new Date());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        MaterialToolbar toolbar = findViewById(R.id.calendar_toolbar);
        toolbar.setNavigationOnClickListener((view) -> finish());
        Bundle extras = getIntent().getExtras();

        Button dateSelectButton = findViewById(R.id.calendar_date_selected_activity);
        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                    setSelectedDate(new Date(year-1900, month, dayOfMonth));
                }
            });
            dateSelectButton.setOnClickListener((View v)->{
                Intent intent = new Intent(v.getContext(), ElevationAndSpeedActivity.class);;
                if(extras!=null && Objects.equals(extras.getString("Display Fields"), "Elevation and Speed")){
                    intent = new Intent(v.getContext(), ElevationAndSpeedActivity.class);
                } else{
                    intent = new Intent(v.getContext(), RunsAndLiftsActivity.class);
                }
                calendar.setTime(selectedDate);
                int year = calendar.get(Calendar.YEAR) - 1900;
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                intent.putExtra("Year", year);
                intent.putExtra("Year", month);
                intent.putExtra("Day", day);

                v.getContext().startActivity(intent);
//                Bundle extras = getIntent().getExtras();
//                if (extras != null) {
//                    String value = extras.getString("key");
//                    //The key argument here must match that used in the other activity
//                }
            });
    }


    public void setSelectedDate(Date date){
        this.selectedDate = date;
    }

    public Date getSelectedDate(){
        return selectedDate;
    }
}
