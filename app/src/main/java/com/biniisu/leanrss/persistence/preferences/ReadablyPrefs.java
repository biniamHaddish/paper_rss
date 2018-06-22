package com.biniisu.leanrss.persistence.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.biniisu.leanrss.R;

/**
 * Created by biniam_Haddish on 12/29/17.
 * <p>
 * A helper class that sets and retrieves main applications preferences such as
 */

public class ReadablyPrefs implements SharedPreferences.OnSharedPreferenceChangeListener {

    // Theme constants
    public static final int AUTO_THEME = 0;
    public static final int LIGHT_THEME = 1;
    public static final int DARK_THEME = 2;
    private static ReadablyPrefs readablyPrefs;


    // Reading and appearance preferences
    public boolean fullScreenReading;
    public boolean autoDarkMode;
    public String dayReadingBgColor;
    public String nightReadingBgColor;
    public boolean switchUsingVolButton;
    public boolean doubleTapForFullArticle;


    // Syncing preferences
    public boolean automaticSync;
    public boolean automaticSyncWiFiOnly;
    public int syncInterval;
    public int unreadItemsToKeep;
    public int readItemsToKeep;
    public int favsToKeep;

    // Image caching preferences
    public boolean autoCacheImages;
    public boolean autoCacheImagesWiFiOnly;
    public SharedPreferences sharedPreferences;
    private Context context;

    private ReadablyPrefs(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        updatePrefs();
    }

    public static ReadablyPrefs getInstance(Context context) {
        if (readablyPrefs == null) {
            readablyPrefs = new ReadablyPrefs(context);
        }
        return readablyPrefs;
    }

    private void updatePrefs() {

        // Load reading and appearance pref values
        fullScreenReading = sharedPreferences.getBoolean(getString(R.string.pref_full_screen_reading_title), true);
        autoDarkMode = sharedPreferences.getBoolean(getString(R.string.pref_auto_dark_mode_title), false);
        dayReadingBgColor = sharedPreferences.getString(getString(R.string.pref_day_reading_theme_title), getString(R.string.white_bg_name));
        nightReadingBgColor = sharedPreferences.getString(getString(R.string.pref_night_reading_theme_title), getString(R.string.black_bg_name));
        switchUsingVolButton = sharedPreferences.getBoolean(getString(R.string.pref_enable_vol_button_feed_switching_title), false);
        doubleTapForFullArticle = sharedPreferences.getBoolean(getString(R.string.pref_enable_double_tap_for_full_article_title), true);

        // Load syncing pref values
        automaticSync = sharedPreferences.getBoolean(getString(R.string.pref_auto_sync_title), true);
        automaticSyncWiFiOnly = sharedPreferences.getBoolean(getString(R.string.pref_auto_sync_wifi_only_title), true);
        syncInterval = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_auto_sync_interval_title), "3600"));
        unreadItemsToKeep = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_unread_items_to_keep_title), "50"));
        readItemsToKeep = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_keep_read_items_for_title), "50"));
        favsToKeep = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_fav_items_to_keep_title), "100"));

        // Load image caching values
        autoCacheImages = sharedPreferences.getBoolean(getString(R.string.pref_auto_cache_images_title), true);
        automaticSyncWiFiOnly = sharedPreferences.getBoolean(getString(R.string.pref_auto_cache_images_on_wifi_only_title), true);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        updatePrefs();
    }

    private String getString(int stringRes) {
        return context.getString(stringRes);
    }

    public void updateIntPref(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void updateStringPref(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void updateBooleanPref(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    @Override
    protected void finalize() throws Throwable {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}