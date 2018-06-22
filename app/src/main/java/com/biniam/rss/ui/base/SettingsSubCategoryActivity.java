package com.biniam.rss.ui.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.biniam.rss.R;

public class SettingsSubCategoryActivity extends AppCompatActivity {

    public static final String PREF_CAT_EXTRA = "PREF_CAT_EXTRA";

    public static final int READING_PREF = 0;
    public static final int SYNC_PREF = 1;
    public static final int ABOUT_PREF = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(PREF_CAT_EXTRA)) {
            int cat = getIntent().getIntExtra(PREF_CAT_EXTRA, 0);
            if (cat == READING_PREF) {
                setTitle(getString(R.string.reading_ux));
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new ReadingPreferencesFragment())
                        .commit();
            } else if (cat == SYNC_PREF) {
                setTitle(getString(R.string.sync));
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new SyncPreferencesFragment())
                        .commit();
            }
        }


    }

}
