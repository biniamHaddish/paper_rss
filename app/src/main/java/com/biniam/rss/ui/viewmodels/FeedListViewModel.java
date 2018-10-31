package com.biniam.rss.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.biniam.rss.persistence.db.PaperDatabase;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.persistence.db.roomentities.TagEntity;
import com.biniam.rss.utils.PaperApp;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by biniam_haddish on 11/27/17.
 * <p>
 * A view model responsible for updateFeedListDateSections ui components in {@link com.biniam.rss.ui.base.HomeActivity}
 */

public class FeedListViewModel extends ViewModel {

    public static final String TAG = FeedListViewModel.class.getSimpleName();


    public static final int FAV = 0;
    public static final int UNREAD = 1;
    public static final int ALL = 2;

    private String subscriptionId = null;
    private PaperDatabase paperDatabase;
    private MutableLiveData<FeedListItemModel[]> feedItemsMutableLiveData = new MediatorLiveData<>();
    private MediatorLiveData tagSubscriptionAggregateMediatorLiveData = new MediatorLiveData<>();


    public FeedListViewModel() {
        paperDatabase = PaperApp.getInstance().getDatabase();
    }

    public void init() {
        // Setup an aggregate live data mediator to listen on tags an subscriptions at one
        LiveData<SubscriptionEntity[]> subscriptionEntityLiveData = paperDatabase.dao().getAllSubscriptionsLiveData();
        LiveData<TagEntity[]> tagEntitiesLiveData = paperDatabase.dao().getDistinctTagsLiveData();
        tagSubscriptionAggregateMediatorLiveData.addSource(subscriptionEntityLiveData, o -> tagSubscriptionAggregateMediatorLiveData.postValue(o));
        tagSubscriptionAggregateMediatorLiveData.addSource(tagEntitiesLiveData, o -> tagSubscriptionAggregateMediatorLiveData.postValue(o));
    }

    private FeedListItemModel[] getFeedItemsForCategoryAndSubscription(int category, String subscriptionId, boolean sortNewerToOlder) {


        Log.d(TAG, String.format("getFeedItemsForCategoryAndSubscription: category is %d and subcriptionId is %s", category, subscriptionId));

        switch (category) {
            case UNREAD:
                if (subscriptionId != null) {
                    return sortNewerToOlder ?
                            paperDatabase.dao().getUnreadItemsForSubscriptionFeedListModels(subscriptionId) :
                            paperDatabase.dao().getUnreadItemsForSubscriptionFeedListModelsOlderToNewer(subscriptionId);
                } else {
                    return sortNewerToOlder ?
                            paperDatabase.dao().getAllUnreadFeedListModels() :
                            paperDatabase.dao().getAllUnreadFeedListModelsOlderToNewer();
                }

            case ALL:
                if (subscriptionId != null) {
                    return sortNewerToOlder ?
                            paperDatabase.dao().getAllFeedItemsForSubscriptionFeedListModels(subscriptionId) :
                            paperDatabase.dao().getAllFeedItemsForSubscriptionFeedListModelsOlderToNewer(subscriptionId);
                } else {
                    return sortNewerToOlder ?
                            paperDatabase.dao().getAllFeedListModels() :
                            paperDatabase.dao().getAllFeedListModelsOlderToNewer();
                }

            case FAV:
                if (subscriptionId != null) {
                    return sortNewerToOlder ?
                            paperDatabase.dao().getFavoriteFeedListModelsForSubscription(subscriptionId) :
                            paperDatabase.dao().getFavoriteFeedListModelsForSubscriptionOlderToNewer(subscriptionId);

                } else {
                    return sortNewerToOlder ?
                            paperDatabase.dao().getAllFavoriteFeedListModels() :
                            paperDatabase.dao().getAllFavoriteFeedListModelsOlderToNewer();
                }
        }

        return null;
    }


    private FeedListItemModel[] getFeedItemsForCategoryAndTag(int category, String tagName, boolean sortNewerToOlder) {
        //List<String> subscriptionIds = paperDatabase.dao().getSubscriptionIdsForTag(tagName);
        switch (category) {
            case UNREAD:
                return sortNewerToOlder ?
                        paperDatabase.dao().getUnreadFeedListModelsForTag(tagName) :
                        paperDatabase.dao().getUnreadFeedListModelsForTagOlderToNewer(tagName);
            case ALL:
                return sortNewerToOlder ?
                        paperDatabase.dao().getAllFeedListModelsForTag(tagName) :
                        paperDatabase.dao().getAllFeedListModelsForTagOlderToNewer(tagName);
            case FAV:
                return sortNewerToOlder ?
                        paperDatabase.dao().getFavFeedListModelsForTag(tagName) :
                        paperDatabase.dao().getFavFeedListModelsForTagOlderToNewer(tagName);
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
