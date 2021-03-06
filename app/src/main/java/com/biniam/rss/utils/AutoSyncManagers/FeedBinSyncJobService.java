package com.biniam.rss.utils.AutoSyncManagers;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.biniam.rss.connectivity.feedbin.feedbinApi.FeedbinAPI;
import com.biniam.rss.connectivity.feedbin.retrofitClient.RetrofitFeedbinClient;
import com.biniam.rss.models.feedbin.FeedBinEntriesItem;
import com.biniam.rss.models.feedbin.FeedBinSubscriptionsItem;
import com.biniam.rss.models.feedbin.FeedBinTaggingsItem;
import com.biniam.rss.persistence.db.PaperDatabase;
import com.biniam.rss.persistence.db.roomentities.FeedItemEntity;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.persistence.db.roomentities.TagEntity;
import com.biniam.rss.persistence.preferences.InternalStatePrefs;
import com.biniam.rss.persistence.preferences.PaperPrefs;
import com.biniam.rss.ui.controllers.FeedParser;
import com.biniam.rss.utils.AccountBroker;
import com.biniam.rss.utils.AutoImageCache;
import com.biniam.rss.utils.ConnectivityState;
import com.biniam.rss.utils.DateUtils;
import com.biniam.rss.utils.FavIconFetcher;
import com.biniam.rss.utils.HouseKeeper;
import com.biniam.rss.utils.PaperApp;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;


/**
 * Created by biniam_Haddish on 1/10/18.
 *
 * This service takes care of the logic required to sync with feedbin.com
 *
 */

public class FeedBinSyncJobService extends JobService {

    public static final String TAG = FeedBinSyncJobService.class.getSimpleName();
    public static final String SUBSCRIPTION_IDS = "SUBSCRIPTION_IDS";
    public static final String STARRED = "starred_entries";
    public static final String UNREAD = "unread_entries";
    public static final int PER_PAGE_LIMIT = 100;

    /**
     * Unique job ID for this service.
     */
    public static final int JOB_ID = 332702;

