package com.biniam.rss.utils.AutoSyncManagers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.biniam.rss.R;
import com.biniam.rss.connectivity.inoreader.InoApiFactory;
import com.biniam.rss.persistence.db.ReadablyDatabase;
import com.biniam.rss.persistence.preferences.InternalStatePrefs;
import com.biniam.rss.utils.AccountBroker;
import com.biniam.rss.utils.FeedSyncNotificationManager;
import com.biniam.rss.utils.HouseKeeper;
import com.biniam.rss.utils.ReadablyApp;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by biniam on 2/18/18.
 */

public class InoReaderSyncService extends JobService {

    public static final String TAG = InoReaderSyncService.class.getSimpleName();
    public static final int JOB_ID = 332704; // Job Id to identify this service from the rest of services
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = ".started_from_notification";
    private Context mContext;
    private long syncStartTime;
    private HouseKeeper houseKeeper;
    private AccountBroker accountBroker;
    private InoApiFactory inoApiFactory;
    private FeedSyncNotificationManager feedSyncNotificationManager;
    private NotificationManager notificationManager;
    private InternalStatePrefs internalStatePrefs;
    private ReadablyDatabase rssDatabase;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!accountBroker.isCurrentAccountInoreader()) {
            sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_FINISHED));
            stopSelf();
        }
        startSync(null);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        houseKeeper = HouseKeeper.getInstance(getApplicationContext());
        accountBroker = AccountBroker.getInstance(getApplicationContext());
        inoApiFactory = InoApiFactory.getInstance(getApplicationContext());
        internalStatePrefs = InternalStatePrefs.getInstance(getApplicationContext());
        rssDatabase = ReadablyApp.getInstance().getDatabase();

    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
     */
    @Override
    public void onDestroy() {
        //  unregisterReceiver(notificationBroadcastReceiver);
        super.onDestroy();
        Log.e(TAG, "onDestroy: stopping service");
    }


    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        if (!accountBroker.isCurrentAccountInoreader()) {
            jobFinished(jobParameters, false);
            sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_FINISHED));
            return false;
        }
        startSync(jobParameters);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
    /**
     * Here is where the syncing of the Account starts .
     *
     * @param jobParameters
     */
    private void startSync(JobParameters jobParameters) {
        //init the sync here
        sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STARTED));
        syncStartTime = System.currentTimeMillis();

        //Running the Sync
        new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver completableObserver) {
                // Sync subscriptions
                inoApiFactory.getSubscriptionList();// will sync the subscription

                if (jobParameters != null) {
                    jobFinished(jobParameters, false);
                }

                sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_FINISHED));
                houseKeeper.deleteOldCaches();

            }
        }.subscribeOn(Schedulers.single())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        if (jobParameters == null) {
                            stopSelf();
                        } else {
                            jobFinished(jobParameters, false);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: " + throwable.getMessage());
                    }
                });
    }
}
