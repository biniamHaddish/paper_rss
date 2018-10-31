package com.biniam.rss.connectivity.inoreader.inoReaderApi;

import android.content.Context;
import android.util.Log;

import com.biniam.rss.connectivity.inoreader.DataCrancher;
import com.biniam.rss.connectivity.inoreader.InoApiFactory;
import com.biniam.rss.connectivity.inoreader.InoReaderRetrofitClient;
import com.biniam.rss.models.inoreader.InoStreamContentList;
import com.biniam.rss.persistence.db.PaperDatabase;
import com.biniam.rss.persistence.preferences.InternalStatePrefs;
import com.biniam.rss.persistence.preferences.PaperPrefs;
import com.biniam.rss.utils.AccountBroker;
import com.biniam.rss.utils.AutoImageCache;
import com.biniam.rss.utils.PaperApp;

import java.util.HashMap;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class InoSyncFactory {


    public static final String TAG = InoApiFactory.class.getSimpleName();
    public static final int MAX_LIMIT = 1000;
    public static int ARTICLE_SYNC_COUNT = 500;
    private static InoSyncFactory inoSyncFactory;
    private Context mContext;
    private long syncStartTime;
    private PaperDatabase rssDatabase;
    private PaperPrefs paperPrefs;
    private AutoImageCache autoImageCache;
    private InternalStatePrefs internalStatePrefs;
    private AccountBroker accountBroker;
    private DataCrancher dataCrancher;
    private CompositeDisposable disposables;

    public InoSyncFactory(Context context) {

        mContext = context;
        rssDatabase = PaperApp.getInstance().getDatabase();
        paperPrefs = PaperPrefs.getInstance(context);
        autoImageCache = AutoImageCache.getInstance(context);
        internalStatePrefs = InternalStatePrefs.getInstance(context);
        accountBroker = AccountBroker.getInstance(context);
        dataCrancher = DataCrancher.getInstance(context);

    }

    /**
     * new instance
     *
     * @param context
     * @return
     */
    public static InoSyncFactory getInstance(Context context) {
        if (inoSyncFactory == null) {
            inoSyncFactory = new InoSyncFactory(context);
        }
        return inoSyncFactory;
    }

    /**
     * get the  Unread feed items by Subscription.
     *
     * @param streamId
     */
    public static void getInoReaderFeedsPerSubscription(final String streamId) {

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("xt", "user/-/state/com.google/read");
        queryMap.put("n", "250");

        InoReaderRetrofitClient.getRetrofit()
                .create(InoReaderAPI.class)
                .getStreamContent(streamId.replace("_", "/").trim(), queryMap)
                .subscribe(new Observer<InoStreamContentList>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (d.isDisposed()) return;
                    }

                    @Override
                    public void onNext(InoStreamContentList inoStreamContentList) {
                        Log.d(TAG, String.format("this is Feed item from ", inoStreamContentList.getTitle()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, String.format("Error from  feed per subscription",e.getMessage()));
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }



}
