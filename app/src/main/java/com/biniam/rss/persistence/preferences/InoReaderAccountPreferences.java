package com.biniam.rss.persistence.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.biniam.rss.connectivity.inoreader.InoReaderConstants;

/**
 * Created by biniam on 10/19/17.
 */

public class InoReaderAccountPreferences implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String inoReaderPrefernceName = "INOREADER_PREFERNCE_DATA";
    //inoReader pref keys
    public String accessToken;
    public String refreshToken;
    public String tokenType;
    public boolean isAuthenticated;
    public long expireTime = 0;
    private Context context;
    private SharedPreferences InoreaderSharedPreferences;
    private PreferenceChangeListener preferenceChangeListener;
    private static InoReaderAccountPreferences inoReaderAccountPreferences ;


    public InoReaderAccountPreferences(Context context) {
        this.context = context;
        this.InoreaderSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.InoreaderSharedPreferences = context.getSharedPreferences(inoReaderPrefernceName, 0);
        this.InoreaderSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        updateInoPrefs();
    }


    public static InoReaderAccountPreferences getInstance(Context context) {
        if (inoReaderAccountPreferences == null) {
            inoReaderAccountPreferences = new InoReaderAccountPreferences(context);
        }

        return inoReaderAccountPreferences;
    }

    public void setPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
        this.preferenceChangeListener = preferenceChangeListener;
    }

    private void updateInoPrefs() {
        this.accessToken = this.InoreaderSharedPreferences.getString(InoReaderConstants.ACCESS_TOKEN, "");
        this.refreshToken = this.InoreaderSharedPreferences.getString(InoReaderConstants.REFRESH_TOKEN, "");
        this.tokenType = this.InoreaderSharedPreferences.getString(InoReaderConstants.TOKEN_TYPE, "");
        this.expireTime = this.InoreaderSharedPreferences.getLong(InoReaderConstants.EXPIRES_IN, 0);
        this.isAuthenticated = this.InoreaderSharedPreferences.getBoolean(InoReaderConstants.IS_AUTHENTICATED, false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // this will truck all the prefe changes that are made inside the InoReaderAccountPreferences.
        if (this.preferenceChangeListener != null) {
            this.preferenceChangeListener.onPrefernceChanged(sharedPreferences, key);
        }
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor customSharedPreferencesEditor = this.InoreaderSharedPreferences.edit();
        customSharedPreferencesEditor.putString(key, value);
        customSharedPreferencesEditor.apply();
    }

    public void setInt(String key, long value) {
        SharedPreferences.Editor customSharedPreferencesEditor = this.InoreaderSharedPreferences.edit();
        customSharedPreferencesEditor.putLong(key, value);
        customSharedPreferencesEditor.apply();
    }

    public void setBoolean(String key, boolean value) {
        SharedPreferences.Editor customSharedPreferencesEditor = this.InoreaderSharedPreferences.edit();
        customSharedPreferencesEditor.putBoolean(key, value);
        customSharedPreferencesEditor.apply();
    }

    public void unregisterPreferenceChangeListeners() {
        InoreaderSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public interface PreferenceChangeListener {
        void onPrefernceChanged(SharedPreferences sharedPreferences, String str);
    }
}
