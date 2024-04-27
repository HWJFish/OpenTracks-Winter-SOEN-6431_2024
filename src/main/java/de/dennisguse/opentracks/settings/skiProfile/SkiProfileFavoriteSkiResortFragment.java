package de.dennisguse.opentracks.settings.skiProfile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.settings.PreferencesUtils;
import de.dennisguse.opentracks.settings.SettingsActivity;

public class SkiProfileFavoriteSkiResortFragment extends PreferenceFragmentCompat {
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, key) -> {
        if (PreferencesUtils.isKey(R.string.night_mode_key, key)) {
            getActivity().runOnUiThread(PreferencesUtils::applyNightMode);
        }
    };

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.fragment_ski_profile_favorite_ski_resort);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_ski_profile_title);
    }

    @Override
    public void onResume() {
        super.onResume();

        EditTextPreference nameInput = findPreference(getString(R.string.settings_ski_profile_name_key));
        nameInput.setDialogTitle(getString(R.string.settings_ski_profile_name_dialog_title));
        nameInput.setOnBindEditTextListener(editText -> {
            editText.setSingleLine(true);
            editText.selectAll(); // select all text
            int maxNameLength = 20;
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxNameLength)});
        });


        EditTextPreference phoneInput = findPreference(getString(R.string.settings_ski_profile_phone_key));
        phoneInput.setDialogTitle(getString(R.string.settings_ski_profile_phone_dialog_title));
        phoneInput.setOnBindEditTextListener(editText -> {
            editText.setSingleLine(true);
            editText.selectAll(); // select all text
            InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
                if (TextUtils.isDigitsOnly(source)) {
                    if (dest.length() + source.length() <= 10) {
                        return null; // Accept the input
                    }
                }
                return ""; // Reject the input
            };
            editText.setFilters(new InputFilter[]{inputFilter});
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() < 10) {
                        editText.setError(getString(R.string.settings_ski_profile_phone_error)); // Set error message
                    } else {
                        editText.setError(null);
                    }
                }
            });
        });
        EditTextPreference countryInput = findPreference("Country");
        countryInput.setDialogTitle("Country");

        EditTextPreference provinceInput = findPreference("Province");
        provinceInput.setDialogTitle("Province");

        ListPreference ski_type = findPreference("ski_type");
        ski_type.setDialogTitle("Type of Skiing");

        PreferencesUtils.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferencesUtils.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }
}
