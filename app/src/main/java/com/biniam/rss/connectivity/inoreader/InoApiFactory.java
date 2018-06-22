package com.biniam.rss.connectivity.inoreader;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.biniam.rss.connectivity.inoreader.inoReaderApi.InoReaderAPI;
import com.biniam.rss.models.inoreader.InoFoldersTagsList;
import com.biniam.rss.models.inoreader.InoReaderSubscriptionItems;
import com.biniam.rss.models.inoreader.InoReaderUserInfo;
import com.biniam.rss.models.inoreader.InoStreamContentList;
import com.biniam.rss.models.inoreader.InoSubscriptionList;
import com.biniam.rss.models.inoreader.InoUnreadCount;
import com.biniam.rss.persistence.db.ReadablyDatabase;
import com.biniam.rss.persistence.db.roomentities.FeedItemEntity;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.persistence.db.roomentities.TagEntity;
import com.biniam.rss.persistence.preferences.InternalStatePrefs;
import com.biniam.rss.persistence.preferences.ReadablyPrefs;
import com.biniam.rss.ui.controllers.FeedParser;
import com.biniam.rss.utils.AccountBroker;
import com.biniam.rss.utils.AutoImageCache;
import com.biniam.rss.utils.ConnectivityState;
import com.biniam.rss.utils.HouseKeeper;
import com.biniam.rss.utils.ReadablyApp;
import com.biniam.rss.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.ComputationScheduler;
import io.reactivex.internal.schedulers.NewThreadScheduler;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by biniam on 2/17/18.
 */

public class InoApiFactory {

    public static final String TAG = InoApiFactory.class.getSimpleName();
    public static final int MAX_LIMIT = 1000;
    public static int ARTICLE_SYNC_COUNT = 500;
    private static InoApiFactory inoApiFactory;
    private Context mContext;
    private long syncStartTime;
    private ReadablyDatabase rssDatabase;
    private ReadablyPrefs readablyPrefs;
    private AutoImageCache autoImageCache;
    private InternalStatePrefs internalStatePrefs;
    private HouseKeeper houseKeeper;
    private AccountBroker accountBroker;

    /**
     * @param context
     */
    private InoApiFactory(Context context) {
        mContext = context;
        rssDatabase = ReadablyApp.getInstance().getDatabase();
        readablyPrefs = ReadablyPrefs.getInstance(context);
        autoImageCache = AutoImageCache.getInstance(context);
        internalStatePrefs = InternalStatePrefs.getInstance(context);
        houseKeeper = HouseKeeper.getInstance(context);
        accountBroker = AccountBroker.getInstance(context);

    }


    /**
     * new instance
     *
     * @param context
     * @return
     */
    public static InoApiFactory getInstance(Context context) {
        if (inoApiFactory == null) {
            inoApiFactory = new InoApiFactory(context);
        }
        return inoApiFactory;
    }

