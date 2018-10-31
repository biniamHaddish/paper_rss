package com.biniam.rss.ui.utils;

import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by biniam_Haddish Tewelderberhan on 7/14/17.
 * <p>
 * Send image download tasks to {@link ImageDownloadThreadPoolManager}
 */

public class ImageDownloadCallable implements Callable {

    public static final String TAG = ImageDownloadCallable.class.getSimpleName();

    // Image download status Message IDs
    public static final int IMG_DOWNLOAD_SUCCESSFUL = 1110;
    public static final int IMG_DOWNLOAD_ERROR = 1111;

    public static final String FEED_BURNER = "feedburner.com";
    public static final String INOREADER_COM = "inoreader.com";

    // Constant used for feed item id
    public static final String FEED_ITEM_ID = "FEED_ITEM_ID";
    public static final String IS_LEAD_IMG = "IS_LEAD_IMG";

    private OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    private WeakReference<ImageDownloadThreadPoolManager> imageDownloadThreadPoolMangerWeakReference;
    private File feedItemCacheDir; // Directory to download images (It's specific for a feed item)
    private String imgDOMId; // Html id of the image (Used to updateFeedListDateSections image dynamically, once downloaded)
    private String url; // Url of the image we want to download
    private Bundle msgBundle;
    private boolean isLead;


    public ImageDownloadCallable(File feedItemCacheDir, String feedItemId, String imgDOMId, String url, boolean isLead) {
        this.feedItemCacheDir = feedItemCacheDir;
        this.imgDOMId = imgDOMId;
        this.url = url;
        this.isLead = isLead;

        // We return back this bundle containing the id of the feed item as a message
        // so that the UI can decide whether this downloaded image actully belongs to the current page
        msgBundle = new Bundle();
        msgBundle.putString(FEED_ITEM_ID, feedItemId);
        msgBundle.putBoolean(IS_LEAD_IMG, isLead);
    }

    @Override
    public Object call() {

        try {
            // Check if thread is interrupted before lengthy operation
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            // If we don't have a valid url, exit with IllegalArgumentException
            if (!URLUtil.isValidUrl(url)) {
                throw new IllegalArgumentException(String.format("Invalid image url %s", url));
            }

            if (url.contains(FEED_BURNER)) {
                Log.d(TAG, "call: feedburner image returning");
                return null;
            }

            String imgFileExtension = MimeTypeMap.getFileExtensionFromUrl(url);
            String imgFileName = URLUtil.guessFileName(url, imgFileExtension, null);
            File imgFile = new File(feedItemCacheDir, imgFileName);

            Request request = new Request.Builder().url(url).build();
            Response response = okHttpClient.newCall(request).execute();
            ResponseBody responseBody = response.body();
            InputStream imgDownloadInputStream = responseBody.byteStream();

            byte[] buffer = new byte[1024 * 4];
            long downloadedBytes = 0;
            long target = responseBody.contentLength();

            if (target > 0 && imgFile.exists() && imgFile.length() == target) {
                // This file has already been downloaded
                // Inform the UI thread here
                // Inform UI thread that download is finished successfully
                if (imageDownloadThreadPoolMangerWeakReference != null &&
                        imageDownloadThreadPoolMangerWeakReference.get() != null) {

                    Message downloadSuccessfulMessage = new Message();
                    downloadSuccessfulMessage.what = IMG_DOWNLOAD_SUCCESSFUL;
                    downloadSuccessfulMessage.obj = new Pair(imgDOMId, Uri.fromFile(imgFile).toString());
                    downloadSuccessfulMessage.setData(msgBundle);

                    imageDownloadThreadPoolMangerWeakReference.get().sendMessageToUiThread(downloadSuccessfulMessage);
                    imageDownloadThreadPoolMangerWeakReference.get().removeImageUrl(url);
                    return null;
                }
            } else if (target > 0 && imgFile.exists() && imgFile.length() != target) {
                // If a file exists with the same name but with different size
                // We will append "_" to the filename about to be downloaded
                String nameWoExt = imgFileName.replace("." + imgFileExtension, "");
                nameWoExt = nameWoExt + "_";
                imgFileName = nameWoExt + "." + imgFileExtension;
                imgFile = new File(feedItemCacheDir, imgFileName);
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
                        imgDOMId,
                        downloadedBytes >= 1000 ? String.valueOf(((float) downloadedBytes) / 1000f) + " KB" : downloadedBytes + " Bytes",
                        target >= 1000 ? String.valueOf(((float) target) / 1000f) + " KB" : target + " Bytes"));

                imgFileOutputStream.write(buffer, 0, read);
                downloadedBytes += read;
            }

            imgFileOutputStream.flush();
            imgFileOutputStream.close();
            imgDownloadInputStream.close();

            // Inform UI thread that download is finished successfully
            if (imageDownloadThreadPoolMangerWeakReference != null &&
                    imageDownloadThreadPoolMangerWeakReference.get() != null) {

                Message downloadSuccessfulMessage = new Message();
                downloadSuccessfulMessage.what = IMG_DOWNLOAD_SUCCESSFUL;
                downloadSuccessfulMessage.obj = new Pair(imgDOMId, Uri.fromFile(imgFile).toString());
                downloadSuccessfulMessage.setData(msgBundle);

                imageDownloadThreadPoolMangerWeakReference.get().sendMessageToUiThread(downloadSuccessfulMessage);
                imageDownloadThreadPoolMangerWeakReference.get().removeImageUrl(url);
            }

        } catch (FileNotFoundException e) {
            Log.d(TAG, String.format("File IO Error: %s", e.getMessage()));
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, String.format("Network error: %s", e.getMessage()));
            e.printStackTrace();

            // Inform the UI thread about the error
            if (imageDownloadThreadPoolMangerWeakReference != null &&
                    imageDownloadThreadPoolMangerWeakReference.get() != null) {

                Message downloadFailedMessage = new Message();
                downloadFailedMessage.what = IMG_DOWNLOAD_ERROR;
                downloadFailedMessage.obj = imgDOMId;
                downloadFailedMessage.setData(msgBundle);

                imageDownloadThreadPoolMangerWeakReference.get().sendMessageToUiThread(downloadFailedMessage);
                imageDownloadThreadPoolMangerWeakReference.get().removeImageUrl(url);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setImageDownloadThreadPoolManger(ImageDownloadThreadPoolManager imageDownloadThreadPoolManger) {
        this.imageDownloadThreadPoolMangerWeakReference = new WeakReference<ImageDownloadThreadPoolManager>(imageDownloadThreadPoolManger);
    }

    public String getUrl() {
        return url;
    }
}
