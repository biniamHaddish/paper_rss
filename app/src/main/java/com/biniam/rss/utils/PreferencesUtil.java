package com.biniam.rss.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by biniam-Haddish on 7/22/17.
 */

public class PreferencesUtil implements SharedPreferences.OnSharedPreferenceChangeListener {


    Constants constants;
    private Context context;
    private SharedPreferences customSharedPreferences;
    private PreferenceChangeListener preferenceChangeListener;


    public PreferencesUtil(Context context) {
        this.context = context;
        this.customSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.customSharedPreferences = context.getSharedPreferences(Constants.CUSTOM_PREFRENCES, Context.MODE_PRIVATE);
        this.customSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void setPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
        this.preferenceChangeListener = preferenceChangeListener;
    }

    public void updateChanges() {
        //UPDATE THE CHANGES HERE.
    }

    public void setIntegerPrefernces(String key, int value) {
        SharedPreferences.Editor customSharedPreferencesEditor = this.customSharedPreferences.edit();
        customSharedPreferencesEditor.putInt(key, value);
        customSharedPreferencesEditor.apply();
    }

    public void setBooleanPreferences(String key, boolean value) {
        SharedPreferences.Editor customSharedPreferencesEditor = this.customSharedPreferences.edit();
        customSharedPreferencesEditor.putBoolean(key, value);
        customSharedPreferencesEditor.apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateChanges();
        if (this.preferenceChangeListener != null) {
            this.preferenceChangeListener.onPreferenceChanged(sharedPreferences, key);
        }
    }

    /*interface to monitor the changes of the custom preferences */
    public interface PreferenceChangeListener {
        void onPreferenceChanged(SharedPreferences sharedPreferences, String str);
    }
}
