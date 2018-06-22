package com.biniam.rss.ui.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import java.lang.ref.WeakReference;

/**
 * Created by biniam_Haddish Tewelderberhan on 7/14/17.
 * <p>
 * This class manages worker threads that download of full articles using the mercury API
 * and sends the results with UI thread
 */

public class BackgroundTasksHandlerThread extends HandlerThread {

    public static final String NAME = "Mercury Handler Thread";

    public static final int FULL_ARTICLE_DOWNLOAD_SUCCESSFUL = 1112;
    public static final int FULL_ARTICLE_DOWNLOAD_ERROR = 1113;

    Handler mercuryAPIThreadHandler;
    private WeakReference<ThreadPoolCallback> feedReaderUICallbackWeakReference;

    public BackgroundTasksHandlerThread(String name) {
        super(name, Process.THREAD_PRIORITY_BACKGROUND);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mercuryAPIThreadHandler = new Handler(getLooper());
    }


    // Used by UI thread to send a runnable to the work thread's message queue
    public void postRunnable(Runnable runnable) {
        if (mercuryAPIThreadHandler != null) {
            mercuryAPIThreadHandler.post(runnable);
        }
    }

    public void setFeedReaderUICallbackWeakReference(ThreadPoolCallback threadPoolCallback) {
        this.feedReaderUICallbackWeakReference = new WeakReference<ThreadPoolCallback>(threadPoolCallback);
    }
}
