package com.biniam.rss.connectivity.local;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.biniam.rss.BuildConfig;
import com.biniam.rss.persistence.db.ReadablyDatabase;
import com.biniam.rss.persistence.db.roomentities.FeedItemEntity;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.persistence.preferences.InternalStatePrefs;
import com.biniam.rss.persistence.preferences.ReadablyPrefs;
import com.biniam.rss.utils.AccountBroker;
import com.biniam.rss.utils.AutoImageCache;
import com.biniam.rss.utils.ConnectivityState;
import com.biniam.rss.utils.FavIconFetcher;
import com.biniam.rss.utils.HouseKeeper;
import com.biniam.rss.utils.ReadablyApp;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by biniam_Haddish on 12/10/17.
 * <p>
 * This service takes care of the logic required to sync raw rss/atom
 */

public class LocalSyncJobService extends JobService {


    public static final int JOB_ID = 332701;
    public static final String TAG = LocalSyncJobService.class.getSimpleName();
    public static final String SUBSCRIPTION_IDS = "SUBSCRIPTION_IDS";


    private OkHttpClient client = new OkHttpClient.Builder().build();
    private long syncStartTime;
    private ReadablyDatabase readablyDatabase;
    private AutoImageCache autoImageCache;
    private InternalStatePrefs internalStatePrefs;
    private HouseKeeper houseKeeper;
    private AccountBroker accountBroker;
    //private BetaTestExpiryChecker betaTestExpiryChecker;

    @Override
    public void onCreate() {
        readablyDatabase = ReadablyApp.getInstance().getDatabase();
        autoImageCache = AutoImageCache.getInstance(getApplicationContext());
        internalStatePrefs = InternalStatePrefs.getInstance(getApplicationContext());
        houseKeeper = HouseKeeper.getInstance(getApplicationContext());
        accountBroker = AccountBroker.getInstance(getApplicationContext());
        //betaTestExpiryChecker = BetaTestExpiryChecker.getInstance(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showToast("Manual sync starting");

        if (!accountBroker.isCurrentAccountLocal()) {
            stopSelf();
        }

        if (intent != null) {
            List<String> subscriptionIds = (List<String>) intent.getSerializableExtra(SUBSCRIPTION_IDS);
            startSync(null, subscriptionIds);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        showToast("Scheduled Sync starting");

        if (!accountBroker.isCurrentAccountLocal()) {
            jobFinished(jobParameters, false);
            return false;
        }

        startSync(jobParameters, null);
        return true;
    }


    private void startSync(JobParameters jobParameters, List<String> subscriptionIds) {
        new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {

                // Check if the beta release is expired
//                if (!internalStatePrefs.isBetaExpired) betaTestExpiryChecker.checkBetaExpiry();
//
//                if (internalStatePrefs.isBetaExpired){
//                    sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_FINISHED));
//                    if (jobParameters == null) {
//                        stopSelf();
//                    } else {
//                        jobFinished(jobParameters, false);
//                    }
//                    return;
//                }

                if (subscriptionIds == null) {
                    sync(readablyDatabase.dao().getAllSubscriptions());
                } else {
                    sync(readablyDatabase.dao().getSubscriptions(subscriptionIds));
                }

                if (jobParameters != null) {
                    jobFinished(jobParameters, false);
                }

                houseKeeper.deleteOldCaches();

                s.onComplete();
            }
        }.subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        if (jobParameters == null) {
                            showToast("Manual sync complete");
                            stopSelf();
                        } else {
                            showToast("Scheduled sync complete");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void sync(SubscriptionEntity[] subscriptionEntities) {

        AtomRssParser atomRssParser = AtomRssParser.getInstance(getApplicationContext());
        ReadablyPrefs readablyPrefs = ReadablyPrefs.getInstance(getApplicationContext());

        syncStartTime = System.currentTimeMillis();


        sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STARTED));

        for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
            try {
                // Don't sync this subscription if it has been updated less 15 minutes ago
                if (true) {


                    List<FeedItemEntity> filteredFeedItemEntities = new ArrayList<>();
                    List<FeedItemEntity> parsedFeedItemEntities = atomRssParser.getFeedItems(getXmlString(subscriptionEntity.rssLink), subscriptionEntity);


                    for (FeedItemEntity parsedFeedItem : parsedFeedItemEntities) {
                        FeedItemEntity existing = readablyDatabase.dao().getFeedItem(parsedFeedItem.id);

                        if (existing == null) {
                            filteredFeedItemEntities.add(parsedFeedItem);
                        }
                    }


                    readablyDatabase.dao().insertFeedItems(
                            filteredFeedItemEntities
                    );

                    internalStatePrefs.setLongPref(InternalStatePrefs.LAST_SYNC_TIME_PREF_KEY, System.currentTimeMillis());

                    Log.d(TAG, String.format("%s synced successfully", subscriptionEntity.title));
                    // Update updated timestamp
                    subscriptionEntity.lastUpdatedTimestamp = System.currentTimeMillis();
                    readablyDatabase.dao().updateSubscription(subscriptionEntity);


                    // Get high-res fav icon
                    if (subscriptionEntity.iconUrl == null) {
                        String favIconUrl = FavIconFetcher.getFavIconUrl(subscriptionEntity.siteLink);

                        if (favIconUrl != null && !favIconUrl.isEmpty()) {
                            Log.w(TAG, String.format("onNext: favicon url is %s", favIconUrl));
                            subscriptionEntity.iconUrl = favIconUrl;
                            readablyDatabase.dao().updateSubscription(subscriptionEntity);
                        }
                    }

                } else {
                    Log.d(TAG, String.format("sync: %s was synced less than 15 minutes ago skipping...", subscriptionEntity.title));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, String.format("sync: error syncing %s, reason: %s", subscriptionEntity.title, e.getMessage()));
            }
        }

        if (!readablyPrefs.autoCacheImages) {
            return;
        } else if (readablyPrefs.automaticSyncWiFiOnly && ConnectivityState.isOnWiFi()) {
            autoImageCache.startCaching(syncStartTime);
        } else if (readablyPrefs.autoCacheImages && !readablyPrefs.automaticSyncWiFiOnly && ConnectivityState.hasDataConnection()) {
            autoImageCache.startCaching(syncStartTime);
        }

        sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_FINISHED));
        //houseKeeper.deleteOldCaches();
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    public String getXmlString(String subscriptionUrl) throws Exception {
        if (subscriptionUrl == null || subscriptionUrl.isEmpty()) {
            throw new IllegalArgumentException("Rss url can not be null or empty");
        }

        Log.d(TAG, String.format("getUrlStringContents: getting xml for %s", subscriptionUrl));

        client.followRedirects();
        Request request = new Request.Builder().url(subscriptionUrl).build();
        Response response = client.newCall(request).execute();
        String body = response.body().string();
        response.close();
        return body;
    }

    private void showToast(String message) {
        if (BuildConfig.DEBUG) Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isLocalAccountSelected() {
        return internalStatePrefs.currentAccount == InternalStatePrefs.LOCAL_ACCOUNT;
    }
}