    /**
     * User information like email address username image link and more..
     */
    public void userInfo() {
        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .userInfo()
                .filter(inoReaderUserInfo -> inoReaderUserInfo != null)
                .subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<InoReaderUserInfo>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(InoReaderUserInfo inoReaderUserInfo) {
                        Log.d(TAG, "onNext: \t User email\t"
                                + inoReaderUserInfo.getUserEmail()
                                + "\tuserName\t" + inoReaderUserInfo.getUserName() +
                                "\tUserProfileId\t" + inoReaderUserInfo.getUserProfileId()

                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: \t" + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: \t");
                    }
                });
    }

    /**
     * Get the list of InoReader Subscription List there is no Pagination on the Query
     */
    public void getSubscriptionList() {
        mContext.sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_SUBSCRIPTIONS));
        syncStartTime = System.currentTimeMillis();
        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .getSubscriptionList()
                .filter(inoSubscriptionList -> inoSubscriptionList != null)
                .subscribe(new Observer<InoSubscriptionList>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(InoSubscriptionList inoSubscriptionList) {

                        List<SubscriptionEntity> entities = AddInoReaderSubscriptionToRoom(inoSubscriptionList.getSubscriptions());
                        List<String> syncedSubscriptionIds = getSyncedSubscriptionIds(inoSubscriptionList.getSubscriptions());

                        Log.d(TAG, "syncedSubscriptionIds: " + syncedSubscriptionIds);
                        rssDatabase.dao().insertSubscriptions(entities);// inserting data to the Subscription Table
                        for (String savedSubscriptionId : rssDatabase.dao().getAllSubscriptionIds()) {
                            Log.d(TAG, "savedSubscriptionId: \t" + savedSubscriptionId);
                            if (!syncedSubscriptionIds.contains(savedSubscriptionId)) {
                                if (accountBroker.isCurrentAccountInoreader()) {
                                    rssDatabase.dao().deleteSubscription(rssDatabase.dao().getSubscription(savedSubscriptionId)
                                    );
                                }
                            }
                            /*getting the Feeds by subscription ids*/
                            List<TagEntity> tagEntity = addInoReaderTags(inoSubscriptionList.getSubscriptions());// getting the tag entities
                            rssDatabase.dao().addTags(tagEntity);// adding the Tag into tags table
                            Log.d(TAG, "subscriptionIds\t" + replaceDashWithSlash(savedSubscriptionId));
                        }
                        //sync ino feed items
                        getInoUnreadCount();
                        //sync unread feed items to ino server
                        markFeedItemsAsUnread(rssDatabase.dao().getModifiedFeedEntitiesSinceLastSync(false));//will Mark items as Unread
                        //sync read feed items to ino server
                        markFeedItemsAsRead(rssDatabase.dao().getModifiedFeedEntitiesSinceLastSync(true));//will Mark items As read
                        //sync unStarred to ino server
                        syncUnstarredItems(rssDatabase.dao().getFavedFeedItemEntitySinceLastSync(false));
                        //sync starred feed items to ino server
                        syncStarredItems(rssDatabase.dao().getFavedFeedItemEntitySinceLastSync(true));
                        //get all the starred Items
                        getStarred();
                        //get subscription Fav icons
                        Utils.getSubscriptionFavIcon(rssDatabase);//will get all the necessary subscription logo
                        //if the auto cache is enabled it will Auto Cache the feed items images
                        autoImageCache();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, " getSubscriptionList_Error: \t" + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    /**
     * will Cache images
     */
    private void autoImageCache() {
        if (!readablyPrefs.autoCacheImages) {
            return;
        } else if (readablyPrefs.automaticSyncWiFiOnly && ConnectivityState.isOnWiFi()) {
            autoImageCache.startCaching(syncStartTime);
        } else if (readablyPrefs.autoCacheImages && !readablyPrefs.automaticSyncWiFiOnly && ConnectivityState.hasDataConnection()) {
            autoImageCache.startCaching(syncStartTime);
        }
    }

    /**
     * get List of Feed items
     *
     * @param subscriptionId
     *//*
    public void getFeedItems(String subscriptionId, int count) {

        mContext.sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_ITEMS));

        Map<String, String> query = new HashMap<>();
        query.put("xt", InoReaderUrls.getQueryReadArticles());//will get the unread FeedEntries
        query.put("n", String.valueOf(count));

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .getFeedItems(subscriptionId, query)
                .filter(itemsBeans -> itemsBeans != null)
                .subscribeOn(Schedulers.io())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<InoStreamContentList>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(InoStreamContentList itemsBeans) {
                        Log.d(TAG, "Continuation: \t" + itemsBeans.getContinuation());
                        rssDatabase.dao().insertFeedItems(addInoReaderEntries(itemsBeans.getItems(), subscriptionId));
                        if (itemsBeans.getContinuation() != null) {
                            getMoreArticles(itemsBeans.getContinuation());
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });

    }
*/
    /**
     * Add new InoReader subscription Just give it the site url
     *
     * @param subscriptionUrl
     */
    public void addNewInoSubscription(String subscriptionUrl) {

        Map<String, String> query = new HashMap<>();
        query.put("quickadd", subscriptionUrl);

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .addNewSubscription(query)
                .filter(inoReaderSubscriptionItems -> inoReaderSubscriptionItems != null)
                .subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<InoReaderSubscriptionItems>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(InoReaderSubscriptionItems inoReaderSubscriptionItems) {
                        Log.d(TAG, "onNext: \t"
                                + inoReaderSubscriptionItems.getStreamName()
                                + "\tID\t" + inoReaderSubscriptionItems.getStreamId());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    /**
     * Edit the subscription
     *
     * @param commandName
     * @param url
     * @param title
     * @param folderName
     * @param removeFolder
     */
    public void editInoSubscription(String commandName, String url, String title, String folderName, String removeFolder) {

        Map<String, String> query = new HashMap<>();
        query.put("ac", commandName);
        query.put("s", url);
        query.put("t", title);
        query.put("a", folderName);
        query.put("r", removeFolder);

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .editInoSubscription(query)
                .subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            if (responseBody.equals("OK")) {

                                Log.d(TAG, "editInoSubscription: " + responseBody.string());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError_editInoSubscription\t" + throwable.getMessage());
                    }
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }
    /**
     * Collect Ino Reader Tag list
     */
    public void getTagList() {

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .getTagList()
                .filter(tagsBeans -> tagsBeans != null)
                .subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<InoFoldersTagsList>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(InoFoldersTagsList tagsBeans) {
                        for (InoFoldersTagsList.TagsBean tagsList : tagsBeans.getTags()) {
                            Log.d(TAG, "TagIds\n: " + tagsList.getId());

                        }

                       /* List<TagEntity> tagEntity = addInoReaderTags(tagsBeans.getTags());// getting the tag entities
                        rssDatabase.dao().addTags(tagEntity);// adding the Tag into tags table*/

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: " + throwable);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    /**
     * This method is for fetching  the unread counters
     * for folders, tags and feeds all the stuff that come with it .
     */
    public void getInoUnreadCount(){
        mContext.sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_ITEMS));
        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .getInoUnreadCount()
                .filter(unreadcountsBeans -> unreadcountsBeans != null)
                .subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<InoUnreadCount>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(InoUnreadCount unreadcountsBeans) {
                        // int itemCountToSync = readablyPrefs.unreadItemsToKeep;
                        for (InoUnreadCount.UnreadcountsBean unreadcounts : unreadcountsBeans.getUnreadcounts()) {
                            if (unreadcounts.getId().startsWith("feed")) {
                                //int subUnreadCount = rssDatabase.dao()().getUnreadCountForSubscription(replaceSlashWithDash(unreadcounts.getId()));
                                Log.d(TAG, "Unread_ids\t" + unreadcounts.getId() + "UnReadCount: \t" + unreadcounts.getCount());
                                getAllFeedItems(ARTICLE_SYNC_COUNT, unreadcounts.getId());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    /**
     * Collect all unread the Feed Items
     * @param count
     */
    public void getAllFeedItems(int count, String streamId) {
        mContext.sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_ITEMS));

        Map<String, String> query = new HashMap<>();
        query.put("xt", "user/-/state/com.google/read");
        query.put("n", String.valueOf(count));

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .getStreamContent(streamId, query)
                .filter(inoStreamContentList -> inoStreamContentList.getItems().size() > 0 || inoStreamContentList != null)
                /*.subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())*/
                .subscribe(new Observer<InoStreamContentList>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }
                    @Override
                    public void onNext(InoStreamContentList itemsBeans) {
                        Log.d(TAG, "onNext: allItemsCount\t" + itemsBeans.getItems().size());
                        rssDatabase.dao().insertFeedItems(addAllInoReaderEntries(itemsBeans));
                        if (itemsBeans.getContinuation() != null) {
                            getMoreArticles(itemsBeans.getContinuation());// will fetch more articles till the continuation is null.
                        }
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: \t" + throwable.getMessage());
                    }
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    /**
     * Will get more articles till the Continuation value is Null.
     *
     * @param continuation
     */
    private void getMoreArticles(String continuation) {
        mContext.sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_ITEMS));
        Map<String, String> queryMap = new HashMap<>();
        if (continuation != null) {
            queryMap.put("xt", "user/-/state/com.google/read");
            queryMap.put("c", continuation);
        }
        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .getStreamContent(queryMap)
                .filter(inoStreamContentList -> inoStreamContentList.getItems().size() > 0 || inoStreamContentList != null)
                /*.subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())*/
                .subscribe(new Observer<InoStreamContentList>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable != null) return;
                    }

                    @Override
                    public void onNext(InoStreamContentList inoStreamContentList) {
                        Log.d(TAG, "continuation:\t " + continuation);
                        rssDatabase.dao().insertFeedItems(addAllInoReaderEntries(inoStreamContentList));

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });

    }

    /**
     * Collect all the starred items from inoReader Server
     */
    public void getStarred() {
        Map<String, String> query = new HashMap<>();
        query.put("it", InoReaderUrls.getQueryStarredArticles());
        final boolean[] lastSuccessfulSync = {false};
        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .getStarred(query)
                .filter(itemsBeans -> itemsBeans != null)
                /*.subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())*/
                .subscribe(new Observer<InoStreamContentList>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(InoStreamContentList itemsBeans) {
                        lastSuccessfulSync[0] = true;
                        rssDatabase.dao().insertFeedItems(addStarredInoreaderEntries(itemsBeans));
                        if (lastSuccessfulSync[0]) {
                            internalStatePrefs.setLongPref(InternalStatePrefs.LAST_SYNC_TIME_PREF_KEY, System.currentTimeMillis());
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError getting Starred: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    /**
     *
     * @param feedItemEntities
     */
    private void syncUnstarredItems(FeedItemEntity[] feedItemEntities) {
        Map<String, String> queryMap = new HashMap<>();
        for (FeedItemEntity favItem : feedItemEntities) {
            if (!favItem.favorite) {
                queryMap.put("r", "user/-/state/com.google/starred");

            }
            queryMap.put("i", favItem.id);
            InoReaderRetrofitClient.getRetrofit()
                    .create(InoReaderAPI.class)
                    .editFeedItem(queryMap)
                    /*.subscribeOn(new ComputationScheduler())
                    .observeOn(new ComputationScheduler())*/
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onSubscribe(Disposable disposable) {
                            if (disposable.isDisposed()) return;
                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            if (responseBody != null) {
                                try {
                                    String result = responseBody.string();
                                    Log.d(TAG, "ItemsFav: " + result);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                            Log.d(TAG, "onError: " + throwable.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "ItemsAreMarkedAsRead \t onComplete: ");
                        }
                    });
        }
    }

    private void syncStarredItems(FeedItemEntity[] feedItemEntities) {
        Map<String, String> queryMap = new HashMap<>();
        for (FeedItemEntity favItem : feedItemEntities) {
            if (favItem.favorite) {
                queryMap.put("a", "user/-/state/com.google/starred");

            }
            queryMap.put("i", favItem.id);
            InoReaderRetrofitClient.getRetrofit()
                    .create(InoReaderAPI.class)
                    .editFeedItem(queryMap)
                    .subscribeOn(new ComputationScheduler())
                    .observeOn(new ComputationScheduler())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onSubscribe(Disposable disposable) {
                            if (disposable.isDisposed()) return;
                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            if (responseBody != null) {
                                try {
                                    String result = responseBody.string();
                                    Log.d(TAG, "ItemsFav: " + result);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                            Log.d(TAG, "onError: " + throwable.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "ItemsAreMarkedAsRead \t onComplete: ");
                        }
                    });
        }
    }


    /**
     * Will mark items from room Db as read at once by checking if the FeedItem is read or not.
     */
    private void markFeedItemsAsRead(FeedItemEntity[] feedItemEntities) {
        mContext.sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_ITEMS));
        Map<String, String> queryMap = new HashMap<>();
        for (FeedItemEntity readItems : feedItemEntities) {
            if (readItems.read) {
                queryMap.put("a", "user/-/state/com.google/read");
            }
            queryMap.put("i", readItems.id);
            InoReaderRetrofitClient.getRetrofit()
                    .create(InoReaderAPI.class)
                    .editFeedItem(queryMap)
                    /*.subscribeOn(new ComputationScheduler())
                    .observeOn(new ComputationScheduler())*/
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onSubscribe(Disposable disposable) {
                            if (disposable.isDisposed()) return;
                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            if (responseBody != null) {
                                try {
                                    String result = responseBody.string();
                                    Log.d(TAG, "itemRead: " + result);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            throwable.printStackTrace();
                            Log.d(TAG, "onError: " + throwable.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "ItemsAreMarkedAsRead \t onComplete: ");
                        }
                    });
        }
    }

    /**
     * will Mark the Given feedItems As Unread
     */
    private void markFeedItemsAsUnread(FeedItemEntity[] feedItemEntities) {
        mContext.sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_ITEMS));
        Map<String, String> queryMap = new HashMap<>();
        if (feedItemEntities != null) {
            for (FeedItemEntity itemEntity : feedItemEntities) {
                if (!itemEntity.read) {
                    queryMap.put("r", "user/-/state/com.google/read");
                }
                queryMap.put("i", itemEntity.id);
                InoReaderRetrofitClient.getRetrofit()
                        .create(InoReaderAPI.class)
                        .editFeedItem(queryMap)
                       /* .subscribeOn(new ComputationScheduler())
                        .observeOn(new ComputationScheduler())*/
                        .subscribe(new Observer<ResponseBody>() {
                            @Override
                            public void onSubscribe(Disposable disposable) {
                                if (disposable.isDisposed()) return;
                            }

                            @Override
                            public void onNext(ResponseBody responseBody) {
                                if (responseBody != null) {
                                    try {
                                        String result = responseBody.string();
                                        Log.d(TAG, "itemUnRead: " + result);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                throwable.printStackTrace();
                                Log.d(TAG, "onError: " + throwable.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                Log.d(TAG, "ItemsAreMarkedAsUnRead \t onComplete: ");
                            }
                        });
            }
        }
    }
    /**
     * Update InoReader immediately if the user is online if not it will fallback and next time
     * user is online the ba will take care of it h
     *
     * @param feedItemEntity
     * @param state
     */
    public void updateFeedItemReadStatus(FeedItemEntity feedItemEntity, boolean state) {
        HashMap<String, String> queryMap = new HashMap<>();
        if (state) {
            queryMap.put("a", "user/-/state/com.google/read");
        } else {
            queryMap.put("r", "user/-/state/com.google/read");
        }
        queryMap.put("i", feedItemEntity.id);
        Log.d(TAG, "updateFeedItemReadStatus:\t" + queryMap);

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .editFeedItem(queryMap)
                .subscribeOn(Schedulers.io())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        if (responseBody != null) {
                            try {
                                String result = responseBody.string();
                                Log.d(TAG, "Updated FeedItem read state: " + result);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "ItemStarred \t onComplete: ");
                    }
                });
    }
    /**
     * Rename the Tag
     * @param source
     * @param destination
     */
    public void renameTag(String source, String destination) {
        Map<String, String> query = new HashMap<>();
        query.put("s", source);
        query.put("dest", "user/-/label/" + destination);

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .renameTag(query)
                .subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        Log.d(TAG, "onNext: \t" + aBoolean);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    /**
     * Delete an existing Tag from the ino Server
     *
     * @param source
     */
    public void deleteTag(String source) {

        Map<String, String> query = new HashMap<>();
        query.put("s", source);

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .deleteTag(query)
                .subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        Log.d(TAG, "onNext: \t" + aBoolean);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }


    /**
     * Adding the InoReader List of Subscriptions to Room Table by matching the column and rows to fit the table at Room
     *
     * @param inoSubscriptionLists
     * @return
     */
    private List<SubscriptionEntity> AddInoReaderSubscriptionToRoom(List<InoSubscriptionList.SubscriptionsBean> inoSubscriptionLists) {
        // init the entities
        List<SubscriptionEntity> subscriptionEntities = new ArrayList<>();
        //InoSubscriptionList.SubscriptionsBean subscription:
        for (InoSubscriptionList.SubscriptionsBean inoSubscriptionListItems : inoSubscriptionLists) {
            // Log.d(TAG, "AddInoReaderSubscriptionToRoom: "+inoSubscriptionListItems.getSubId());
            SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
            subscriptionEntity.id = inoSubscriptionListItems.getSubId();
            subscriptionEntity.title = inoSubscriptionListItems.getTitle();
            subscriptionEntity.siteLink = inoSubscriptionListItems.getHtmlUrl();
            subscriptionEntity.rssLink = inoSubscriptionListItems.getUrl();
            subscriptionEntity.createdTimestamp = inoSubscriptionListItems.getFirstitemmsec();
            subscriptionEntities.add(subscriptionEntity);// adding the final result to the room subscription table
        }
        return subscriptionEntities;
    }

    /**
     * @param subscriptionsBean
     * @return
     */
    private List<String> getSyncedSubscriptionIds(List<InoSubscriptionList.SubscriptionsBean> subscriptionsBean) {
        List<String> syncedSubscriptionIds = new ArrayList<>();
        for (InoSubscriptionList.SubscriptionsBean subscriptionList : subscriptionsBean) {
            syncedSubscriptionIds.add(subscriptionList.getSubId());
        }
        return syncedSubscriptionIds;
    }

    /**
     * get synced tag ids
     *
     * @param tagEntities
     * @return
     */
    private List<String> getSyncedTagIds(List<TagEntity> tagEntities) {
        List<String> syncedTagIds = new ArrayList<>();
        for (TagEntity tagEntity : tagEntities) {
            syncedTagIds.add(tagEntity.serverId);
        }
        return syncedTagIds;
    }

    /**
     * @param subscriptionsBeans
     * @return
     */
    private List<TagEntity> addInoReaderTags(List<InoSubscriptionList.SubscriptionsBean> subscriptionsBeans) {
        List<TagEntity> tagEntities = new ArrayList<>();
        for (InoSubscriptionList.SubscriptionsBean tags : subscriptionsBeans) {
            for (InoSubscriptionList.SubscriptionsBean.CategoriesBean categoriesBean : tags.getCategories()) {
                TagEntity tagEntity = new TagEntity();
                tagEntity.subscriptionId = tags.getSubId();
                tagEntity.name = categoriesBean.getLabel();
                tagEntity.serverId = categoriesBean.getId();
                tagEntities.add(tagEntity);
            }
        }
        return tagEntities;
    }

    /**
     * @param itemsBeans
     * @param subscriptionId
     * @return
     */
    private List<FeedItemEntity> addInoReaderEntries(List<InoStreamContentList.ItemsBean> itemsBeans, String subscriptionId) {

        List<FeedItemEntity> feedItemEntities = new ArrayList<>();
        FeedParser feedParser = FeedParser.getInstance(mContext);

        for (InoStreamContentList.ItemsBean inoFeedItems : itemsBeans) {
            FeedItemEntity existing = rssDatabase.dao().getFeedItem(getIdLastPath(inoFeedItems.getId()));
            SubscriptionEntity subscriptionEntity = rssDatabase.dao().getSubscription(subscriptionId);

            for (InoStreamContentList.ItemsBean.AlternateBean alternateBean : inoFeedItems.getAlternate()) {
                if (existing == null && subscriptionEntity != null) {
                    Log.d(TAG, "addInoReaderEntries: " + subscriptionId);
                    FeedItemEntity feedItemEntity = new FeedItemEntity(
                            inoFeedItems.getTitle(),
                            replaceSlashWithDash(subscriptionId),
                            getIdLastPath(inoFeedItems.getId()),
                            setInoReaderProperDateTime(inoFeedItems.getPublished()),
                            inoFeedItems.getSummary().getContent(),
                            null,//this the excerpt cuz inoreader does not support excerpt at the moment.
                            alternateBean.getHref(),
                            subscriptionEntity.title
                    );
                    feedItemEntity.createdAt = Long.parseLong(inoFeedItems.getCrawlTimeMsec());
                    feedItemEntity.author = inoFeedItems.getAuthor();
                    feedItemEntity.syncedAt = System.currentTimeMillis();
                    feedItemEntity = feedParser.parseFeedItem(feedItemEntity);
                    feedItemEntities.add(feedItemEntity);
                } else if (existing != null) {
                    rssDatabase.dao().updateFeedItem(existing);
                }
            }
        }
        return feedItemEntities;
    }

    /**
     * @param itemsBeans
     * @return
     */
    private List<FeedItemEntity> addAllInoReaderEntries(InoStreamContentList itemsBeans) {

        List<FeedItemEntity> feedItemEntities = new ArrayList<>();
        FeedParser feedParser = FeedParser.getInstance(mContext);

        for (InoStreamContentList.ItemsBean inoFeedItems : itemsBeans.getItems()) {

            FeedItemEntity existing = rssDatabase.dao().getFeedItem(getIdLastPath(inoFeedItems.getId()));
            SubscriptionEntity subscriptionEntity = rssDatabase.dao()
                    .getSubscription(replaceSlashWithDash(inoFeedItems.getOrigin().getStreamId()));

            for (InoStreamContentList.ItemsBean.AlternateBean alternateBean : inoFeedItems.getAlternate()) {
                if (existing == null && subscriptionEntity != null) {
                    Log.d(TAG, "StreamId: \t" + inoFeedItems.getOrigin().getStreamId());
                    FeedItemEntity feedItemEntity = new FeedItemEntity(
                            inoFeedItems.getTitle(),
                            replaceSlashWithDash(inoFeedItems.getOrigin().getStreamId()),
                            getIdLastPath(inoFeedItems.getId()),
                            setInoReaderProperDateTime(inoFeedItems.getPublished()),
                            inoFeedItems.getSummary().getContent(),
                            null,//this the excerpt cuz inoreader does not support excerpt at the moment.
                            alternateBean.getHref(),
                            subscriptionEntity.title
                    );
                    feedItemEntity.createdAt = Long.parseLong(inoFeedItems.getCrawlTimeMsec());
                    feedItemEntity.author = inoFeedItems.getAuthor();
                    feedItemEntity.syncedAt = System.currentTimeMillis();
                    feedItemEntity = feedParser.parseFeedItem(feedItemEntity);
                    feedItemEntities.add(feedItemEntity);
                } else if (existing != null) {
                    rssDatabase.dao().updateFeedItem(existing);
                }
            }
        }
        return feedItemEntities;
    }


    /**
     * Adding the starred items to the database
     *
     * @param itemsBeans
     * @return
     */
    private List<FeedItemEntity> addStarredInoreaderEntries(InoStreamContentList itemsBeans) {

        List<FeedItemEntity> feedItemEntities = new ArrayList<>();
        FeedParser feedParser = FeedParser.getInstance(mContext);

        for (InoStreamContentList.ItemsBean inoFeedItems : itemsBeans.getItems()) {

            FeedItemEntity existing = rssDatabase.dao().getFeedItem(getIdLastPath(inoFeedItems.getId()));

            if (existing != null) {
                existing.favorite = true;
                rssDatabase.dao().updateFeedItem(existing);

            } else {

                for (InoStreamContentList.ItemsBean.AlternateBean alternateBean : inoFeedItems.getAlternate()) {
                    FeedItemEntity feedItemEntity = new FeedItemEntity(
                            inoFeedItems.getTitle(),
                            replaceSlashWithDash(inoFeedItems.getOrigin().getStreamId()),
                            getIdLastPath(inoFeedItems.getId()),
                            setInoReaderProperDateTime(inoFeedItems.getPublished()),
                            inoFeedItems.getSummary().getContent(),
                            null,
                            alternateBean.getHref(),
                            inoFeedItems.getOrigin().getTitle()
                    );
                    feedItemEntity.createdAt = Long.parseLong(inoFeedItems.getCrawlTimeMsec());
                    feedItemEntity.author = inoFeedItems.getAuthor();
                    feedItemEntity.syncedAt = System.currentTimeMillis();
                    feedItemEntity.modifiedAt = 0;
                    feedItemEntity.favorite = true;
                    feedItemEntity.read = false;
                    feedItemEntity = feedParser.parseFeedItem(feedItemEntity);
                    feedItemEntities.add(feedItemEntity);
                }
            }
        }
        return feedItemEntities;
    }

    /**
     * This method marks all items in a given stream as read. Please provide the ts parameter - unix timestamp,
     * generated the last time the list stream was fetched and displayed to the user,
     * so it won't mark as read items that the user never got.
     * @param url
     * @param lastSyncTime
     */
    public void markAllAsRead(String url, long lastSyncTime) {
        markAllAsReadInRoom(url);// will mark the items in that subscription id as Read
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("ts", Long.toString(lastSyncTime));
        queryMap.put("s", url);

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .markAllAsRead(queryMap)
                .subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        if (responseBody != null) {
                            try {
                                Log.d(TAG, "markAllAsRead_responseBody: " + responseBody.string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: From MarkAllRead");
                    }
                });
    }
    /**
     * will the feed items as Read
     *
     * @param subscriptionId
     */
    private void markAllAsReadInRoom(String subscriptionId) {
        //mark the FeedItems as read
        FeedItemEntity[] feedItemEntity = rssDatabase.dao().getAllFeedItemsForSubscription(replaceSlashWithDash(subscriptionId));
        for (FeedItemEntity entity : feedItemEntity) {
            if (entity != null) {
                entity.read = true;
                rssDatabase.dao().updateFeedItem(entity);
            }
        }
    }

    /**
     * Will give us the proper InoReader DateTime Conversion.
     *
     * @param time
     * @return
     */
    private long setInoReaderProperDateTime(long time) {
        return (Long.valueOf(time).longValue()) * 1000;
    }

    /**
     * will get the Last part of the id
     *
     * @param id
     * @return
     */
    private String getIdLastPath(String id) {
        String[] parts = id.split("/");
        return parts[parts.length - 1];
    }

    /**
     * @param subsId
     * @return
     */
    private String replaceSlashWithDash(String subsId) {
        return subsId.replace("/", "_").trim();
    }

    /**
     * @param subsId
     * @return
     */
    private String replaceDashWithSlash(String subsId) {
        return subsId.replace("_", "/").trim();
    }

    /**
     * @param lastId
     * @return
     */
    private String appendFeedIdProperId(String lastId) {
        return "tag:google.com,2005:reader/item/" + lastId.trim();
    }


    /**
     * we can Unsubscribe from the feed Subscription
     *
     * @param feedId
     */
    public void unsubscribe(String feedId) {
        editInoSubscription("unsubscribe", feedId, "", "", "");
    }

    /**
     * @param title
     * @param feedId
     * @param categories
     */
    public void subscribeFeed(String title, String feedId, String categories) {
        editInoSubscription("subscribe", feedId, title, categories, "");
    }

    /**
     * @param title
     * @param feedId
     * @param addCategories
     * @param removeCategories
     */
    public void editFeed(String title, String feedId, String addCategories, String removeCategories) {
        editInoSubscription("edit", title, feedId, addCategories, removeCategories);
    }

    /**
     * Will compare Number of Feed Items to Pull per subscription
     *
     * @param subscriptionsCount
     * @return
     */
    private int computeNumberOfFeedItemsToSync(int subscriptionsCount) {
        Log.d(TAG, "computeNumberOfFeedItemsToSync:\t " + subscriptionsCount);
        int count = 0;
        if (subscriptionsCount > 0) {
            count = subscriptionsCount * readablyPrefs.unreadItemsToKeep;
            if (count >= MAX_LIMIT) {
                count = MAX_LIMIT;
            }
        }
        return count;
    }

}