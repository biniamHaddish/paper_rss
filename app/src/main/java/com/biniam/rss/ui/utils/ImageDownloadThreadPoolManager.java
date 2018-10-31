package com.biniam.rss.ui.utils;

import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by biniam_Haddish Teweldeberhan  on 7/13/17.
 * <p>
 * This class handles the downloading of images as the user reads feed articles
 * by using always ready multiple threads
 */

public class ImageDownloadThreadPoolManager {

    public  static final String TAG = ImageDownloadThreadPoolManager.class.getSimpleName();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static ImageDownloadThreadPoolManager sInstance = null;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    static {
        sInstance = new ImageDownloadThreadPoolManager();
    }

    private ExecutorService executorService;
    private BlockingQueue<Runnable> taskQueue;
    private List<Future> runningTaskList;
    private List<String> queuedImgUrls = new ArrayList<>();
    private WeakReference<ThreadPoolCallback> feedReaderUICallbackWeakReference;

    public ImageDownloadThreadPoolManager() {
        taskQueue = new LinkedBlockingQueue<Runnable>();
        runningTaskList = new ArrayList<>();

        Log.d(TAG, "ImageDownloadThreadPoolManager: Available cores: " + NUMBER_OF_CORES);
        //executorService = Executors.newFixedThreadPool(2, new BackgroundThreadFactory());
        executorService = new ThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES * 2,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                taskQueue,
                new BackgroundThreadFactory()
        );

    }

    public static ImageDownloadThreadPoolManager getInstance() {
        return sInstance;
    }

    public void addCallable(Callable callable) {
        Log.d(TAG, "addCallable: adding a callable");
        String imgUrl = ((ImageDownloadCallable) callable).getUrl();
        if (!queuedImgUrls.contains(imgUrl)) {
            Future future = executorService.submit(callable);
            runningTaskList.add(future);
            queuedImgUrls.add(imgUrl);
        }

    }

    public void cancelAllTask() {
        Log.d(TAG, "cancelAllTask: cancelling all image download tasks");
        synchronized (this) {
            taskQueue.clear();
            for (Future task : runningTaskList) {
                if (!task.isDone()) task.cancel(true);
            }
            runningTaskList.clear();
            queuedImgUrls.clear();
        }
        //sendMessageToUiThread(new Message());
    }

    public void setFeedReaderUICallbackWeakReference(ThreadPoolCallback threadPoolCallback) {
        this.feedReaderUICallbackWeakReference = new WeakReference<ThreadPoolCallback>(threadPoolCallback);
    }

    public void sendMessageToUiThread(Message message) {
        if (feedReaderUICallbackWeakReference != null && feedReaderUICallbackWeakReference.get() != null) {
            feedReaderUICallbackWeakReference.get().publish(message);
        }
    }

    public void removeImageUrl(String url) {
        if (feedReaderUICallbackWeakReference != null && feedReaderUICallbackWeakReference.get() != null) {
            queuedImgUrls.remove(url);
        }
    }

    private static class BackgroundThreadFactory implements ThreadFactory {
        private static int tag = 1;

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("CustomThread" + tag);
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);

            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    Log.d(TAG, t.getName() + " encountered an error: " + e.getMessage());
                }
            });

            return thread;
        }
    }
}
