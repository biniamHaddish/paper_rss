package com.biniisu.leanrss.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.biniisu.leanrss.persistence.db.ReadablyDatabase;
import com.biniisu.leanrss.persistence.db.roomentities.SubscriptionEntity;
import com.biniisu.leanrss.persistence.db.roomentities.TagEntity;
import com.biniisu.leanrss.utils.ReadablyApp;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by  on 11/27/17.
 * <p>
 * A view model responsible for updateFeedListDateSections ui components in {@link com.biniisu.leanrss.ui.base.HomeActivity}
 */

public class FeedListViewModel extends ViewModel {

    public static final String TAG = FeedListViewModel.class.getSimpleName();


    public static final int FAV = 0;
    public static final int UNREAD = 1;
    public static final int ALL = 2;

    private String subscriptionId = null;
    private ReadablyDatabase readablyDatabase;
    private MutableLiveData<FeedListItemModel[]> feedItemsMutableLiveData = new MediatorLiveData<>();
    private MediatorLiveData tagSubscriptionAggregateMediatorLiveData = new MediatorLiveData<>();


    public FeedListViewModel() {
        readablyDatabase = ReadablyApp.getInstance().getDatabase();
    }

    public void init() {
        // Setup an aggregate live data mediator to listen on tags an subscriptions at one
        LiveData<SubscriptionEntity[]> subscriptionEntityLiveData = readablyDatabase.dao().getAllSubscriptionsLiveData();
        LiveData<TagEntity[]> tagEntitiesLiveData = readablyDatabase.dao().getDistinctTagsLiveData();
        tagSubscriptionAggregateMediatorLiveData.addSource(subscriptionEntityLiveData, o -> tagSubscriptionAggregateMediatorLiveData.postValue(o));
        tagSubscriptionAggregateMediatorLiveData.addSource(tagEntitiesLiveData, o -> tagSubscriptionAggregateMediatorLiveData.postValue(o));
    }

    private FeedListItemModel[] getFeedItemsForCategoryAndSubscription(int category, String subscriptionId, boolean sortNewerToOlder) {


        Log.d(TAG, String.format("getFeedItemsForCategoryAndSubscription: category is %d and subcriptionId is %s", category, subscriptionId));

        switch (category) {
            case UNREAD:
                if (subscriptionId != null) {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getUnreadItemsForSubscriptionFeedListModels(subscriptionId) :
                            readablyDatabase.dao().getUnreadItemsForSubscriptionFeedListModelsOlderToNewer(subscriptionId);
                } else {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getAllUnreadFeedListModels() :
                            readablyDatabase.dao().getAllUnreadFeedListModelsOlderToNewer();
                }

            case ALL:
                if (subscriptionId != null) {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getAllFeedItemsForSubscriptionFeedListModels(subscriptionId) :
                            readablyDatabase.dao().getAllFeedItemsForSubscriptionFeedListModelsOlderToNewer(subscriptionId);
                } else {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getAllFeedListModels() :
                            readablyDatabase.dao().getAllFeedListModelsOlderToNewer();
                }

            case FAV:
                if (subscriptionId != null) {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getFavoriteFeedListModelsForSubscription(subscriptionId) :
                            readablyDatabase.dao().getFavoriteFeedListModelsForSubscriptionOlderToNewer(subscriptionId);

                } else {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getAllFavoriteFeedListModels() :
                            readablyDatabase.dao().getAllFavoriteFeedListModelsOlderToNewer();
                }
        }

        return null;
    }


    private FeedListItemModel[] getFeedItemsForCategoryAndTag(int category, String tagName, boolean sortNewerToOlder) {
        //List<String> subscriptionIds = readablyDatabase.dao().getSubscriptionIdsForTag(tagName);
        switch (category) {
            case UNREAD:
                return sortNewerToOlder ?
                        readablyDatabase.dao().getUnreadFeedListModelsForTag(tagName) :
                        readablyDatabase.dao().getUnreadFeedListModelsForTagOlderToNewer(tagName);
            case ALL:
                return sortNewerToOlder ?
                        readablyDatabase.dao().getAllFeedListModelsForTag(tagName) :
                        readablyDatabase.dao().getAllFeedListModelsForTagOlderToNewer(tagName);
            case FAV:
                return sortNewerToOlder ?
                        readablyDatabase.dao().getFavFeedListModelsForTag(tagName) :
                        readablyDatabase.dao().getFavFeedListModelsForTagOlderToNewer(tagName);
        }
        return null;
    }

    public void showFeedItemsForSubscription(int category, String subscriptionId, boolean sortNewerToOlder) {
        new Observable<FeedListItemModel[]>() {
            @Override
            protected void subscribeActual(io.reactivex.Observer<? super FeedListItemModel[]> observer) {
                FeedListItemModel[] feedListItemModels = getFeedItemsForCategoryAndSubscription(category, subscriptionId, sortNewerToOlder);
                Log.d(TAG, String.format("onNext: there are %d items for subscriptionid %s", feedListItemModels.length, subscriptionId));
                observer.onNext(feedListItemModels);
            }
        }
                .subscribeOn(Schedulers.io())
                .subscribeWith(
                        new DisposableObserver<FeedListItemModel[]>() {
                            @Override
                            public void onNext(FeedListItemModel[] feedItemEntities) {
                                feedItemsMutableLiveData.postValue(feedItemEntities);
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onComplete() {
                            }
                        });
    }

    public void showFeedItemsForTag(int category, String tag, boolean sortNewerToOlder) {
        new Observable<FeedListItemModel[]>() {
            @Override
            protected void subscribeActual(io.reactivex.Observer<? super FeedListItemModel[]> observer) {
                observer.onNext(getFeedItemsForCategoryAndTag(category, tag, sortNewerToOlder));
            }
        }
                .subscribeOn(Schedulers.io())
                .subscribeWith(
                        new DisposableObserver<FeedListItemModel[]>() {
                            @Override
                            public void onNext(FeedListItemModel[] feedItemEntities) {
                                feedItemsMutableLiveData.postValue(feedItemEntities);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        }
                );
    }


    public MutableLiveData<FeedListItemModel[]> getFeedItemsMutableLiveData() {
        return feedItemsMutableLiveData;
    }

    public MediatorLiveData getTagSubscriptionAggregateMediatorLiveData() {
        return tagSubscriptionAggregateMediatorLiveData;
    }
}
