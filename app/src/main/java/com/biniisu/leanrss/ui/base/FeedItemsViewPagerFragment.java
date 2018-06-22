package com.biniisu.leanrss.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.persistence.db.ReadablyDatabase;
import com.biniisu.leanrss.persistence.db.roomentities.FeedItemEntity;
import com.biniisu.leanrss.persistence.preferences.InternalStatePrefs;
import com.biniisu.leanrss.persistence.preferences.ReadingPrefs;
import com.biniisu.leanrss.ui.controllers.FeedItemsPagerAdapter;
import com.biniisu.leanrss.ui.controllers.WebViewInterceptor;
import com.biniisu.leanrss.utils.ReadablyApp;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by  on 8/28/17.
 */

public class FeedItemsViewPagerFragment extends Fragment {

    public static final String ARG_START_POSITION = "ARG_START_POSITION";
    public static final String ARG_SUBSCRIPTION_ID = "ARG_SUBSCRIPTION_ID";
    public static final String ARG_TAG_NAME = "ARG_TAG_NAME";
    public static final String ARG_ALL_SUBSCRIPTIONS = "ARG_ALL_SUBSCRIPTIONS";
    public static final String ARG_LAST_UNREAD_FEED_ITEM = "ARG_LAST_UNREAD_FEED_ITEM";

