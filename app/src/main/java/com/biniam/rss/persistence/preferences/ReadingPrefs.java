package com.biniam.rss.persistence.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.biniam.rss.R;

/**
 * Created by biniam_Haddish on 6/14/17.
 *
 * A helper class that sets and retrieves reading appearance preferences such as
 * background color, font-size, text color etc...
 *
 */

public class ReadingPrefs {


    public static final String READING_APPEARANCE_PREFS = "READING_APPEARANCE_PREFS";
    public static final String SELECTED_FONT_INDEX_PREF_KEY = "SELECTED_FONT_INDEX_PREF_KEY";
    public static final String SELECTED_BG_INDEX_PREF_KEY = "SELECTED_BG_INDEX_PREF_KEY";
    public static final String SELECTED_FONT_NAME_PREF_KEY = "SELECTED_FONT_NAME_PREF_KEY";
    public static final String BACKGROUND_COLOR_PREF_KEY = "BACKGROUND_COLOR_PREF_KEY";
    public static final String TEXT_COLOR_PREF_KEY = "TEXT_COLOR_PREF_KEY";
    public static final String LINK_COLOR_PREF_KEY = "LINK_COLOR_PREF_KEY";
    public static final String TITLE_FONT_SIZE_PREF_KEY = "TITLE_FONT_SIZE_PREF_KEY";
    public static final String CONTENT_FONT_SIZE_PREF_KEY = "CONTENT_FONT_SIZE_PREF_KEY";
    public static final String ARTICLE_INFO_FONT_SIZE_PREF_KEY = "ARTICLE_INFO_FONT_SIZE_PREF_KEY";
    public static final String LINE_HEIGHT_PREF_KEY = "LINE_HEIGHT_PREF_KEY";
    public static final String JUSTIFICATION_PREF_KEY = "JUSTIFICATION_PREF_KEY";
    private static ReadingPrefs readingPrefs;
    public int selectedFontIndex;
    public int selectedBgIndex;
    public String backgroundColor;
    public String textColor;
    public String linkColor;
    public String selectedFontName;
    public int titleFontSize;
    public int contentFontSize;
    public int articleInfoFontSize;
    public float lineHeight;
    public String justification;
    private SharedPreferences displayPreferences;
    private Context context;
    private PrefChangesListener prefChangesListener = new PrefChangesListener();

    private ReadingPrefs(Context context) {
        this.context = context;
        displayPreferences = context.getSharedPreferences(READING_APPEARANCE_PREFS, 0);
        reloadPrefs();
        displayPreferences.registerOnSharedPreferenceChangeListener(prefChangesListener);
    }

    public static ReadingPrefs getInstance(Context context) {
        if (readingPrefs == null) {
            readingPrefs = new ReadingPrefs(context);
        }

        return readingPrefs;
    }

    private void reloadPrefs() {
        selectedFontIndex = displayPreferences.getInt(SELECTED_FONT_INDEX_PREF_KEY, 0);
        selectedBgIndex = displayPreferences.getInt(SELECTED_BG_INDEX_PREF_KEY, 0);
        selectedFontName = displayPreferences.getString(SELECTED_FONT_NAME_PREF_KEY, context.getString(R.string.opensans));
        backgroundColor = displayPreferences.getString(BACKGROUND_COLOR_PREF_KEY, context.getString(R.string.white));
        textColor = displayPreferences.getString(TEXT_COLOR_PREF_KEY, context.getString(R.string.white_bg_text_color));
        linkColor = displayPreferences.getString(LINK_COLOR_PREF_KEY, context.getString(R.string.white_bg_link_color));
        titleFontSize = displayPreferences.getInt(TITLE_FONT_SIZE_PREF_KEY, 22);
        articleInfoFontSize = displayPreferences.getInt(ARTICLE_INFO_FONT_SIZE_PREF_KEY, 12);

        contentFontSize = displayPreferences.getInt(CONTENT_FONT_SIZE_PREF_KEY, 16);
        lineHeight = displayPreferences.getFloat(LINE_HEIGHT_PREF_KEY, 1.2f);
        justification = displayPreferences.getString(JUSTIFICATION_PREF_KEY, "left");
    }

    public void setStringPref(String key, String value) {

        if (key.equals(BACKGROUND_COLOR_PREF_KEY)) {
            Log.d(ReadingPrefs.class.getSimpleName(), String.format("Saving %s background color", value));
        }

        SharedPreferences.Editor editor = displayPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void setIntPref(String key, int value) {
        SharedPreferences.Editor editor = displayPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void setFloatPref(String key, float value) {
        SharedPreferences.Editor editor = displayPreferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    @Override
    protected void finalize() throws Throwable {
        displayPreferences.unregisterOnSharedPreferenceChangeListener(prefChangesListener);
    }

    private class PrefChangesListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            Log.d(ReadingPrefs.class.getSimpleName(), "onSharedPreferenceChanged");
            reloadPrefs();
        }
    }

}
