package com.biniisu.leanrss.connectivity.local;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.biniisu.leanrss.persistence.db.ReadablyDatabase;
import com.biniisu.leanrss.persistence.db.roomentities.FeedItemEntity;
import com.biniisu.leanrss.persistence.db.roomentities.SubscriptionEntity;
import com.biniisu.leanrss.utils.ReadablyApp;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by biniam_Haddish on 12/7/2017.
 *
 * This services parses raw xml (atom or channel) files and saves persists them using room
 *
 */

public class AtomRssSyncService extends Service {

    public static final String TAG = AtomRssSyncService.class.getSimpleName();
    public static final String SUBSCRIPTION_URLS = "SUBSCRIPTION_IDS";
    private ReadablyDatabase readablyDatabase;
    private OkHttpClient client = new OkHttpClient.Builder().build();
    private AtomRssParser atomRssParser;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        readablyDatabase = ReadablyApp.getInstance().getDatabase();
        atomRssParser = AtomRssParser.getInstance(getApplicationContext());

        if (intent != null) {
            List<String> subscriptionIds = (List<String>) intent.getSerializableExtra(SUBSCRIPTION_URLS);

            if (subscriptionIds != null) {
                new Observable<SubscriptionEntity[]>() {
                    @Override
                    protected void subscribeActual(io.reactivex.Observer<? super SubscriptionEntity[]> observer) {
                        observer.onNext(readablyDatabase.dao().getSubscriptions(subscriptionIds));
                    }
                }
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(
                                new DisposableObserver<SubscriptionEntity[]>() {
                                    @Override
                                    public void onNext(SubscriptionEntity[] subscriptionEntities) {
                                        saveRssAtomFeeds(subscriptionEntities);
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                }
                        );
            }
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void saveRssAtomFeeds(SubscriptionEntity[] subscriptionEntities) {
        new Observable<List<FeedItemEntity>>() {
            @Override
            protected void subscribeActual(Observer observer) {
                for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
                    try {
                        observer.onNext(
                                atomRssParser.getFeedItems(getXmlString(subscriptionEntity.rssLink), subscriptionEntity)
                        );
                    } catch (Exception e) {
                        observer.onError(e);
                    }
                }

                observer.onComplete();
            }
        }.subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<List<FeedItemEntity>>() {
                    @Override
                    public void onNext(List<FeedItemEntity> feedItemEntities) {
                        readablyDatabase.dao()
                                .insertFeedItems(feedItemEntities);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: sync complete");
                        stopSelf();
                    }
                });

    }


    public String getXmlString(String subscriptionUrl) throws Exception {
        if (subscriptionUrl == null || subscriptionUrl.isEmpty()) {
            throw new IllegalArgumentException("Rss url can not be null or empty");
        }

        Log.d(TAG, String.format("getUrlStringContents: getting xml for %s", subscriptionUrl));

        client.followRedirects();
        Request request = new Request.Builder().url(subscriptionUrl).build();
        Response response = client.newCall(request).execute();
        String body = response.body().string();
        response.close();
        return body;
    }
}