    Context context;
    private PaperDatabase paperDatabase;
    private long syncStartTime;
    private PaperPrefs paperPrefs;
    private AutoImageCache autoImageCache;
    private InternalStatePrefs internalStatePrefs;
    private HouseKeeper houseKeeper;
    private AccountBroker accountBroker;
    //private BetaTestExpiryChecker betaTestExpiryChecker;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!accountBroker.isCurrentAccountFeedBin()) {
            sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_FINISHED));
            stopSelf();
        }

        startSync(null);
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        if (!accountBroker.isCurrentAccountFeedBin()) {
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


    private void startSync(JobParameters jobParameters) {
        sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STARTED));
        syncStartTime = System.currentTimeMillis();

        new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {


                // Sync subscriptions
                syncSubscriptions();

                if (jobParameters != null) {
                    jobFinished(jobParameters, false);
                }

                sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_FINISHED));


                //houseKeeper.deleteOldCaches();

                s.onComplete();

            }
        }.subscribeOn(Schedulers.single())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        if (jobParameters == null) {
                            stopSelf();
                        }
                    }
                });
    }

    /**
     * onCreate() Called by the system when the service is first created.
     * so all the init. process should go here
     */
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        paperDatabase = PaperApp.getInstance().getDatabase();
        paperPrefs = PaperPrefs.getInstance(getApplicationContext());
        autoImageCache = AutoImageCache.getInstance(getApplicationContext());
        internalStatePrefs = InternalStatePrefs.getInstance(getApplicationContext());
        houseKeeper = HouseKeeper.getInstance(getApplicationContext());
        accountBroker = AccountBroker.getInstance(getApplicationContext());
        //betaTestExpiryChecker = BetaTestExpiryChecker.getInstance(getApplicationContext());
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: stopping service");
    }

    /**
     *
     */
    private void syncSubscriptions() {
        Log.d(TAG, String.format("syncSubscriptions: starting sync"));

        sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_SUBSCRIPTIONS));

        // We have no saved subscriptions we sync them all
        RetrofitFeedbinClient.getRetrofit()
                .create(FeedbinAPI.class)
                .getAllFeedbinSubscription()
                .filter(feedBinSubscriptionsItems -> feedBinSubscriptionsItems != null)
                .subscribe(new Observer<List<FeedBinSubscriptionsItem>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(List<FeedBinSubscriptionsItem> subscriptionsItems) {
                        Log.d(TAG, "onNext: subscriptions synced");
                        List<SubscriptionEntity> allSyncedSubscriptionEntities = convertFeedBinSubscriptions(subscriptionsItems);
                        List<String> syncedSubscriptionIds = getSyncedSubscriptionIds(subscriptionsItems);
                        paperDatabase.dao().insertSubscriptions(allSyncedSubscriptionEntities);

                        // Check if we have subscription items without remote equivalents and delete them
                        for (String savedSubscriptionId : paperDatabase.dao().getAllSubscriptionIds()) {
                            if (!syncedSubscriptionIds.contains(savedSubscriptionId)) {
                                // This subscription is not contained in the subscription ids retrieved from feedbin
                                // so it must have been
                                if (isFeedbinSelectedAccount()) {
                                    paperDatabase.dao().deleteSubscription(
                                            paperDatabase.dao().getSubscription(savedSubscriptionId)
                                    );
                                }
                            }
                        }


                        // Get tags
                        syncTags();

                        // Mark items as unread
                        boolean markingItemsAsUnreadSuccessful = markItemsAsUnRead(paperDatabase.dao().getModifiedFeedItemsSinceLastSync(false));

                        // Mark items as read
                        markItemsAsRead(paperDatabase.dao().getModifiedFeedItemsSinceLastSync(true));

                        // Get unread items
                        if (markingItemsAsUnreadSuccessful) syncUnreadItems();


                        // Get read items
                        syncReadItems();

                        syncFavEntries();

                        // Get high-res fav icons
                        for (SubscriptionEntity subscriptionEntity : paperDatabase.dao().getAllSubscriptions()) {


                            if (subscriptionEntity.iconUrl == null) {
                                String favIconUrl = FavIconFetcher.getFavIconUrl(subscriptionEntity.siteLink);

                                if (favIconUrl != null && !favIconUrl.isEmpty()) {
                                    Log.w(TAG, String.format("onNext: favicon url is %s", favIconUrl));
                                    subscriptionEntity.iconUrl = favIconUrl;
                                }

                                paperDatabase.dao().updateSubscription(subscriptionEntity);
                            }
                        }


                        // Cache lead images of new unread items
                        if (!paperPrefs.autoCacheImages) {
                            return;
                        } else if (paperPrefs.automaticSyncWiFiOnly && ConnectivityState.isOnWiFi()) {
                            autoImageCache.startCaching(syncStartTime);
                        } else if (paperPrefs.autoCacheImages && !paperPrefs.automaticSyncWiFiOnly && ConnectivityState.hasDataConnection()) {
                            autoImageCache.startCaching(syncStartTime);
                        }

                        //houseKeeper.deleteOldCaches();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, " onError getting subscriptions : " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, " onComplete");
                    }
                });

    }

    /**
     *
     */
    private void syncTags() {
        RetrofitFeedbinClient.getRetrofit()
                .create(FeedbinAPI.class)
                .getAllTages()
                .filter(feedBinTaggingsItems -> feedBinTaggingsItems != null)
                .subscribe(new Observer<List<FeedBinTaggingsItem>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(List<FeedBinTaggingsItem> feedBinTaggingItems) {
                        Log.d(TAG, "onNext: tags synced");
                        List<TagEntity> tagEntities = convertFeedBinTags(feedBinTaggingItems);
                        List<String> syncedTagIds = getSyncedTagIds(tagEntities);

                        // Lets cleanup removed tags
                        for (TagEntity savedTagEntity : paperDatabase.dao().getAllTags()) {
                            if (!syncedTagIds.contains(savedTagEntity.serverId) && isFeedbinSelectedAccount()) {
                                paperDatabase.dao().deleteTagByServerId(savedTagEntity.subscriptionId, savedTagEntity.name);
                            }
                        }

                        paperDatabase.dao().addTags(tagEntities);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, String.format("onError: tag sync failed reason : %s", e.getMessage()));
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }

                });

    }


    @SuppressLint("CheckResult")
    private void syncUnreadItems() {

        sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_ITEMS));

        Log.w(TAG, "syncUnreadItems: getting all unread items");
        RetrofitFeedbinClient.getRetrofit()
                .create(FeedbinAPI.class)
                .getUnreadEntries()
                .filter(integers -> integers != null)
                .subscribeWith(new DisposableObserver<List<Integer>>() {
                    @Override
                    public void onNext(List<Integer> integers) {
                        if (!integers.isEmpty()) {

                            // Remove already saved ids, this makes syncing very efficient
                            // It also marks unread items in db that doesnt exist here as read
                            for (FeedItemEntity feedItemEntity : paperDatabase.dao().getAllUnreadFeedItems()) {
                                if (integers.contains(Integer.valueOf(feedItemEntity.id))) {
                                    integers.remove(Integer.valueOf(feedItemEntity.id));
                                } else {
                                    feedItemEntity.read = true;
                                    paperDatabase.dao().updateFeedItem(feedItemEntity);
                                }
                            }


                            List<String> sets = makeIdRequestList(integers);

                            final boolean[] successfulAtLeastOnce = {false};

                            for (int i = 0; i < sets.size(); i++) {

                                Log.d(TAG, String.format("onNext: getting entries for set %d", i + 1));

                                RetrofitFeedbinClient.getRetrofit()
                                        .create(FeedbinAPI.class)
                                        .getEntriesByIds(sets.get(i))
                                        .filter(feedBinEntriesItems -> feedBinEntriesItems != null)
                                        .subscribe(new DisposableObserver<List<FeedBinEntriesItem>>() {
                                            @Override
                                            public void onNext(List<FeedBinEntriesItem> feedBinEntriesItems) {
                                                Log.w(TAG, "onNext: got unread items");
                                                successfulAtLeastOnce[0] = true;
                                                if (isFeedbinSelectedAccount())
                                                    paperDatabase.dao().insertFeedItems(convertFeedBinEntries(feedBinEntriesItems, false));
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Log.e(TAG, String.format("onError: error getting unread items reason: %s", e.getMessage()));
                                            }

                                            @Override
                                            public void onComplete() {
                                            }
                                        });
                            }

                            if (successfulAtLeastOnce[0]) {
                                internalStatePrefs.setLongPref(InternalStatePrefs.LAST_SYNC_TIME_PREF_KEY, System.currentTimeMillis());
                            }

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, String.format("onError: error getting unread ids reason : %s", e.getMessage()));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @SuppressLint("CheckResult")
    private void syncReadItems() {

        Log.w(TAG, "syncReadItems: getting read items");

        sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_ITEMS));

        RetrofitFeedbinClient.getRetrofit()
                .create(FeedbinAPI.class)
                .getEntriesForSubscription(true)
                .filter(feedBinEntriesItems -> feedBinEntriesItems != null)
                .subscribeWith(new DisposableObserver<List<FeedBinEntriesItem>>() {
                    @Override
                    public void onNext(List<FeedBinEntriesItem> feedBinEntriesItems) {
                        paperDatabase.dao().insertFeedItems(convertFeedBinEntries(feedBinEntriesItems, true));
                        Log.w(TAG, String.format("onNext: sync success for read items, got %d items", feedBinEntriesItems.size()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, String.format("onError: sync error for read items reason: %s", e.getMessage()));
                    }

                    @Override
                    public void onComplete() {
                    }
                });

    }


    private void syncFavEntries() {
        Log.e(TAG, "syncFavEntriesForSubscription: getting starred items");

        sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_ITEMS));

        RetrofitFeedbinClient.getRetrofit()
                .create(FeedbinAPI.class)
                .getStarredEntries(100)
                .filter(integers -> integers != null)
                .subscribe(new DisposableObserver<List<Integer>>() {
                    @Override
                    public void onNext(List<Integer> integers) {
                        Log.w(TAG, String.format("onNext: got %d starred items", integers.size()));

                        RetrofitFeedbinClient.getRetrofit()
                                .create(FeedbinAPI.class)
                                .getEntriesByIds(makeStringForIdsRequest(integers))
                                .filter(feedBinEntriesItems -> feedBinEntriesItems != null)
                                .subscribe(new DisposableObserver<List<FeedBinEntriesItem>>() {
                                    @Override
                                    public void onNext(List<FeedBinEntriesItem> feedBinEntriesItems) {
                                        Log.w(TAG, "onNext: got starred items and saving them");
                                        if (isFeedbinSelectedAccount()) {
                                            paperDatabase.dao().insertFeedItems(convertFavFeedBinEntries(feedBinEntriesItems));
                                            updateUnStarredItems(integers);
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.e(TAG, "onError: error getting starred items", e);
                                    }

                                    @Override
                                    public void onComplete() {
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: error getting starred items");
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private boolean markItemsAsRead(int[] entryIds) {
        if (entryIds.length == 0) {
            Log.e(TAG, "markItemsAsRead: zero items marked read since last sync");
            return true;
        }

        if (entryIds.length > 0) {
            JsonObject jsonObject = makeIdsForRequest(UNREAD, entryIds);
            Log.w(TAG, String.format("markItemsAsRead: marking %d items read", entryIds.length));

            try {
                Response<JsonArray> response = RetrofitFeedbinClient
                        .getRetrofit()
                        .create(FeedbinAPI.class)
                        .markAsRead(jsonObject)
                        .execute();

                if (response.code() == 200) {
                    JsonArray responseJsonArray = response.body();
                    Log.w(TAG, String.format("markItemsAsRead: successfully marked %d items as read", responseJsonArray.size()));
                    resetModifiedTime(responseJsonArray);
                    return true;
                }

            } catch (IOException e) {
                Log.e(TAG, String.format("markItemsAsRead: error marking items as read. Reason : %s", e.getMessage()));
                return false;
            }
        }

        return false;
    }


    private boolean markItemsAsUnRead(int[] entryIds) {
        if (entryIds.length == 0) return true;

        if (entryIds.length > 0) {
            JsonObject jsonObject = makeIdsForRequest(UNREAD, entryIds);
            Log.w(TAG, String.format("markItemsAsRead: marking %d items unread", entryIds.length));

            try {
                Response<JsonArray> response = RetrofitFeedbinClient
                        .getRetrofit()
                        .create(FeedbinAPI.class)
                        .markAsUnRead(jsonObject)
                        .execute();

                if (response.code() == 200) {
                    JsonArray jsonArray = response.body();
                    Log.w(TAG, String.format("markItemsAsRead: successfully marked %d items as unread", jsonArray.size()));
                    resetModifiedTime(jsonArray);
                    return true;
                }

            } catch (IOException e) {
                Log.e(TAG, String.format("markItemsAsUnRead: error marking items as unread : %s", e.getMessage()));
                return false;
            }
        }

        return false;
    }


    private boolean markItemsAsFavorite(int[] entryIds) {
        if (entryIds.length == 0) {
            Log.e(TAG, "markItemsAsFavorite: no new items marked favorite");
            return true;
        }

        if (entryIds.length > 0) {
            JsonObject jsonObject = makeIdsForRequest(STARRED, entryIds);

            Log.d(TAG, String.format("markItemsAsFavorite: marking items starred %s", jsonObject.toString()));

            try {

                Response<JsonArray> response = RetrofitFeedbinClient
                        .getRetrofit()
                        .create(FeedbinAPI.class)
                        .markAsFavorite(jsonObject)
                        .execute();

                if (response.code() == 200) {
                    Log.d(TAG, String.format("markItemsAsFavorite: success marking items as starred %s", response.body().toString()));
                    return true;
                }


            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, String.format("markItemsAsFavorite: error marking items starred reason: %s", e.getMessage()));
                return false;
            }
        }

        return false;
    }

    private List<SubscriptionEntity> convertFeedBinSubscriptions(List<FeedBinSubscriptionsItem> feedBinSubscriptionsItems) {
        List<SubscriptionEntity> subscriptionEntities = new ArrayList<>();


        for (FeedBinSubscriptionsItem feedBinSubscriptionsItem : feedBinSubscriptionsItems) {

            SubscriptionEntity savedSubscriptionEntity = paperDatabase.dao().getSubscription(String.valueOf(feedBinSubscriptionsItem.getFeed_id()));

            if (savedSubscriptionEntity != null) {
                if (!savedSubscriptionEntity.title.equals(feedBinSubscriptionsItem.getTitle())) {
                    savedSubscriptionEntity.title = feedBinSubscriptionsItem.getTitle();
                    paperDatabase.dao().updateSubscription(savedSubscriptionEntity);
                }

            } else {
                SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
                subscriptionEntity.id = String.valueOf(feedBinSubscriptionsItem.getFeed_id());
                subscriptionEntity.title = feedBinSubscriptionsItem.getTitle();
                subscriptionEntity.siteLink = feedBinSubscriptionsItem.getSite_url();
                subscriptionEntity.rssLink = feedBinSubscriptionsItem.getFeed_url();

                subscriptionEntities.add(subscriptionEntity);
            }




        }

        return subscriptionEntities;
    }

    private List<String> getSyncedSubscriptionIds(List<FeedBinSubscriptionsItem> feedBinSubscriptionsItems) {
        List<String> syncedSubscriptionIds = new ArrayList<>();

        for (FeedBinSubscriptionsItem feedBinSubscriptionsItem : feedBinSubscriptionsItems) {
            syncedSubscriptionIds.add(String.valueOf(feedBinSubscriptionsItem.getFeed_id()));
        }

        return syncedSubscriptionIds;
    }

    private List<String> getSyncedTagIds(List<TagEntity> tagEntities) {
        List<String> syncedTagIds = new ArrayList<>();
        for (TagEntity tagEntity : tagEntities) {
            syncedTagIds.add(tagEntity.serverId);
        }

        return syncedTagIds;
    }

    private List<TagEntity> convertFeedBinTags(List<FeedBinTaggingsItem> feedBinTaggingsItems) {
        List<TagEntity> tagEntities = new ArrayList<>();
        for (FeedBinTaggingsItem feedBinTaggingsItem : feedBinTaggingsItems) {
            TagEntity tagEntity = new TagEntity();
            tagEntity.subscriptionId = String.valueOf(feedBinTaggingsItem.getSubscriptionId());
            tagEntity.name = feedBinTaggingsItem.getTagName();
            tagEntity.serverId = String.valueOf(feedBinTaggingsItem.getTagId());
            tagEntities.add(tagEntity);
        }

        return tagEntities;
    }

    private List<FeedItemEntity> convertFeedBinEntries(List<FeedBinEntriesItem> feedBinEntriesItems, boolean read) {

        List<FeedItemEntity> feedItemEntities = new ArrayList<>();
        FeedParser feedParser = FeedParser.getInstance(getApplicationContext());


        for (FeedBinEntriesItem feedBinEntriesItem : feedBinEntriesItems) {

            FeedItemEntity existing = paperDatabase.dao().getFeedItem(feedBinEntriesItem.getId());
            SubscriptionEntity subscriptionEntity = paperDatabase.dao().getSubscription(feedBinEntriesItem.getSubscriptionId());

            if (existing == null && subscriptionEntity != null) {
                FeedItemEntity feedItemEntity = new FeedItemEntity(
                        feedBinEntriesItem.getTitle(),
                        feedBinEntriesItem.getSubscriptionId(),
                        feedBinEntriesItem.getId(),
                        DateUtils.fromISO8601UTC(feedBinEntriesItem.getPublished()),
                        feedBinEntriesItem.getContent(),
                        feedBinEntriesItem.getSummary(),
                        feedBinEntriesItem.getUrl(),
                        subscriptionEntity.title
                );

                feedItemEntity.createdAt = DateUtils.fromISO8601UTC(feedBinEntriesItem.getCreated_at());
                feedItemEntity.author = feedBinEntriesItem.getAuthor();
                feedItemEntity.read = read;
                if (!read) feedItemEntity.syncedAt = System.currentTimeMillis();

                feedItemEntity = feedParser.parseFeedItem(feedItemEntity);
                feedItemEntities.add(feedItemEntity);
            } else if (existing != null) {
                if (existing.modifiedAt == 0) existing.read = read;
                paperDatabase.dao().updateFeedItem(existing);
            }
        }

        return feedItemEntities;
    }


    private List<FeedItemEntity> convertFavFeedBinEntries(List<FeedBinEntriesItem> feedBinEntriesItems) {

        List<FeedItemEntity> feedItemEntities = new ArrayList<>();
        FeedParser feedParser = FeedParser.getInstance(getApplicationContext());
        for (FeedBinEntriesItem feedBinEntriesItem : feedBinEntriesItems) {

            FeedItemEntity existing = paperDatabase.dao().getFeedItem(feedBinEntriesItem.getId());

            if (existing != null) {
                existing.favorite = true;
                paperDatabase.dao().updateFeedItem(existing);
            } else {
                SubscriptionEntity subscriptionEntity = paperDatabase.dao().getSubscription(feedBinEntriesItem.getSubscriptionId());

                if (subscriptionEntity == null) continue;

                FeedItemEntity feedItemEntity = new FeedItemEntity(
                        feedBinEntriesItem.getTitle(),
                        feedBinEntriesItem.getSubscriptionId(),
                        feedBinEntriesItem.getId(),
                        DateUtils.fromISO8601UTC(feedBinEntriesItem.getPublished()),
                        feedBinEntriesItem.getContent(),
                        feedBinEntriesItem.getSummary(),
                        feedBinEntriesItem.getUrl(),
                        subscriptionEntity != null ? subscriptionEntity.title : ""
                );

                feedItemEntity.createdAt = DateUtils.fromISO8601UTC(feedBinEntriesItem.getCreated_at());
                feedItemEntity.author = feedBinEntriesItem.getAuthor();
                feedItemEntity.syncedAt = System.currentTimeMillis();
                feedItemEntity.modifiedAt = 0;

                feedItemEntity.favorite = true;
                feedItemEntity.read = false;
                feedItemEntity = feedParser.parseFeedItem(feedItemEntity);
                feedItemEntities.add(feedItemEntity);
            }
        }

        return feedItemEntities;
    }


    private void updateUnStarredItems(List<Integer> ids) {
        int[] starredIds = paperDatabase.dao().getFavItemsIds();

        for (int i = 0; i < starredIds.length; i++) {
            if (!ids.contains(starredIds[i])) {
                FeedItemEntity feedItemEntity = paperDatabase.dao().getFeedItem(String.valueOf(starredIds[i]));
                if (feedItemEntity != null) {
                    feedItemEntity.favorite = false;
                    Log.e(TAG, String.format("updateUnStarredItems: un-starring %s", feedItemEntity.title));
                    paperDatabase.dao().updateFeedItem(feedItemEntity);
                }
            }
        }
    }


    private JsonObject makeIdsForRequest(String property, @NonNull int[] entryIds) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (Number entryId : entryIds) {
            jsonArray.add(Integer.valueOf(entryId.intValue()));
        }
        jsonObject.add(property, jsonArray);
        return jsonObject;
    }


    // Resets the modified time of successfully synced items
    private void resetModifiedTime(JsonArray jsonArray) {
        List<Integer> ids = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            ids.add((jsonArray.get(i).getAsInt()));
        }

        FeedItemEntity[] feedItemEntities = paperDatabase.dao().getFeedItemsForIds(ids);
        for (FeedItemEntity feedItemEntity : feedItemEntities) {
            feedItemEntity.modifiedAt = 0;
        }

        paperDatabase.dao().updateFeedItems(feedItemEntities);
    }

    private String makeStringForIdsRequest(List<Integer> ids) {
        StringBuilder idsStringBuilder = new StringBuilder();

        for (int i = 0; i < ids.size(); i++) {
            if (i == ids.size() - 1) { // No comma for the last one
                idsStringBuilder.append(ids.get(i));
            } else {
                idsStringBuilder.append(ids.get(i) + ",");
            }
        }

        return idsStringBuilder.toString();
    }


    private List<String> makeIdRequestList(List<Integer> ids) {
        List<String> requestStrings = new ArrayList<>();


        if (ids.size() / PER_PAGE_LIMIT == 0) {
            requestStrings.add(makeStringForIdsRequest(ids));
        } else {
            int startIndex = 0;
            int endIndex = PER_PAGE_LIMIT - 1;
            int setCount = ids.size() / PER_PAGE_LIMIT;

            Log.d(TAG, String.format("makeIdRequestList: set count is %d", setCount));

            for (int i = 0; i < setCount; i++) {
                requestStrings.add(makeStringForIdsRequest(ids.subList(startIndex, endIndex)));
                //Log.d(TAG, String.format("makeIdRequestList: set %d has %d items", i + 1, ids.subList(startIndex, endIndex).size()));
                if (i <= setCount - 1) {
                    startIndex = endIndex;
                    endIndex = startIndex + PER_PAGE_LIMIT;
                }
            }

            if (ids.size() % PER_PAGE_LIMIT > 0) {
                requestStrings.add(makeStringForIdsRequest(ids.subList((PER_PAGE_LIMIT * setCount) - 1, ids.size())));
                Log.d(TAG, String.format("makeIdRequestList: last set has %d items", ids.subList(PER_PAGE_LIMIT * setCount, ids.size() - 1).size()));
            }
        }


        return requestStrings;
    }

    private boolean isFeedbinSelectedAccount() {
        return internalStatePrefs.currentAccount == InternalStatePrefs.FEED_BIN_ACCOUNT;
    }

}