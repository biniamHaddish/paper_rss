package com.biniam.rss.ui.base;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.biniam.rss.R;
import com.biniam.rss.persistence.db.PaperDatabase;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.persistence.preferences.PaperPrefs;
import com.biniam.rss.utils.AccountBroker;
import com.biniam.rss.utils.DateUtils;
import com.biniam.rss.utils.HouseKeeper;
import com.biniam.rss.utils.PaperApp;


import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class SyncPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    public static final String TAG = SyncPreferencesFragment.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private AccountBroker accountBroker;
    private SyncPreferencesFragment mContext;
    private static PaperDatabase rssDatabase;
    private PaperPrefs paperPrefs;
    private HouseKeeper houseKeeper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sync_preferences);
        mContext = this;
        accountBroker = AccountBroker.getInstance(getActivity());
        paperPrefs = PaperPrefs.getInstance(getActivity());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        rssDatabase = PaperApp.getInstance().getDatabase();
        houseKeeper = HouseKeeper.getInstance(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


        if (key.equals(getString(R.string.pref_auto_sync_title))) {
            if (paperPrefs.automaticSync) {
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
            if (paperPrefs.automaticSync) accountBroker.scheduleAccountJob();
        }
        if (key.equals(getString(R.string.pref_read_Items_keep_key))) {
            // Clean up caches and database to reflect new settings
            new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver s) {
                    /* keep the code her for the how long will tha app retain the feed Data*/
                    Log.d(TAG, "DaysToMilliSec:" + DateUtils.numDaysToMiliSec(paperPrefs.unreadItemsToKeep));
                    long keeptime = DateUtils.numDaysToMiliSec(paperPrefs.unreadItemsToKeep);
                    long finalTime = System.currentTimeMillis() - keeptime;

                    Log.d(TAG, "finalDate> " + (System.currentTimeMillis() - keeptime));
                    List<String> listOfIds = houseKeeper.getImagesCacheIds(finalTime);
                    SubscriptionEntity[] subscriptionEntities = rssDatabase.dao().getAllSubscriptions();
                    for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
                        houseKeeper.imagesCacheDirStr(subscriptionEntity, listOfIds);
                    }
                    houseKeeper.deleteOldEntries(finalTime);
                }
            }.subscribeOn(Schedulers.computation())
                    .subscribeWith(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            Log.d(TAG, "onComplete: ");
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Log.e(TAG, "onError: " + e.getMessage());
                        }
                    });

        }

    }
}