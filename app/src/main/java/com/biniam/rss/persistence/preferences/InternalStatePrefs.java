package com.biniam.rss.persistence.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by biniam_Haddish on 12/23/17.
 * <p>
 * Preferences that save internal ui and other states
 */

public class InternalStatePrefs implements SharedPreferences.OnSharedPreferenceChangeListener {


    public static final String INTERNAL_PREFS = "INTERNAL_PREFS";

    public static final int NO_ACCOUNT_CHOSEN = -1;
    public static final int LOCAL_ACCOUNT = 0;
    public static final int FEED_BIN_ACCOUNT = 1;
    public static final int FEED_WRANGLER_ACCOUNT = 2;
    public static final int INOREADER_ACCOUNT = 3;

    // Feed filter constants
    public static final int FAVORITES = 0;
    public static final int UNREAD = 1;
    public static final int EVERYTHING = 2;


    // Preference keys
    public static final String SELECTED_NAV_POS_PREF_KEY = "SELECTED_NAV_POS_PREF_KEY";
    public static final String SELECTED_TAG_NAME_PREF_KEY = "SELECTED_TAG_NAME_PREF_KEY";
    public static final String SELECTED_SUBSCRIPTION_ID_PREF_KEY = "SELECTED_SUBSCRIPTION_ID_PREF_KEY";
    public static final String SELECTED_FEED_FILTER_PREF_KEY = "SELECTED_FEED_FILTER_PREF_KEY";
    public static final String EXPANDED_NAV_TAGS_PREF_KEY = "EXPANDED_NAV_TAGS_PREF_KEY";
    public static final String TEMPLATES_EXTRACTED_PREF_KEY = "TEMPLATES_EXTRACTED_PREF_KEY";
    public static final String ACCOUNT_SELECTED_PREF_KEY = "ACCOUNT_SELECTED_PREF_KEY";
    public static final String LAST_SYNC_TIME_PREF_KEY = "LAST_SYNC_TIME_PREF_KEY";
    public static final String BETA_EXPIRED_PREF_KEY = "BETA_EXPIRED_PREF_KEY";
    public static final String SORT_ORDER_NEWER_TO_OLDER_PREF_KEY = "SORT_ORDER_NEWER_TO_OLDER_PREF_KEY";
    public static final String READ_ARTICLES_COUNT_PREF_KEY = "READ_ARTICLES_COUNT_PREF_KEY";
    public static final String RATE_PROMPT_SHOWN_PREF_KEY = "RATE_PROMPT_SHOWN_PREF_KEY";

    private static InternalStatePrefs internalStatePrefs;

    public String selectedTagName;
    public String selectedSubscriptionId;
    public Set<String> expandedNavTags;
    public int selectedNavPos;
    public int selectedFeedFilter;
    public boolean templateExtracted;
    public boolean isBetaExpired;
    public boolean sortNewerToOlder;
    public int currentAccount;
    public long lastSyncTime;
    public int readArticlesCount;
    public boolean ratePromptShown;
    private SharedPreferences internalPreferences;


    private InternalStatePrefs(Context context) {
        internalPreferences = context.getSharedPreferences(INTERNAL_PREFS, Context.MODE_PRIVATE);
        internalPreferences.registerOnSharedPreferenceChangeListener(this);
        updatePreferences();
    }

    public static InternalStatePrefs getInstance(Context context) {
        if (internalStatePrefs == null) {
            internalStatePrefs = new InternalStatePrefs(context);
        }

        return internalStatePrefs;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferences();
    }

    private void updatePreferences() {
        selectedTagName = internalPreferences.getString(SELECTED_TAG_NAME_PREF_KEY, "");
        selectedSubscriptionId = internalPreferences.getString(SELECTED_SUBSCRIPTION_ID_PREF_KEY, "");
        selectedFeedFilter = internalPreferences.getInt(SELECTED_FEED_FILTER_PREF_KEY, 1);
        selectedNavPos = internalPreferences.getInt(SELECTED_NAV_POS_PREF_KEY, 0);
        expandedNavTags = internalPreferences.getStringSet(EXPANDED_NAV_TAGS_PREF_KEY, new HashSet<>());
        templateExtracted = internalPreferences.getBoolean(TEMPLATES_EXTRACTED_PREF_KEY, false);
        currentAccount = internalPreferences.getInt(ACCOUNT_SELECTED_PREF_KEY, -1);
        lastSyncTime = internalPreferences.getLong(LAST_SYNC_TIME_PREF_KEY, 0);
        isBetaExpired = internalPreferences.getBoolean(BETA_EXPIRED_PREF_KEY, false);
        sortNewerToOlder = internalPreferences.getBoolean(SORT_ORDER_NEWER_TO_OLDER_PREF_KEY, true);
        readArticlesCount = internalPreferences.getInt(READ_ARTICLES_COUNT_PREF_KEY, 0);
        ratePromptShown = internalPreferences.getBoolean(RATE_PROMPT_SHOWN_PREF_KEY, false);
    }

    @Override
    protected void finalize() throws Throwable {
        internalPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void setStringPref(String key, String value) {
        SharedPreferences.Editor editor = internalPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void setIntPref(String key, int value) {
        SharedPreferences.Editor editor = internalPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void setLongPref(String key, long value) {
        SharedPreferences.Editor editor = internalPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void setBooleanPref(String key, boolean value) {
        SharedPreferences.Editor editor = internalPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void setStringSetPrefs(String key, Set<String> value) {
        SharedPreferences.Editor editor = internalPreferences.edit();
        editor.putStringSet(key, value);
        editor.commit();
    }

}
