package com.biniam.rss.connectivity.feedWangler.retrofit;

import android.util.Log;

import com.biniam.rss.models.feedWragler.FeedCollections;
import com.biniam.rss.models.feedWragler.FeedItemsList;
import com.biniam.rss.models.feedWragler.FeedWranglerFeedIdsList;
import com.biniam.rss.models.feedWragler.NewStreamItem;
import com.biniam.rss.models.feedWragler.StreamItems;
import com.biniam.rss.models.feedWragler.StreamList;
import com.biniam.rss.models.feedWragler.StreamUpdate;
import com.google.gson.JsonObject;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.schedulers.NewThreadScheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by biniam on 1/1/18.
 */

public class FeedWranglerConnectify {

    public static final String TAG = FeedWranglerConnectify.class.getSimpleName();

    public static void addNewSubscription(String url) {

        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .addNewSubscription(url)
                .subscribeOn(Schedulers.io())
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<JsonObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.d(TAG, "onNext: Add new subscriptions\t" + jsonObject.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "addNewSubscription onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, " addNewSubscription onComplete: ");
                    }
                });
    }


    public static void subscribeToURLAndWait(String url, boolean choose_first) {
        // TODO: 1/2/18  change the return type to the Model type from JsonObject type
        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .subscribeToURLWait(url, choose_first)
                .subscribeOn(Schedulers.io())
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<JsonObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.d(TAG, "onNext subscribeToURLWait \t: " + jsonObject.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, " subscribeToURLAndWait onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, " subscribeToURLAndWait onComplete: ");
                    }
                });
    }


    public static void unsubscribeFromFeed(int feed_id) {

        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .unsubscribeFromFeed(feed_id)
                .subscribeOn(Schedulers.io())
                .retry()
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<JsonObject>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.d(TAG, "onNext: unsubscribeFromFeed" + jsonObject.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, " unsubscribeFromFeed onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "unsubscribeFromFeed onComplete: ");
                    }
                });
    }


    public static void renameFeed(int feed_id, String newFeedName) {

        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .renameFeed(feed_id, newFeedName)
                .subscribeOn(Schedulers.io())
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<JsonObject>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        if (jsonObject != null) {
                            Log.d(TAG, "onNext:  renameFeed \t> " + jsonObject.toString());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "renameFeed onComplete:");
                    }
                });
    }


    public static void retrieveFeedItems() {
        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .retrieveFeedItems()
                .subscribeOn(Schedulers.io())
                .observeOn(new NewThreadScheduler())

                .filter(new Predicate<FeedItemsList>() {
                    @Override
                    public boolean test(FeedItemsList itemsList) throws Exception {
                        return itemsList.getCount() > 0;  // filter and return if only the items are greater than Zero;
                    }
                })
                .subscribe(new Observer<FeedItemsList>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(FeedItemsList itemsList) {
                        if (itemsList != null) {
                            for (FeedItemsList.FeedItemsBean itemsBean : itemsList.getFeed_items()) {
                                Log.d(TAG, "onNext: retrieveFeedItems\t"
                                        + "Title\t" + itemsBean.getTitle() +
                                        "Author" + itemsBean.getAuthor() +
                                        "Body" + itemsBean.getBody()
                                );
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: retrieveFeedItems\t" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "retrieveFeedItems\tonComplete: ");
                    }
                });
    }

    public static void retrieveFeedItemsIds() {
        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .retrieveFeedItemsIds()
                .filter(new Predicate<FeedWranglerFeedIdsList>() {
                    @Override
                    public boolean test(FeedWranglerFeedIdsList idsList) throws Exception {
                        return idsList.getCount() > 0;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<FeedWranglerFeedIdsList>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(FeedWranglerFeedIdsList idsList) {
                        for (FeedWranglerFeedIdsList.FeedItemsBean itemsBean : idsList.getFeed_items()) {
                            Log.d(TAG, "onNext: idsList \t" + itemsBean.getFeed_item_id());
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: retrieveFeedItemsIds\t" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: retrieveFeedItemsIds\t ");
                    }
                });
    }

    /**
     * All the FeedItems with the given FeedIds.
     *
     * @param feed_ids
     */
    public static void retrieveFeedItemsCollectionByIds(Object feed_ids) {
        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .retrieveFeedItemsCollectionByIds(String.format("%3f", feed_ids))
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<FeedCollections>() {
                    @Override
                    public boolean test(FeedCollections feedCollections) throws Exception {
                        return feedCollections.getFeed_items().size() > 0;
                    }
                })
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<FeedCollections>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(FeedCollections feedCollections) {
                        for (FeedCollections.FeedItemsBean itemsBean : feedCollections.getFeed_items()) {
                            Log.d(TAG, "onNext: retrieveFeedItemsCollectionByIds\t" + itemsBean.getFeed_name());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: retrieveFeedItemsCollectionByIds\t" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: retrieveFeedItemsCollectionByIds\t");
                    }
                });
    }

    /**
     *
     */
    public static void markMultipleFeedItemsAsRead(StringBuilder feed_ids) {
        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .markMultipleFeedItemsAsRead(feed_ids)
                .subscribeOn(Schedulers.io())
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<JsonObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.d(TAG, "onNext: markMultipleFeedItemsAsRead\t" + jsonObject.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: markMultipleFeedItemsAsRead\t" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    public static void listCurrentSmartStreams() {
        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .listCurrentSmartStreams()
                .subscribeOn(Schedulers.io())
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<StreamList>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(StreamList streamList) {
                        for (StreamList.StreamsBean streamsBean : streamList.getStreams()) {
                            Log.d(TAG, "onNext: listCurrentSmartStreams:\tTitle:\t " +
                                    "\t" + streamsBean.getTitle() +
                                    "\tsearch Terms\t" + streamsBean.getSearch_term() +
                                    "\tStream ID\t" + streamsBean.getStream_id()
                            );
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: listCurrentSmartStreams\t" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: listCurrentSmartStreams\t");
                    }
                });
    }

    /**
     * @param stream_id
     */
    public static void retrieveCurrentFeedItemsSmartStream(int stream_id) {
        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .retrieveCurrentFeedItemsSmartStream(stream_id)
                .subscribeOn(Schedulers.io())
                .filter(streamItems -> streamItems.getCount() > 0)
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<StreamItems>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(StreamItems streamItems) {
                        for (StreamItems.FeedItemsBean itemsBean : streamItems.getFeed_items()) {
                            Log.d(TAG, "onNext: retrieveCurrentFeedItemsSmartStream\t:>Author:\t" +
                                    itemsBean.getAuthor() +
                                    "body\t" + itemsBean.getBody() +
                                    "feed Names\t" + itemsBean.getFeed_name()
                            );
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    /**
     * @param title
     * @param search_term
     * @param all_feeds
     * @param only_unread
     */
    public static void createNewSmartStream(String title, String search_term, boolean all_feeds, boolean only_unread) {
        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .createNewSmartStream(title, search_term, all_feeds, only_unread)
                .subscribeOn(Schedulers.io())
                .retry()// will retry if the Call is resulted on Error
                .filter(new Predicate<NewStreamItem>() {
                    @Override
                    public boolean test(NewStreamItem newStreamItem) throws Exception {
                        if (newStreamItem != null) {
                            return newStreamItem.getStream().getFeeds().size() > 0;
                        }

                        return false;
                    }
                })
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<NewStreamItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(NewStreamItem newStreamItem) {
                        Log.d(TAG,
                                "onNext: Title:\t " +
                                        newStreamItem.getStream() +
                                        "StreamId\t" + newStreamItem.getStream()
                        );
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    /**
     * @param stream_id
     * @param title
     * @param search_term
     * @param all_feeds
     * @param only_unread
     */
    public static void updateSmartStream(int stream_id, String title, String search_term, boolean all_feeds, boolean only_unread) {
        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .updateSmartStream(stream_id, title, search_term, all_feeds, only_unread)
                .subscribeOn(Schedulers.io())
                .filter(streamUpdate -> streamUpdate.getStream().getFeeds().size() > 0)
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<StreamUpdate>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(StreamUpdate streamUpdate) {
                        Log.d(TAG, "onNext: Title:\t" + streamUpdate.getStream().getTitle());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    /**
     * destroySmartStream.
     *
     * @param stream_id
     */
    public static void destroySmartStream(int stream_id) {
        FeedWranglerClient.getRetrofit()
                .create(FeedWranglerAPI.class)
                .destroySmartStream(stream_id)
                .subscribeOn(Schedulers.io())
                .observeOn(new NewThreadScheduler())
                .subscribe(new Observer<JsonObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(JsonObject streamUpdate) {
                        Log.d(TAG, "onNext: " + streamUpdate.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

}
