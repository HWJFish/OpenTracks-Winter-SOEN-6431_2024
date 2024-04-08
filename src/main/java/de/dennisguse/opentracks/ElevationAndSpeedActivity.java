package de.dennisguse.opentracks;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class ElevationAndSpeedActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevation_and_speed);
        MaterialToolbar toolbar = findViewById(R.id.elevation_and_speed);
        toolbar.setNavigationOnClickListener((view) -> finish());
    }
}
