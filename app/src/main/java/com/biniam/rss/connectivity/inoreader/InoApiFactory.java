package com.biniam.rss.connectivity.inoreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;

import com.biniam.rss.BuildConfig;
import com.biniam.rss.connectivity.feedly.FeedlyConstants;
import com.biniam.rss.connectivity.inoreader.inoReaderApi.HeaderInterceptor;
import com.biniam.rss.connectivity.inoreader.inoReaderApi.InoReaderAPI;
import com.biniam.rss.models.feedly.FeedlyUserProfile;
import com.biniam.rss.models.inoreader.InoReaderSubscriptionItems;
import com.biniam.rss.models.inoreader.InoReaderUserInfo;
import com.biniam.rss.models.inoreader.InoStreamContentList;
import com.biniam.rss.models.inoreader.InoSubscriptionList;
import com.biniam.rss.models.inoreader.InoUnreadCount;
import com.biniam.rss.persistence.db.PaperDatabase;
import com.biniam.rss.persistence.db.roomentities.FeedItemEntity;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.persistence.db.roomentities.TagEntity;
import com.biniam.rss.persistence.preferences.InoReaderAccountPreferences;
import com.biniam.rss.persistence.preferences.InternalStatePrefs;
import com.biniam.rss.persistence.preferences.PaperPrefs;
import com.biniam.rss.ui.controllers.FeedParser;
import com.biniam.rss.utils.AccountBroker;
import com.biniam.rss.utils.AutoImageCache;
import com.biniam.rss.utils.ConnectivityState;
import com.biniam.rss.utils.PaperApp;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.schedulers.ComputationScheduler;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;

/**
 * Created by biniam on 2/17/18.
 */


public class InoApiFactory {

