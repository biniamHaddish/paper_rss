package com.biniisu.leanrss.ui.utils;

import android.os.Message;
import android.util.Log;
import android.webkit.URLUtil;

import com.biniisu.leanrss.models.MercuryResult;
import com.biniisu.leanrss.persistence.db.roomentities.FeedItemEntity;
import com.biniisu.leanrss.ui.controllers.FeedParser;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by biniam_Haddish Teweldeberhan on 7/14/17.
 * <p>
 * This class downloads a full article for a given link using Mercury Parser API
 * and sends results to UI thread
 */

public class MercuryParserRunnable implements Runnable {

    public static final String TAG = MercuryParserRunnable.class.getSimpleName();


    private final String MERCURY_PARSER_URL = "https://mercury.postlight.com/parser?url=";
    private final String MERCURY_API_KEY_HEADER = "x-api-key";
    private final String MERCURY_API_KEY = "2oLeyDQUsF6EzxLlJHKsuXA6GGq5p7ZKiJdt4HPS";


    private WeakReference<ThreadPoolCallback> feedReaderUICallbackWeakReference;
    private OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    private FeedItemEntity feedItem;
    private String requestUrl;
    private Gson gson = new Gson();

    public MercuryParserRunnable(FeedItemEntity feedItem) {
        this.feedItem = feedItem;
        // Don't proceed if we have an invalid url
        if (!URLUtil.isValidUrl(feedItem.link))
            throw new IllegalArgumentException(String.format("Invalid url %s", feedItem.link));
        requestUrl = MERCURY_PARSER_URL + feedItem.link;
    }

    @Override
    public void run() {
        try {
            // Before running some lengthy and blocking work, check if the thread has been interrupted
            if (Thread.interrupted()) throw new InterruptedException();

            Request request = new Request.Builder()
                    .url(requestUrl)
                    .addHeader(MERCURY_API_KEY_HEADER, MERCURY_API_KEY)
                    .build();

            // Request for a full article version of urlToParse
            Response response = okHttpClient.newCall(request).execute();
            String responseJSON = response.body().string();
            MercuryResult mercuryResult = gson.fromJson(responseJSON, MercuryResult.class);

            if (mercuryResult != null) {
                if (mercuryResult.getContent() != null
                        && !mercuryResult.getContent().isEmpty()) {
                    Document contentDOM = Jsoup.parse(mercuryResult.getContent());
                    Elements imgs = contentDOM.getElementsByTag(FeedParser.IMG_TAG);

                    if (URLUtil.isValidUrl(mercuryResult.getLead_image_url()) && imgs.isEmpty()) {
                        // If MercuryResult contains leading images let's make it part of the content
                        // if the content doesn't contain any images to avoid repeating an image twice

                        // Only append lead image into content if the content has no images at all

                        contentDOM.prependElement(FeedParser.IMG_TAG).attr(FeedParser.SRC_ATTR, mercuryResult.getLead_image_url());
                        Log.d(TAG, String.format("run: html with added leading image looks like %s", contentDOM.toString()));
                        feedItem.fullArticle = contentDOM.toString();

                    }

                    feedItem.fullArticle = mercuryResult.getContent();

                    if (mercuryResult.getAuthor() != null && !mercuryResult.getAuthor().trim().isEmpty()) {
                        feedItem.fullArticle = mercuryResult.getAuthor();
                    }

                    if (feedReaderUICallbackWeakReference != null &&
                            feedReaderUICallbackWeakReference.get() != null) {

                        // Inform the UI thread about the successful mercury article parse
                        Message mercurySuccessMessage = new Message();
                        mercurySuccessMessage.what = BackgroundTasksHandlerThread.FULL_ARTICLE_DOWNLOAD_SUCCESSFUL;
                        mercurySuccessMessage.obj = feedItem;

                        Log.d(TAG, String.format("run: full article is %s", feedItem.fullArticle));
                        feedReaderUICallbackWeakReference.get().publish(mercurySuccessMessage);
                    }


                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // Network error notify UI thread
            Log.d(TAG, String.format("run: error downloading full article -> %s", e.getMessage()));
            if (feedReaderUICallbackWeakReference != null &&
                    feedReaderUICallbackWeakReference.get() != null) {
                // Inform the UI thread about the mercury error
                Message mercuryErrorMessage = new Message();
                mercuryErrorMessage.what = BackgroundTasksHandlerThread.FULL_ARTICLE_DOWNLOAD_ERROR;
                mercuryErrorMessage.obj = feedItem;

                feedReaderUICallbackWeakReference.get().publish(mercuryErrorMessage);
            }

            e.printStackTrace();
        }
    }

    public void setFeedReaderUICallback(ThreadPoolCallback threadPoolCallback) {
        this.feedReaderUICallbackWeakReference = new WeakReference<ThreadPoolCallback>(threadPoolCallback);
    }
}
