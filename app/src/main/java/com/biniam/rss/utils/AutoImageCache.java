package com.biniam.rss.utils;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import com.biniam.rss.persistence.db.ReadablyDatabase;
import com.biniam.rss.persistence.db.roomentities.FeedItemEntity;
import com.biniam.rss.persistence.db.roomentities.SubscriptionEntity;
import com.biniam.rss.ui.controllers.FeedParser;
import com.biniam.rss.ui.utils.ImageDownloadCallable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by biniam_Haddish on 1/20/18.
 * <p>
 * Caches images of recently synced unread feed items
 */

public class AutoImageCache {

    public static final String TAG = AutoImageCache.class.getSimpleName();

    private static AutoImageCache autoImageCache;
    private Context context;
    private ReadablyDatabase readablyDatabase;
    private OkHttpClient client = new OkHttpClient();
    private long since = -1;


    private AutoImageCache(Context context) {
        this.context = context;
        readablyDatabase = ReadablyApp.getInstance().getDatabase();
    }

    public static AutoImageCache getInstance(Context context) {
        if (autoImageCache == null) {
            autoImageCache = new AutoImageCache(context);
        }

        return autoImageCache;
    }

    public void startCaching(long since) {
        if (since < 0) return;

        context.sendBroadcast(new Intent(AccountBroker.ACTION_SYNC_STAGE_IMAGES));

        SubscriptionEntity[] subscriptionEntities = readablyDatabase.dao().getAllSubscriptions();
        for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
            FeedItemEntity[] feedItemEntities = readablyDatabase.dao().getFeedItemsForSubscriptionSyncAt(subscriptionEntity.id, since);
            for (FeedItemEntity feedItemEntity : feedItemEntities) {
                cacheImagesForFeedEntity(feedItemEntity);
            }
        }
    }

    private void cacheImagesForFeedEntity(FeedItemEntity feedItemEntity) {

        Document feedEntityDom = Jsoup.parse(feedItemEntity.hasFullArticle() ? feedItemEntity.fullArticle : feedItemEntity.content);
        Elements images = feedEntityDom.getElementsByTag(FeedParser.IMG_TAG);

        Log.d(TAG, String.format("downloadImagesForCurrentPage: there are %d images here", images.size()));

        // Let's first prepare the cache folder that images will be downloaded
        String subscriptionId = feedItemEntity.subscriptionId;
        String feedItemId = feedItemEntity.id;

        // Create the subscription directory
        File subscriptionDir = new File(context.getCacheDir(), subscriptionId);

        if (!subscriptionDir.exists()) subscriptionDir.mkdir();

        // Create the feed item directory as sub-directory of subscriptionDir
        File feedItemDir = new File(subscriptionDir, feedItemId);

        if (!feedItemDir.exists()) feedItemDir.mkdir();

        // We only cache the lead image here
        if (!images.isEmpty()) {
            Element img = images.iterator().next();
            String domId = img.id();
            String url = img.attr(FeedParser.ORG_SRC_ATTR);

            if (!Boolean.parseBoolean(img.attr(FeedParser.IMG_DOWNLAODED))) {
                try {
                    downloadImg(feedItemEntity, feedEntityDom, url, domId, feedItemDir);
                } catch (IOException e) {
                    Log.d(TAG, "cacheImagesForFeedEntity: error downloading image");
                }
            }
        }

    }

    private void downloadImg(FeedItemEntity feedItemEntity, Document feedEntityDom, String url, String domId, File feedItemDir) throws IOException {
        String imgFileExtension = MimeTypeMap.getFileExtensionFromUrl(url);
        String imgFileName = URLUtil.guessFileName(url, imgFileExtension, null);
        File imgFile = new File(feedItemDir, imgFileName);

        if (!URLUtil.isValidUrl(url)) return;

        if (url.contains(ImageDownloadCallable.FEED_BURNER)) {
            Log.d(TAG, "call: feedburner image returning");
            return;
        }

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        InputStream imgDownloadInputStream = responseBody.byteStream();

        byte[] buffer = new byte[1024 * 4];
        long downloadedBytes = 0;
        long target = responseBody.contentLength();

        if (target > 0 && imgFile.exists() && imgFile.length() == target) {
            // This file has already been downloaded
            return;
        } else if (target > 0 && imgFile.exists() && imgFile.length() != target) {
            // If a file exists with the same name but with different size
            // We will append "_" to the filename about to be downloaded
            String nameWoExt = imgFileName.replace("." + imgFileExtension, "");
            nameWoExt = nameWoExt + "_";
            imgFileName = nameWoExt + "." + imgFileExtension;
            imgFile = new File(feedItemDir, imgFileName);
        }


        FileOutputStream imgFileOutputStream = new FileOutputStream(imgFile);

        // Downloading the image
        while (true) {
            int read = imgDownloadInputStream.read(buffer);
            if (read == -1) {
                // We have reached the end of imgDownloadInputStream
                break;
            }

            Log.d(TAG, String.format("doInBackground: id %s download status %s / %s",
                    domId,
                    downloadedBytes >= 1000 ? String.valueOf(((float) downloadedBytes) / 1000f) + " KB" : downloadedBytes + " Bytes",
                    target >= 1000 ? String.valueOf(((float) target) / 1000f) + " KB" : target + " Bytes"));

            imgFileOutputStream.write(buffer, 0, read);
            downloadedBytes += read;
        }

        imgFileOutputStream.flush();
        imgFileOutputStream.close();
        imgDownloadInputStream.close();

        feedEntityDom.getElementById(domId)
                .attr(FeedParser.IMG_DOWNLAODED, "true");

        feedEntityDom
                .getElementById(domId)
                .attr(FeedParser.SRC_ATTR, Uri.fromFile(imgFile).toString());

        feedItemEntity.leadImgPath = Uri.fromFile(imgFile).toString();

        if (feedItemEntity.hasFullArticle()) {
            feedItemEntity.fullArticle = feedEntityDom.toString();
        } else {
            feedItemEntity.content = feedEntityDom.toString();
        }

        readablyDatabase.dao().updateFeedItem(feedItemEntity);
    }

}