    public static final String TAG = InoApiFactory.class.getSimpleName();
    private static InoApiFactory inoApiFactory;
    private Context mContext;
    private long syncStartTime;
    private PaperDatabase rssDatabase;
    FeedParser feedParser;
    private PaperPrefs paperPrefs;
    private AutoImageCache autoImageCache;
    private InternalStatePrefs internalStatePrefs;
    private AccountBroker accountBroker;
    private InoReaderAccountPreferences inoReaderAccountPreferences;
    private Map<String, String> subscriptionWithCount = new ArrayMap<>();
    List<String> idsFromIno = new ArrayList<>();
    //OkHttpClient  with interceptor
    private static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new HeaderInterceptor(PaperApp.getInstance()))
            .build();
    private static final String API_BASE_URL = "https://www.inoreader.com/reader/api/0";
    private static String markFeedReadURL = API_BASE_URL + "/edit-tag?a=user/-/state/com.google/read";
    private static String markFeedUnReadURL = API_BASE_URL + "/edit-tag?r=user/-/state/com.google/read";

    private static String markFeedStarredURL = API_BASE_URL + "/edit-tag?a=user/-/state/com.google/starred";
    private static String markFeedUnStarredURL = API_BASE_URL + "/edit-tag?r=user/-/state/com.google/starred";

    /**
     * @param context
     */
    private InoApiFactory(Context context) {
        mContext = context;
        rssDatabase = PaperApp.getInstance().getDatabase();
        feedParser = FeedParser.getInstance(context);
        paperPrefs = PaperPrefs.getInstance(context);
        autoImageCache = AutoImageCache.getInstance(context);
        internalStatePrefs = InternalStatePrefs.getInstance(context);
        accountBroker = AccountBroker.getInstance(context);
        inoReaderAccountPreferences = InoReaderAccountPreferences.getInstance(context);

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
//                        Log.d(TAG, "onNext: \t User email\t"
//                                + inoReaderUserInfo.getUserEmail()
//                                + "\tuserName\t" + inoReaderUserInfo.getUserName() +
//                                "\tUserProfileId\t" + inoReaderUserInfo.getUserProfileId()
//
//                        );
                        getSubscriptionList();
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
        syncStartTime = System.currentTimeMillis();// calculating the current Time for the display
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

                        //sync ino feed items
                        getInoUnreadCount();
                        //..Preparing the subscription data as SubscriptionEntity Format...
                        List<SubscriptionEntity> convertingSubscription = convertingToSubscriptionEntity(inoSubscriptionList.getSubscriptions());
                        // collecting the subscription Ids.
                        List<String> inoreaderFetchedSubscriptionIds = getSyncedSubscriptionIds(inoSubscriptionList.getSubscriptions());

                        rssDatabase.dao().insertSubscriptions(convertingSubscription);// inserting subscription entities to the Subscription Table

                        List<String> subscriptionIds = rssDatabase.dao().getAllSubscriptionIds();

                        for (String inoReaderSavedSubscriptionIds : subscriptionIds) {
                            if (!inoreaderFetchedSubscriptionIds.contains(inoReaderSavedSubscriptionIds)) {
                                if (accountBroker.isCurrentAccountInoreader()) {
                                    rssDatabase.dao().deleteSubscription(rssDatabase.dao().getSubscription(inoReaderSavedSubscriptionIds));
                                }
                            }
                            // there was a tag here
                        }
                        //Updating the Subscription name
                        for (SubscriptionEntity subscriptionEntity22 : rssDatabase.dao().getAllSubscriptions()) {
                            for (SubscriptionEntity subscriptionEntity4 : convertingSubscription) {
                                if (subscriptionEntity22.id.equals(subscriptionEntity4.id) && !subscriptionEntity22.title.equals(subscriptionEntity4.title)) {
                                    Log.d("Updating_subscription", String.format("Updating_subscription: subscription %s has been renamed to %s ",
                                            new Object[]{subscriptionEntity22.title, subscriptionEntity4.title}));
                                    subscriptionEntity22.title = subscriptionEntity4.title;
                                    rssDatabase.dao().updateSubscription(subscriptionEntity22);
                                }
                            }
                        }
                        // getting the tag entities
                        List<TagEntity> tagEntityFromInoReader = convertingToTagEntity(inoSubscriptionList.getSubscriptions());
                        rssDatabase.dao().addTags(tagEntityFromInoReader);// adding the Tag into tags table
                        // Collect  all the server ids
                        List<String> tagList = new ArrayList<>();
                        for (TagEntity tag : tagEntityFromInoReader) {
                            tagList.add(tag.serverId);
                        }
                        // Check if the tags in Paper tags table, are equal with the one that are coming from the server
                        for (TagEntity tagsFromPaper : rssDatabase.dao().getAllTags()) {
                            for (TagEntity tagsFrmIno : tagEntityFromInoReader) {
                                if (tagsFromPaper.serverId.equals(tagsFrmIno.serverId) && tagsFromPaper.subscriptionId.equals(tagsFrmIno.subscriptionId) && !tagsFromPaper.name.equals(tagsFrmIno.name)) {
                                    Log.e("TAGS_Compare",
                                            String.format("tags_Update: tag %s has been renamed to %s updating...",
                                                    new Object[]{tagsFromPaper.name, tagsFrmIno.name}));
                                    tagsFromPaper.name = tagsFrmIno.name;
                                    rssDatabase.dao().updateTag(tagsFromPaper);
                                }
                            }
                        }
                        // Checking for removed tags
                        for (String tagsById : rssDatabase.dao().getAllTagsServerIds()) {
                            if (!tagList.contains(tagsById)) {
                                Log.w("deleting_tags",
                                        String.format("Deleting_Tags: tag %s removed, deleting...", new Object[]{rssDatabase.dao().getTagByServerId(tagsById).name}));
                                rssDatabase.dao().deletTagByServerId(tagsById);
                            }
                        }

                        FeedItemEntity[] feedItemEntities = rssDatabase.dao().getModifiedFeedEntitiesSinceLastSync(true);
                        Log.d(TAG, String.format("CountReadFeeds: %d", feedItemEntities.length));
                        //  SyncReadFromPaper(rssDatabase.dao().getModifiedFeedEntitiesSinceLastSync(true));
                        //markFeedItemsAsReadOnInoReader(rssDatabase.dao().getModifiedFeedEntitiesSinceLastSync(true));
                        //markFeedItemsAsRead(rssDatabase.dao().getModifiedFeedEntitiesSinceLastSync(true));//will Mark items As read

                        //sync unread feed items to ino server
                        // markFeedItemsAsUnreadOnInoReader(rssDatabase.dao().getModifiedFeedEntitiesSinceLastSync(false));//will Mark items as Unread

                        //trying to get the feed content by feed id
                        if (subscriptionWithCount.size() > 0 && subscriptionWithCount != null) {
                            for (Map.Entry<String, String> entry : subscriptionWithCount.entrySet()) {
                                getInoReaderFeedsPerSubscription(entry.getKey(), entry.getValue());
                            }
                        }
                        // marking the  feed as read if there is an id difference in inoServer and by database
                        // markFeedItemsASReadOnPaper();
                        //sync starred feed items to ino server
                        // syncStarredItems(rssDatabase.dao().getFavedFeedItemEntitySinceLastSync(true));
                        //sync unStarred to ino server
                        //syncUnStarredItems(rssDatabase.dao().getFavedFeedItemEntitySinceLastSync(false));
                        //get all the starred Items
                        getStarred();
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
     * marking the feedItems as read on paper
     */
    private void markFeedItemsASReadOnPaper() {
        List<String> feedIdsFromPaper = getAllFeedIds();
        if (feedIdsFromPaper.size() > 0) {
            for (String feedIds : feedIdsFromPaper) {
                if (!idsFromIno.contains(feedIds)) {
                    FeedItemEntity feedItemEntity = rssDatabase.dao().getFeedItem(feedIds);
                    feedItemEntity.read = true;
                    rssDatabase.dao().updateFeedItem(feedItemEntity);
                }
            }
        }
        idsFromIno.clear();
    }

    /**
     * Trying to add Feed Items using Subscription Id for each of them
     *
     * @param streamId
     */
    public void getInoReaderFeedsPerSubscription(final String streamId, String count) {
        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("xt", "user/-/state/com.google/read");
        queryMap.put("n", count);

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .getStreamContent(streamId.replace("_", "/").trim(), queryMap)
                .filter(inoStreamContentList -> {
                    Log.d(TAG, String.format("InoReaderFeedsPerSubscription:  %s", inoStreamContentList.getContinuation()));
                    return inoStreamContentList.getItems().size() > 0 || inoStreamContentList != null;
                })
                .subscribe(new Observer<InoStreamContentList>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(InoStreamContentList inoStreamContentList) {
                        List<FeedItemEntity> unreadFeedItems = convertingToFeedEntity(inoStreamContentList);
                        rssDatabase.dao().insertFeedItems(unreadFeedItems);
                        if (inoStreamContentList.getContinuation() != null) {
                            getMoreArticles(inoStreamContentList.getContinuation());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.e(TAG, String.format("Error from  feed per subscription %s", e.getMessage()));
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }


    /**
     * @return
     */
    public List<String> getAllFeedIds() {
        List<String> ids = rssDatabase.dao().getAllFeedItemsIds();
        if (ids != null && ids.size() > 0) return ids;
        return null;
    }


    /**
     * will Cache images
     */
    private void autoImageCache() {
        if (!paperPrefs.autoCacheImages) {
            return;
        } else if (paperPrefs.automaticSyncWiFiOnly && ConnectivityState.isOnWiFi()) {
            autoImageCache.startCaching(syncStartTime);
        } else if (paperPrefs.autoCacheImages && !paperPrefs.automaticSyncWiFiOnly &&
                ConnectivityState.hasDataConnection()) {
            autoImageCache.startCaching(syncStartTime);
        }
    }


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

//    /**
//     * Edit the subscription
//     *
//     * @param commandName
//     * @param url
//     * @param title
//     * @param folderName
//     * @param removeFolder
//     */
//    public void editInoSubscription(String commandName, String url, String title, String folderName, String removeFolder) {
//
//        Map<String, String> query = new HashMap<>();
//        query.put("ac", commandName);
//        query.put("s", url);
//        query.put("t", title);
//        query.put("a", folderName);
//        query.put("r", removeFolder);
//
//        InoReaderRetrofitClient.getRetrofit()
//                .create(InoReaderAPI.class)
//                .editInoSubscription(query)
//                .subscribeOn(new ComputationScheduler())
//                .observeOn(new ComputationScheduler())
//                .subscribe(new Observer<ResponseBody>() {
//                    @Override
//                    public void onSubscribe(Disposable disposable) {
//                        if (disposable.isDisposed()) return;
//                    }
//
//                    @Override
//                    public void onNext(ResponseBody responseBody) {
//                        try {
//                            if (responseBody.equals("OK")) {
//
//                                Log.d(TAG, "editInoSubscription: " + responseBody.string());
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        throwable.printStackTrace();
//                        Log.d(TAG, "onError_editInoSubscription\t" + throwable.getMessage());
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "onComplete: ");
//                    }
//                });
//    }

//    /**
//     * Collect Ino Reader Tag list
//     */
//    public void getTagList() {
//
//        InoReaderRetrofitClient.getRetrofit()
//                .create(InoReaderAPI.class)
//                .getTagList()
//                .filter(tagsBeans -> tagsBeans != null)
//                .subscribeOn(new ComputationScheduler())
//                .observeOn(new ComputationScheduler())
//                .subscribe(new Observer<InoFoldersTagsList>() {
//                    @Override
//                    public void onSubscribe(Disposable disposable) {
//                        if (disposable.isDisposed()) return;
//                    }
//
//                    @Override
//                    public void onNext(InoFoldersTagsList tagsBeans) {
//                        for (InoFoldersTagsList.TagsBean tagsList : tagsBeans.getTags()) {
//                            Log.d(TAG, "TagIds\n: " + tagsList.getId());
//
//                        }
//
//                       /* List<TagEntity> tagEntity = convertingToTagEntity(tagsBeans.getTags());// getting the tag entities
//                        rssDatabase.dao().addTags(tagEntity);// adding the Tag into tags table*/
//
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        throwable.printStackTrace();
//                        Log.d(TAG, "onError: " + throwable);
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }

    /**
     * This method is for fetching  the unread counters
     * for folders, tags and feeds all the stuff that come with it .
     */
    public void getInoUnreadCount() {
        mContext.sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_ITEMS));
        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .getInoUnreadCount()
                .filter(unreadcountsBeans -> unreadcountsBeans != null)
                .subscribe(new Observer<InoUnreadCount>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(InoUnreadCount unreadcountsBeans) {

                        for (InoUnreadCount.UnreadcountsBean unreadcounts : unreadcountsBeans.getUnreadcounts()) {
                            if (unreadcounts.getId().startsWith("feed")) {
                                //  Log.d(TAG, String.format("UnreadCount: %s and Feed id which starts with feed %s", unreadcounts.getCount(), unreadcounts.getId()));
                                subscriptionWithCount.put(unreadcounts.getId(), String.valueOf(unreadcounts.getCount()));

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

//    /**
//     * Collect all the unread the Feed Items
//     *
//     * @param count
//     */
//    public void getAllFeedItems(int count, String streamId, String UserId) {
//        mContext.sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_ITEMS));
//
//        Map<String, String> query = new HashMap<>();
//        query.put("xt", "user/" + UserId + "/state/com.google/read");
//        query.put("n", String.valueOf(count));
//
//        InoReaderRetrofitClient.getRetrofit()
//                .create(InoReaderAPI.class)
//                .getStreamContent(streamId, query)
//                .filter(inoStreamContentList -> inoStreamContentList.getItems().size() > 0 || inoStreamContentList != null)
//                .subscribe(new Observer<InoStreamContentList>() {
//                    @Override
//                    public void onSubscribe(Disposable disposable) {
//                        if (disposable.isDisposed()) return;
//                    }
//
//                    @Override
//                    public void onNext(InoStreamContentList itemsBeans) {
//                        Log.d(TAG, "onNext: allItemsCount\t" + itemsBeans.getItems().size());
//                        rssDatabase.dao().insertFeedItems(convertingToFeedEntity(itemsBeans));
//                        if (itemsBeans.getContinuation() != null) {
//                            getMoreArticles(itemsBeans.getContinuation());
//                            // will fetch more articles till the continuation is null.
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        throwable.printStackTrace();
//                        Log.d(TAG, "onError: \t" + throwable.getMessage());
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "onComplete: ");
//                    }
//                });
//    }


    public void getMoreStarredItems(String continuation) {
        Map<String, String> query = new HashMap<>();
        if (continuation != null) {
            query.put("it", "user/-/state/com.google/starred");
            query.put("c", continuation);
            InoReaderRetrofitClient.getRetrofit()
                    .create(InoReaderAPI.class)
                    .getStarred(query)
                    .filter(new Predicate<InoStreamContentList>() {
                        @Override
                        public boolean test(InoStreamContentList itemsBeans) throws Exception {
                            return itemsBeans != null;
                        }
                    })
                    .subscribe(new Observer<InoStreamContentList>() {
                        @Override
                        public void onSubscribe(Disposable disposable) {
                            if (disposable.isDisposed()) return;
                        }

                        @Override
                        public void onNext(InoStreamContentList itemsBeans) {

                            if (itemsBeans.getContinuation() != null) {
                                rssDatabase.dao().insertFeedItems(convertingToFeedEntityForStarredItems(itemsBeans));
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
            InoReaderRetrofitClient.getRetrofit()
                    .create(InoReaderAPI.class)
                    .getStreamContent(queryMap)
                    .filter(new Predicate<InoStreamContentList>() {
                        @Override
                        public boolean test(InoStreamContentList inoStreamContentList) throws Exception {
                            return inoStreamContentList.getItems().size() > 0 || inoStreamContentList != null;
                        }
                    })
                    .subscribe(new Observer<InoStreamContentList>() {
                        @Override
                        public void onSubscribe(Disposable disposable) {
                            if (disposable != null) return;
                        }

                        @Override
                        public void onNext(InoStreamContentList inoStreamContentList) {
                            Log.d(TAG, "continuation:\t " + continuation);
                            // preparing the data for paper Db
                            if (inoStreamContentList != null) {
                                List<FeedItemEntity> unreadFeedItems = convertingToFeedEntity(inoStreamContentList);
                                rssDatabase.dao().insertFeedItems(unreadFeedItems);
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
    }

    /**
     * Collect all the starred items from inoReader Server
     */
    public void getStarred() {
        final boolean[] lastSuccessfulSync = {false};
        Map<String, String> query = new HashMap<>();
        query.put("it", "user/-/state/com.google/starred");
        query.put("c", "250");

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .getStarred(query)
                .filter(new Predicate<InoStreamContentList>() {
                    @Override
                    public boolean test(InoStreamContentList itemsBeans) throws Exception {
                        return itemsBeans != null;
                    }
                })
                .subscribe(new Observer<InoStreamContentList>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        if (disposable.isDisposed()) return;
                    }

                    @Override
                    public void onNext(InoStreamContentList itemsBeans) {
                        rssDatabase.dao().insertFeedItems(convertingToFeedEntityForStarredItems(itemsBeans));
                        if (itemsBeans.getContinuation() != null) {
                            getMoreStarredItems(itemsBeans.getContinuation());
                        }
                        lastSuccessfulSync[0] = true;
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


    public BufferedReader connectServerToInoReader(HttpUrl url) {
        Response response = null;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.d(TAG, String.format("connectToInoReaderServer: %s ", response.body().string()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().source().inputStream(), "utf-8"));
                return reader;
            } else {
                Log.d(TAG, String.format("connectServerToInoReader: Something went wrong %d ", response.code()));
            }
        } catch (IOException e) {
            Log.e(TAG, String.format("connectServerToInoReader_Error: %s ", e.getMessage()));
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Closing the Reader
     *
     * @param reader
     */
    public static void closeReader(BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, String.format("closeReader: ", e.getMessage()));
                // something bad happened here :(
            }
        }
    }

    /**
     * marking the Feed items as read on InoReader server as read
     *
     * @param feedItemEntities
     */
    public void markFeedItemsAsReadOnInoReader(FeedItemEntity[] feedItemEntities) {
        new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {

                Log.d(TAG, String.format("markFeedItemsAsReadOnInoReader: %d", feedItemEntities.length));
                StringBuilder stringBuilder = new StringBuilder();

                HttpUrl URL = HttpUrl.parse(markFeedReadURL);
                for (FeedItemEntity readItems : feedItemEntities) {
                    if (readItems.read) {
                        stringBuilder.append("&i=" + readItems.id);

                    }
                }
                Log.d(TAG, String.format("Collected_ids: %s", stringBuilder.toString()));
                BufferedReader reader = connectServerToInoReader(URL.newBuilder()
                        .addPathSegment(stringBuilder.toString().trim())
                        //.setQueryParameter("i", readItems.id)
                        .build());
                closeReader(reader);


            }
        }.subscribeActual(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
                if (d.isDisposed()) return;

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Log.e(TAG, String.format("Error marking read on InoReader server: %s ", e.getMessage()));
            }
        });

    }
//
//    /**
//     *
//     * @param feedItemEntities
//     */
//    private void markFeedItemsAsUnreadOnInoReader(FeedItemEntity[] feedItemEntities) {
//        Log.d(TAG, String.format("markFeedItemsAsUnreadOnInoReader: ", feedItemEntities.length));
//        if (feedItemEntities != null && feedItemEntities.length > 0) {
//            StringBuilder stringBuilder = new StringBuilder();
//        HttpUrl URL = HttpUrl.parse(markFeedUnReadURL);
//            for (FeedItemEntity readItems : feedItemEntities) {
//                if (!readItems.read) {
//                    stringBuilder.append("&i="+readItems.id);
//
//                }
//            }
//            Log.d(TAG, String.format("StringBuilderFormat:%s ", stringBuilder.toString()));
//            BufferedReader reader = connectServerToInoReader(URL.newBuilder()
//                    .addPathSegment(stringBuilder.toString().trim())
//                    .build());
//            closeReader(reader);
//        }
//    }
//
//    /**
//     * @param feedItemEntities
//     */
//    private void syncStarredItems(FeedItemEntity[] feedItemEntities) {
//        if (feedItemEntities != null && feedItemEntities.length > 0) {
//            StringBuilder stringBuilder = new StringBuilder();
//            HttpUrl URL = HttpUrl.parse(markFeedStarredURL);
//            for (FeedItemEntity starredItems : feedItemEntities) {
//                if (starredItems.favorite) {
//                    stringBuilder.append("&i="+starredItems.id);
////                    BufferedReader reader = connectServerToInoReader(URL.newBuilder()
////                            .setQueryParameter("i", starredItems.id)
////                            .build());
////                    closeReader(reader);
//                }
//            }
//
//            BufferedReader reader = connectServerToInoReader(URL.newBuilder()
//                    .addPathSegment(stringBuilder.toString().trim())
//                    .build());
//            closeReader(reader);
//        }
//    }
//
//    /**
//     *
//     * @param feedItemEntities
//     */
//    private void syncUnStarredItems(FeedItemEntity[] feedItemEntities) {
//        if (feedItemEntities != null && feedItemEntities.length > 0) {
//            StringBuilder stringBuilder = new StringBuilder();
//            HttpUrl URL = HttpUrl.parse(markFeedUnStarredURL);
//            for (FeedItemEntity starredItems : feedItemEntities) {
//                if (!starredItems.favorite) {
//                    stringBuilder.append("&i="+starredItems.id);
//
//                }
//            }
//            BufferedReader reader = connectServerToInoReader(URL.newBuilder()
//                    .addPathSegment(stringBuilder.toString().trim())
//                    .build());
//            closeReader(reader);
//        }
//    }
//

    /**
     * Rename the Tag
     *
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
    private List<SubscriptionEntity> convertingToSubscriptionEntity
    (List<InoSubscriptionList.SubscriptionsBean> inoSubscriptionLists) {
        // init the entities
        List<SubscriptionEntity> subscriptionEntities = new ArrayList<>();
        //InoSubscriptionList.SubscriptionsBean subscription:
        for (InoSubscriptionList.SubscriptionsBean inoSubscriptionListItems : inoSubscriptionLists) {
            Log.d(TAG, "convertingToSubscriptionEntity: " + inoSubscriptionListItems.getSubId());
            SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
            subscriptionEntity.id = inoSubscriptionListItems.getSubId();
            subscriptionEntity.title = inoSubscriptionListItems.getTitle();
            subscriptionEntity.siteLink = inoSubscriptionListItems.getHtmlUrl();
            StringBuilder stringBuilder = new StringBuilder("https://logo-core.clearbit.com/");// getting the logo from the clearbit logo genarator site
            stringBuilder.append(Uri.parse(subscriptionEntity.siteLink).getHost());
            stringBuilder.append("?size=128");// with the image size of 128px
            subscriptionEntity.iconUrl = stringBuilder.toString();
            subscriptionEntity.rssLink = inoSubscriptionListItems.getUrl();
            subscriptionEntity.createdTimestamp = inoSubscriptionListItems.getFirstitemmsec();
            subscriptionEntities.add(subscriptionEntity);// adding the final result to the room subscription Entities Format
        }
        return subscriptionEntities;
    }

    /**
     * @param subscriptionsBean
     * @return
     */
    private List<String> getSyncedSubscriptionIds
    (List<InoSubscriptionList.SubscriptionsBean> subscriptionsBean) {
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
    private List<TagEntity> convertingToTagEntity
    (List<InoSubscriptionList.SubscriptionsBean> subscriptionsBeans) {
        //init Tag entity
        List<TagEntity> tagEntities = new ArrayList<>();

        //Create a Tag for the subscription if that subscription contains a TAG
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
     * @return
     */
    private List<FeedItemEntity> convertingToFeedEntity(InoStreamContentList itemsBeans) {

        List<FeedItemEntity> feedItemEntities = new ArrayList<>();
        //init the feed Parser here
        for (InoStreamContentList.ItemsBean inoFeedItems : itemsBeans.getItems()) {
            idsFromIno.add(getIdLastPath(inoFeedItems.getId()));//ids
            FeedItemEntity savedFeedItemEntity = rssDatabase.dao().getFeedItem(getIdLastPath(inoFeedItems.getId()));
            SubscriptionEntity subscriptionEntity = rssDatabase.dao().getSubscription(replaceSlashWithDash(inoFeedItems.getOrigin().getStreamId()));

            if (savedFeedItemEntity == null) {
                FeedItemEntity feedItemEntity = new FeedItemEntity(
                        inoFeedItems.getTitle(),
                        replaceSlashWithDash(inoFeedItems.getOrigin().getStreamId()),
                        getIdLastPath(inoFeedItems.getId()),
                        setInoReaderProperDateTime(inoFeedItems.getPublished()),
                        inoFeedItems.getSummary().getContent(),
                        null,//this the excerpt cuz inoreader does not support excerpt at the moment.
                        inoFeedItems.getAlternate().iterator().next().getHref(),
                        subscriptionEntity.title);
                feedItemEntity.createdAt = Long.parseLong(inoFeedItems.getCrawlTimeMsec());
                feedItemEntity.author = inoFeedItems.getAuthor();
                feedItemEntity.syncedAt = System.currentTimeMillis();
                feedItemEntity = feedParser.parseFeedItem(feedItemEntity);
                feedItemEntities.add(feedItemEntity);
            } else{
                rssDatabase.dao().updateFeedItem(savedFeedItemEntity);
            }
        }

        return feedItemEntities;
    }

    /**
     * Dividing the Whole list of items into manageable list
     *
     * @param list
     * @return
     */
    private static List<List<String>> divideLongList(List<String> list) {
        List<String> container = list;
        List<List<String>> arrayList = new ArrayList();
        if (container.size() / 250 != 0 || container.size() <= 0) {
            int size = container.size() / 250;
            int start = 0;
            int end = 249;
            int index = 0;
            while (start < size) {
                arrayList.add(container.subList(index, end));
                if (start <= size - 1) {
                    int count = end;
                    end += 250;
                    index = count;
                }
                start++;
            }
            if (container.size() % 250 <= 0) {
                return arrayList;
            }
            container = container.subList(250 * size, container.size());
        }
        arrayList.add(container);
        return arrayList;
    }

    /**
     * Adding the starred items to the database
     *
     * @param itemsBeans
     * @return
     */
    private List<FeedItemEntity> convertingToFeedEntityForStarredItems(InoStreamContentList
                                                                               itemsBeans) {
        List<FeedItemEntity> feedItemEntities = new ArrayList<>();
        FeedParser feedParser = FeedParser.getInstance(mContext);
        for (InoStreamContentList.ItemsBean inoFeedItems : itemsBeans.getItems()) {
            FeedItemEntity existing = rssDatabase.dao().getFeedItem(getIdLastPath(inoFeedItems.getId()));
            if (existing == null) {
                FeedItemEntity feedItemEntity = new FeedItemEntity(
                        inoFeedItems.getTitle(),
                        replaceSlashWithDash(inoFeedItems.getOrigin().getStreamId()),
                        getIdLastPath(inoFeedItems.getId()),
                        setInoReaderProperDateTime(inoFeedItems.getPublished()),
                        inoFeedItems.getSummary().getContent(),
                        null,
                        inoFeedItems.getAlternate().iterator().next().getHref(),
                        inoFeedItems.getOrigin().getTitle()
                );
                feedItemEntity.createdAt = Long.parseLong(inoFeedItems.getCrawlTimeMsec());
                feedItemEntity.author = inoFeedItems.getAuthor();
                feedItemEntity.syncedAt = System.currentTimeMillis();
                feedItemEntity = feedParser.parseFeedItem(feedItemEntity);
                feedItemEntities.add(feedItemEntity);
            } else if (!existing.favorite) {
                existing.favorite = true;
                existing.starredUnStarredmodified = 0;
                rssDatabase.dao().updateFeedItem(existing);
            }
        }
        return feedItemEntities;
    }

//    /**
//     * This method marks all items in a given stream as read. Please provide the ts parameter - unix timestamp,
//     * generated the last time the list stream was fetched and displayed to the user,
//     * so it won't mark as read items that the user never got.
//     *
//     * @param url
//     * @param lastSyncTime
//     */
//    public void markAllAsRead(String url, long lastSyncTime) {
//        markAllAsReadInRoom(url);// will mark the items in that subscription id as Read
//        Map<String, String> queryMap = new HashMap<>();
//        queryMap.put("ts", Long.toString(lastSyncTime));
//        queryMap.put("s", url);
//
//        InoReaderRetrofitClient.getRetrofit()
//                .create(InoReaderAPI.class)
//                .markAllAsRead(queryMap)
//                .subscribe(new Observer<ResponseBody>() {
//                    @Override
//                    public void onSubscribe(Disposable disposable) {
//                        if (disposable.isDisposed()) return;
//                    }
//
//                    @Override
//                    public void onNext(ResponseBody responseBody) {
//                        if (responseBody != null) {
//                            try {
//                                Log.d(TAG, "markAllAsRead_responseBody: " + responseBody.string());
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        throwable.printStackTrace();
//                        Log.d(TAG, "onError: " + throwable.getMessage());
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "onComplete: From MarkAllRead");
//                    }
//                });
//    }

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


//    /**
//     * we can Unsubscribe from the feed Subscription
//     *
//     * @param feedId
//     */
//    public void unsubscribe(String feedId) {
//        editInoSubscription("unsubscribe", feedId, "", "", "");
//    }
//
//    /**
//     * @param title
//     * @param feedId
//     * @param categories
//     */
//    public void subscribeFeed(String title, String feedId, String categories) {
//        editInoSubscription("subscribe", feedId, title, categories, "");
//    }
//
//    /**
//     * @param title
//     * @param feedId
//     * @param addCategories
//     * @param removeCategories
//     */
//    public void editFeed(String title, String feedId, String addCategories, String removeCategories) {
//        editInoSubscription("edit", title, feedId, addCategories, removeCategories);
//    }
}