package com.biniisu.leanrss.ui.base;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.persistence.preferences.ReadablyPrefs;

public class ReadingPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = ReadingPreferencesFragment.class.getSimpleName();
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 11;

    private SharedPreferences sharedPreferences;
    private ReadablyPrefs readablyPrefs;
    private CheckBoxPreference autoDarkModeCheckBoxPreference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.reading_preferences);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        readablyPrefs = ReadablyPrefs.getInstance(getActivity().getApplicationContext());

        autoDarkModeCheckBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.pref_auto_dark_mode_title));

        autoDarkModeCheckBoxPreference.setOnPreferenceClickListener(preference -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                        && !readablyPrefs.autoDarkMode) {
                    // Request for location permission
                    requestPermissions(
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_LOCATION
                    );
                }
            }

            return true;
        });
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: location permission granted");

                autoDarkModeCheckBoxPreference.setChecked(true);
                readablyPrefs.updateBooleanPref(getString(R.string.pref_auto_dark_mode_title), true);

            } else {
                Log.d(TAG, "onRequestPermissionsResult: location permission denied");
                autoDarkModeCheckBoxPreference.setChecked(false);
                readablyPrefs.updateBooleanPref(getString(R.string.pref_auto_dark_mode_title), false);
            }
        }
    }
}