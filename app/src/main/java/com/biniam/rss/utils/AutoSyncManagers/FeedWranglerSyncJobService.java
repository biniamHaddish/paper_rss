package com.biniam.rss.utils.AutoSyncManagers;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.biniam.rss.connectivity.feedWangler.retrofit.FeedWranglerAPI;
import com.biniam.rss.connectivity.feedWangler.retrofit.FeedWranglerClient;
import com.biniam.rss.models.feedWragler.FeedItemsList;
import com.biniam.rss.persistence.db.ReadablyDatabase;
import com.biniam.rss.persistence.preferences.ReadablyPrefs;
import com.biniam.rss.utils.AutoImageCache;
import com.biniam.rss.utils.ReadablyApp;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;

/**
 * Created by biniam on 12/26/17.
 */

public class FeedWranglerSyncJobService extends JobService {

    public static final String TAG = FeedWranglerSyncJobService.class.getSimpleName();
    public static int JOB_ID = 332703;
    private Context mContext;
    private ReadablyDatabase rssDatabase;
    private long syncStartTime;
    private ReadablyPrefs readablyPrefs;
    private AutoImageCache autoImageCache;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        rssDatabase = ReadablyApp.getInstance().getDatabase();
        readablyPrefs = ReadablyPrefs.getInstance(getApplicationContext());
        autoImageCache = AutoImageCache.getInstance(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (intent != null) {
//            List<String> subscriptionIds = (List<String>) intent.getSerializableExtra(SUBSCRIPTION_IDS);
//            startSync(null, subscriptionIds);
//        }
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    /**
     * Will sync the feed item of the
     * @param limit
     */
    private void syncFeedItem(boolean read,int limit){
        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .retrieveFeedItem(read,limit)
                .filter(new Predicate<FeedItemsList>() {
                    @Override
                    public boolean test(FeedItemsList itemsList) throws Exception {
                        return itemsList.getCount() > 0;  // filter and return if only the items are greater than Zero;
                    }
                }).subscribe(new Observer<FeedItemsList>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()){
                            return;
                        }
                    }
                    @Override
                    public void onNext(FeedItemsList feedItemsList) {
                        Log.d(TAG, "onNext: "+feedItemsList.getResult());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: "+e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }






}
