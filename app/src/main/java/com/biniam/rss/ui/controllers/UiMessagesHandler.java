package com.biniam.rss.ui.controllers;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;

import com.biniam.rss.ui.utils.ImageDownloadCallable;

/**
 * Created by biniam_Haddish on 9/1/17.
 * <p>
 * This class handles messages sent from background threads and delivers them to
 * components with {@link UiMessagesHandlerCallback}
 */

public class UiMessagesHandler extends Handler {
    private UiMessagesHandlerCallback uiMessagesHandlerCallback;

    public UiMessagesHandler(Looper looper, UiMessagesHandlerCallback uiMessagesHandlerCallback) {
        super(looper);
        this.uiMessagesHandlerCallback = uiMessagesHandlerCallback;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        switch (msg.what) {
            case ImageDownloadCallable.IMG_DOWNLOAD_SUCCESSFUL:
                if (msg.getData() != null && msg.obj != null) {
                    String feedItemId = msg.getData().getString(ImageDownloadCallable.FEED_ITEM_ID);
                    String imageDomId = ((Pair<String, String>) msg.obj).first;
                    String imgFilePath = ((Pair<String, String>) msg.obj).second;

                    if (uiMessagesHandlerCallback != null) {
                        uiMessagesHandlerCallback.onImageDownloadSuccessful(feedItemId, imageDomId, imgFilePath);
                    }
                }
                break;

            case ImageDownloadCallable.IMG_DOWNLOAD_ERROR:
                if (uiMessagesHandlerCallback != null && msg.getData() != null) {
                    uiMessagesHandlerCallback.onImageDownloadError(msg.getData().getString(ImageDownloadCallable.FEED_ITEM_ID));
                }
                break;

        }

    }

    public interface UiMessagesHandlerCallback {
        void onImageDownloadSuccessful(String feedItemId, String imgDomId, String downloadedFileUri);

        void onImageDownloadError(String imageDomId);
    }
}