    private int startPosition = 0;
    private String subscriptionId = null;
    private String tagName = null;
    private boolean isAllSubscriptions;
    private FeedItemsPagerAdapter feedItemsPagerAdapter;
    private WebViewInterceptor webViewInterceptor;
    private ViewPager feedItemsViewPager;
    private InternalStatePrefs internalStatePrefs;
    private ReadablyDatabase readablyDatabase;
    private FeedItemEntity lastUnreadFeedItemEntity;
    private ReadingPrefs readingPrefs;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getArguments().containsKey(ARG_START_POSITION))
            startPosition = getArguments().getInt(ARG_START_POSITION);
        if (getArguments().containsKey(ARG_SUBSCRIPTION_ID))
            subscriptionId = getArguments().getString(ARG_SUBSCRIPTION_ID);
        if (getArguments().containsKey(ARG_TAG_NAME))
            tagName = getArguments().getString(ARG_TAG_NAME);
        if (getArguments().containsKey(ARG_ALL_SUBSCRIPTIONS))
            isAllSubscriptions = getArguments().getBoolean(ARG_ALL_SUBSCRIPTIONS);
        if (getArguments().containsKey(ARG_LAST_UNREAD_FEED_ITEM))
            lastUnreadFeedItemEntity = (FeedItemEntity) getArguments().getSerializable(ARG_LAST_UNREAD_FEED_ITEM);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        readablyDatabase = ReadablyApp.getInstance().getDatabase();
        internalStatePrefs = InternalStatePrefs.getInstance(getContext());
        readingPrefs = ReadingPrefs.getInstance(getContext());
        feedItemsViewPager = (ViewPager) inflater.inflate(R.layout.feed_items_viewpager_layout, container, false);

        new Observable<FeedItemEntity[]>() {
            @Override
            protected void subscribeActual(Observer<? super FeedItemEntity[]> observer) {
                if (isAllSubscriptions) {
                    observer.onNext(getFeedItemsForCategoryAndSubscription(internalStatePrefs.selectedFeedFilter, null, internalStatePrefs.sortNewerToOlder));
                } else if (subscriptionId != null) {
                    observer.onNext(getFeedItemsForCategoryAndSubscription(internalStatePrefs.selectedFeedFilter, subscriptionId, internalStatePrefs.sortNewerToOlder));
                } else if (tagName != null) {
                    observer.onNext(getFeedItemsForCategoryAndTag(internalStatePrefs.selectedFeedFilter, tagName));
                }
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<FeedItemEntity[]>() {
            @Override
            public void onNext(FeedItemEntity[] feedItemEntities) {

                webViewInterceptor = new WebViewInterceptor(feedItemsViewPager, (WebViewInterceptor.WebViewInterceptorCallback) getActivity(), startPosition);
                // Add the last unread item
                if (lastUnreadFeedItemEntity != null) {
                    FeedItemEntity[] feedItemEntitiesWithLastUnread = new FeedItemEntity[feedItemEntities.length + 1];

                    int insertedPos = 0;

                    if (startPosition < feedItemEntities.length) {
                        insertedPos = startPosition;
                    }

                    feedItemEntitiesWithLastUnread[startPosition] = lastUnreadFeedItemEntity;
                    for (int i = 0; i < feedItemEntities.length; i++) {
                        if (i != insertedPos) {
                            feedItemEntitiesWithLastUnread[i] = feedItemEntities[i];
                        } else {
                            feedItemEntitiesWithLastUnread[i + 1] = feedItemEntities[i];
                        }
                    }

                    feedItemsPagerAdapter = new FeedItemsPagerAdapter(feedItemsViewPager, feedItemEntitiesWithLastUnread, webViewInterceptor);
                } else {
                    feedItemsPagerAdapter = new FeedItemsPagerAdapter(feedItemsViewPager, feedItemEntities, webViewInterceptor);
                }

                setViewPagerBg();

                feedItemsViewPager.setAdapter(feedItemsPagerAdapter);
                feedItemsViewPager.setCurrentItem(startPosition);
                feedItemsViewPager.addOnPageChangeListener((ViewPager.OnPageChangeListener) getActivity());

                feedItemsViewPager.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        return feedItemsViewPager;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //feedItemsViewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.regular_padding));
        //feedItemsViewPager.setPageMarginDrawable(R.color.french_gray);
    }

    public FeedItemEntity[] getFeedItemsForCategoryAndSubscription(int category, String subscriptionId, boolean sortNewerToOlder) {
        switch (category) {
            case InternalStatePrefs.UNREAD:
                if (subscriptionId != null) {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getUnreadItemsForSubscription(subscriptionId) :
                            readablyDatabase.dao().getUnreadItemsForSubscriptionOlderToNewer(subscriptionId);
                } else {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getAllUnreadFeedItems() :
                            readablyDatabase.dao().getAllUnreadFeedItemsOlderToNewer();
                }

            case InternalStatePrefs.EVERYTHING:
                if (subscriptionId != null) {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getAllFeedItemsForSubscription(subscriptionId) :
                            readablyDatabase.dao().getAllFeedItemsForSubscriptionOlderToNewer(subscriptionId);

                } else {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getAllFeedItems() :
                            readablyDatabase.dao().getAllFeedItemsOlderToNewer();
                }

            case InternalStatePrefs.FAVORITES:
                if (subscriptionId != null) {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getFavoriteFeedItemsForSubscription(subscriptionId) :
                            readablyDatabase.dao().getFavoriteFeedItemsForSubscriptionOlderToNewer(subscriptionId);
                } else {
                    return sortNewerToOlder ?
                            readablyDatabase.dao().getAllFavoriteFeedListItems() :
                            readablyDatabase.dao().getAllFavoriteFeedListItemsOlderToNewer();
                }
        }

        return null;
    }

    public FeedItemEntity[] getFeedItemsForCategoryAndTag(int category, String tagName) {
        switch (category) {
            case InternalStatePrefs.UNREAD:
                return readablyDatabase.dao().getUnreadFeedItemsForTag(tagName);
            case InternalStatePrefs.EVERYTHING:
                return readablyDatabase.dao().getAllFeedItemsForTag(tagName);
            case InternalStatePrefs.FAVORITES:
                return readablyDatabase.dao().getFavFeedItemsForTag(tagName);
        }
        return null;
    }

    public FeedItemsPagerAdapter getFeedItemsPagerAdapter() {
        return feedItemsPagerAdapter;
    }

    public void moveTo(int position) {
        feedItemsViewPager.setCurrentItem(position, true);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
//        //Log.d(FeedItemsViewPagerFragment.class.getSimpleName(), "onAttach: on attach reinitializing");
//        if (webViewInterceptor == null || feedItemsPagerAdapter  == null || localFeedPersistenceManager == null){
//            webViewInterceptor = new WebViewInterceptor(feedItemsViewPager, (WebViewInterceptor.WebViewInterceptorCallback) getActivity(), startPosition);
//            feedItemsPagerAdapter = new FeedItemsPagerAdapter(feedItemsViewPager, getFilteredFeedItems(), webViewInterceptor);
//            feedItemsViewPager.setAdapter(feedItemsPagerAdapter);
//        }
    }


    public void updateCSS() {
        if (webViewInterceptor != null) {
            webViewInterceptor.updateCSS();
        }
    }

    public void goToNextFeed() {
        feedItemsViewPager.setCurrentItem(feedItemsViewPager.getCurrentItem() + 1);
    }

    public void goToPreviousFeed() {
        feedItemsViewPager.setCurrentItem(feedItemsViewPager.getCurrentItem() - 1);
    }

    public void removeAllViews() {
        feedItemsViewPager.removeAllViews();
    }

    public FeedItemsPagerAdapter.FeedViewPagerObject getCurrentPagerObject() {
        return feedItemsPagerAdapter.getCurrentFeedViewPagerObject();
    }


    public FeedItemsPagerAdapter.FeedViewPagerObject getCurrentPagerObjectAt(int position) {
        return feedItemsPagerAdapter.getItem(position);
    }


    public int getFeedItemsCount() {
        return feedItemsPagerAdapter.getCount();
    }

    public SparseArray<FeedItemsPagerAdapter.FeedViewPagerObject> getFeedViewPagerObjectSparseArray() {
        return feedItemsPagerAdapter.getPagerObjectSparseArray();
    }


    public void showControls(boolean landscape) {
        if (landscape) {
            feedItemsViewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } else {
            feedItemsViewPager.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    public void hideControls(boolean landscape) {
        if (!landscape) {
            feedItemsViewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        } else {
            feedItemsViewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }

    public void setSystemUiVisibility(int flags) {
        feedItemsViewPager.setSystemUiVisibility(flags);
    }

    public void swapData(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        feedItemsPagerAdapter = new FeedItemsPagerAdapter(feedItemsViewPager, null, webViewInterceptor);
        feedItemsViewPager.setAdapter(feedItemsPagerAdapter);
    }

    public void setViewPagerBg() {
        if (getActivity() == null) return;

        if (readingPrefs.backgroundColor.equals(getActivity().getString(R.string.white))) {
            feedItemsViewPager.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
            feedItemsPagerAdapter.setBgColor(getActivity().getResources().getColor(R.color.white));
            feedItemsPagerAdapter.setProgressColor(getActivity().getResources().getColor(R.color.mako));
        } else if (readingPrefs.backgroundColor.equals(getString(R.string.merino))) {
            feedItemsViewPager.setBackgroundColor(getActivity().getResources().getColor(R.color.merino));
            feedItemsPagerAdapter.setBgColor(getActivity().getResources().getColor(R.color.merino));
            feedItemsPagerAdapter.setProgressColor(getActivity().getResources().getColor(R.color.irish_coffee));
        } else if (readingPrefs.backgroundColor.equals(getString(R.string.scarpa_flow))) {
            feedItemsViewPager.setBackgroundColor(getActivity().getResources().getColor(R.color.scarpa_flow));
            feedItemsPagerAdapter.setBgColor(getActivity().getResources().getColor(R.color.scarpa_flow));
            feedItemsPagerAdapter.setProgressColor(getActivity().getResources().getColor(R.color.white));
        } else {
            feedItemsViewPager.setBackgroundColor(getActivity().getResources().getColor(R.color.onyx));
            feedItemsPagerAdapter.setBgColor(getActivity().getResources().getColor(R.color.onyx));
            feedItemsPagerAdapter.setProgressColor(getActivity().getResources().getColor(R.color.white));
        }
    }


}
