package com.biniisu.leanrss.ui.controllers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.persistence.db.roomentities.FeedItemEntity;
import com.biniisu.leanrss.utils.ReadablyApp;
import com.biniisu.leanrss.utils.TemplateExtractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by biniam_Haddish on 8/1/17.
 * <p>
 * This a pager adapter that acts as an adpater that shows webviews in a view pager
 */

public class FeedItemsPagerAdapter extends PagerAdapter {

    public static final String TAG = FeedItemsPagerAdapter.class.getSimpleName();
    private FeedItemEntity[] feedItemEntities;
    private SparseArray<FeedViewPagerObject> pagerObjectSparseArray = new SparseArray<>();
    private WebViewInterceptor webViewInterceptor;
    private Context context;
    private WeakReference<ViewPager> viewPagerWeakReference;
    private int bgColor = -1;
    private int progressColor = -1;

    public FeedItemsPagerAdapter(ViewPager viewPager, FeedItemEntity[] feedItemEntities, WebViewInterceptor webViewInterceptor) {
        if (feedItemEntities == null) {
            throw new IllegalArgumentException("Null SparseArray<FeedItemEntity> Data!");
        }

        Log.d(TAG, String.format("FeedItemsPagerAdapter: we have %d feeds", feedItemEntities.length));

        viewPagerWeakReference = new WeakReference<>(viewPager);

        this.feedItemEntities = feedItemEntities;
        this.webViewInterceptor = webViewInterceptor;
        this.context = viewPagerWeakReference.get().getContext();
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.feed_webview_fragment_layout, container, false);
        RelativeLayout containerRelativeLayout = rootView.findViewById(R.id.container);
        WebView webView = rootView.findViewById(R.id.webView);
        ProgressBar progressBar = rootView.findViewById(R.id.webLoadingProgressBar);

        if (bgColor > 0) containerRelativeLayout.setBackgroundColor(bgColor);
        if (progressColor > 0)
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(progressColor));

        FeedViewPagerObject feedViewPagerObject = new FeedViewPagerObject(feedItemEntities[position], webView, progressBar, position);

        pagerObjectSparseArray.put(position, feedViewPagerObject);
        setUpWebView(webView, position);

        container.addView(
                rootView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        return rootView;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        WebView webView = pagerObjectSparseArray.get(position).getWebView();
        webView.removeJavascriptInterface(WebViewInterceptor.WEB_VIEW_JS_BRIDGE);
        container.removeViewInLayout((View) object);
        webView.destroy();
        Log.d(TAG, String.format("destroyItem: destroying item at %d", position));
        pagerObjectSparseArray.remove(position);
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public FeedViewPagerObject getItem(int position) {
        return pagerObjectSparseArray.get(position);
    }


    private void setUpWebView(WebView webView, int position) {
        assert webView != null;

        webView.setTag(pagerObjectSparseArray.get(position));
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setBlockNetworkLoads(true);

        webView.setWebViewClient(webViewInterceptor);
        webView.addJavascriptInterface(webViewInterceptor.getWebViewJsBridge(), WebViewInterceptor.WEB_VIEW_JS_BRIDGE);
        webView.loadUrl(getUrlForFeedItem(feedItemEntities[position].id));
    }


    @Override
    public int getCount() {
        return feedItemEntities.length;
    }


    public SparseArray<FeedViewPagerObject> getPagerObjectSparseArray() {
        return pagerObjectSparseArray;
    }


    /**
     * @return the {@link WebView} object associated with viewpager's current page
     */
    public WebView getCurrentPageWebView() {
        return getItem(viewPagerWeakReference.get().getCurrentItem()).getWebView();
    }

    /**
     * @eturns the {@link Document} object associated with viewpager's current page
     */
    public Document getCurrentPageDOM() {
        return getItem(viewPagerWeakReference.get().getCurrentItem()).pageDOM;
    }

    /**
     * @return the {@link FeedItemEntity} object associated with viewpager's current page
     */
    public FeedItemEntity getCurrentPageFeedItem() {

        if (getItem(viewPagerWeakReference.get().getCurrentItem()) == null) {
            return null;
        }

        return getItem(viewPagerWeakReference.get().getCurrentItem()).getFeedItem();
    }

    /**
     * @return the {@link FeedViewPagerObject} object associated with viewpager's current page
     */
    public FeedItemsPagerAdapter.FeedViewPagerObject getCurrentFeedViewPagerObject() {
        return getItem(viewPagerWeakReference.get().getCurrentItem());
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
    }

    public String getUrlForFeedItem(String id) {
        return "file:///"
                + context.getFilesDir() + "/"
                + TemplateExtractor.ASSET_EXTRACTION_DESTINATION
                + TemplateExtractor.ASSET_HTML_FOLDER
                + id;
    }

    // This class represents the feed item that is displayed in a viewpager
    public class FeedViewPagerObject {
        private FeedItemEntity feedItem;
        private WebView webView;
        private ProgressBar progressBar;
        private int position;
        private Document pageDOM;

        public FeedViewPagerObject(FeedItemEntity feedItem, WebView webView, ProgressBar progressBar, int position) {
            this.webView = webView;
            this.progressBar = progressBar;
            this.position = position;
            this.feedItem = feedItem;

            pageDOM = feedItem.hasFullArticle() ? Jsoup.parse(feedItem.fullArticle) : Jsoup.parse(feedItem.content);
        }

        public WebView getWebView() {
            return webView;
        }

        public int getPosition() {
            return position;
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }

        public FeedItemEntity getFeedItem() {
            return feedItem;
        }

        public void setFeedItem(FeedItemEntity feedItem) {
            this.feedItem = feedItem;
            if (feedItem.hasFullArticle()) {
                pageDOM = Jsoup.parse(feedItem.fullArticle);
            }
            updateFeedItem(false);
        }

        public Document getPageDOM() {
            return pageDOM;
        }

        public void updateFeedItem(boolean updateModifiedTime) {
            Log.d(TAG, String.format("updateFeedItem: updating feed %s", feedItem.title));
            new Observable<Void>() {
                @Override
                protected void subscribeActual(Observer<? super Void> observer) {
                    if (updateModifiedTime) feedItem.modifiedAt = System.currentTimeMillis();
                    ReadablyApp.getInstance().getDatabase().dao().updateFeedItem(feedItem);
                    observer.onComplete();
                }
            }.subscribeOn(Schedulers.io())
                    .subscribeWith(new DisposableObserver<Void>() {
                        @Override
                        public void onNext(Void aVoid) {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }

        public void persistDOM() {

            if (feedItem.hasFullArticle()) {
                feedItem.fullArticle = pageDOM.toString();
            } else {
                feedItem.content = pageDOM.toString();
            }

            updateFeedItem(false);
        }
    }
}