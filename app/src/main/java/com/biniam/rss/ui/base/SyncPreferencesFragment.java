package com.biniam.rss.ui.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.biniam.rss.R;
import com.biniam.rss.persistence.preferences.ReadablyPrefs;
import com.biniam.rss.utils.AccountBroker;
import com.biniam.rss.utils.HouseKeeper;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class SyncPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    public static final String TAG = SyncPreferencesFragment.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private ReadablyPrefs readablyPrefs;
    private AccountBroker accountBroker;
    private HouseKeeper houseKeeper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sync_preferences);

        accountBroker = AccountBroker.getInstance(getActivity());
        readablyPrefs = ReadablyPrefs.getInstance(getActivity());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        houseKeeper = HouseKeeper.getInstance(getActivity().getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


        if (key.equals(getString(R.string.pref_auto_sync_title))) {
            if (readablyPrefs.automaticSync) {
                accountBroker.scheduleAccountJob();
            } else {
                Log.e(TAG, "onSharedPreferenceChanged: cancelling all jobs b/c auto sync is turned off");
                accountBroker.cancelAccountJob();
            }
        }


        // If any of the settings that affect automatic scheduling are altered we reschedule jobs
        if (key.equals(getString(R.string.pref_auto_sync_wifi_only_title))
                || key.equals(getString(R.string.pref_auto_sync_interval_title))) {
            Log.e(TAG, "onSharedPreferenceChanged: rescheduling jobs because of setting changes");
            if (readablyPrefs.automaticSync) accountBroker.scheduleAccountJob();
        }
        if (key.equals(getString(R.string.pref_unread_items_to_keep_title)) ||
                key.equals(getString(R.string.pref_keep_read_items_for_title))) {
            // Clean up caches and database to reflect new settings
            new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver s) {
                    houseKeeper.deleteOldCaches();
                }
            }.subscribeOn(Schedulers.computation())
                    .subscribeWith(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    });

        }

    }

}