package com.biniam.rss.utils.timeToExpire;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.biniam.rss.persistence.preferences.InternalStatePrefs;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by biniam on 1/27/18.
 * calculate expire time of the beta test release :)
 */
public class BetaTestExpiryChecker {

    public static final String TAG = BetaTestExpiryChecker.class.getSimpleName();
    public static final String base_url = "texpandapp.com";
    public static long BETA_EXPIRY_DATE = 1519160400000L;
    static OkHttpClient client;
    private static BetaTestExpiryChecker betaTestExpiryChecker;
    private InternalStatePrefs internalStatePrefs;
    private Context mContext;

    private BetaTestExpiryChecker(Context context) {
        mContext = context;
        internalStatePrefs = InternalStatePrefs.getInstance(context);
        client = new OkHttpClient();
    }

    public static BetaTestExpiryChecker getInstance(Context context) {
        if (betaTestExpiryChecker == null) {
            betaTestExpiryChecker = new BetaTestExpiryChecker(context);
        }
        return betaTestExpiryChecker;
    }

    /**
     * get the Current time from amdoren.com using GMT time zone as (long)
     *
     * @return
     */
    public static long getCurrentTimeFromWebAPI() {
        // build the url down here.
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("http")
                .host(base_url)
                .addPathSegment("time")
                .addPathSegment("time.php")
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null) {
                return Long.valueOf(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return -1;
    }

    /**
     * Check if the time for Beta test is over or not .
     *
     * @param time_from_Api
     * @return
     */
    private static boolean isBetaExpired(long time_from_Api) {
        Log.e(TAG, String.format("isBetaExpired: %d days to expire", ((BETA_EXPIRY_DATE - time_from_Api) / DateUtils.DAY_IN_MILLIS)));
        return time_from_Api > (BETA_EXPIRY_DATE);
    }

    /**
     * Call this Method and will kick off the Observable
     */
    public void checkBetaExpiry() {
        getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getObserver());
    }


    /**
     * Main Observable
     *
     * @return
     */
    private Observable<Long> getObservable() {
        return Observable.create(e -> {
            if (!e.isDisposed()) {
                e.onNext(getCurrentTimeFromWebAPI());
                e.onComplete();
            }
        });
    }

    /**
     * Observer for the emitted time from website API Call
     *
     * @return
     */
    private Observer<Long> getObserver() {
        return new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                if (d.isDisposed()) return;
                Log.d(TAG, " onSubscribe : " + d.isDisposed());
            }

            @Override
            public void onNext(Long time) {
                Log.d(TAG, "onNext_Time\t: " + time);
                if (time > 0) {
                    boolean expired = isBetaExpired(time);
                    if (expired) {
                        Log.d(TAG, "onNext: saving expiry state to preferences");
                        internalStatePrefs.setBooleanPref(InternalStatePrefs.BETA_EXPIRED_PREF_KEY, true);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Log.d(TAG, " onError : " + e.getMessage());
                Log.d(TAG, " Cause of Error\t : " + e.getCause());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, " onComplete");
            }
        };
    }
}
