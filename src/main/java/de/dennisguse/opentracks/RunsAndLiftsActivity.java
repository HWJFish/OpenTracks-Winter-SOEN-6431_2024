package de.dennisguse.opentracks;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class RunsAndLiftsActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runs_and_lifts);
        MaterialToolbar toolbar = findViewById(R.id.runs_and_lifts);
        toolbar.setNavigationOnClickListener((view) -> finish());

    }
}
