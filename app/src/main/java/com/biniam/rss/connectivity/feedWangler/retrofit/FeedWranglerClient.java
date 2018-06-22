package com.biniam.rss.connectivity.feedWangler.retrofit;

import com.biniam.rss.connectivity.feedWangler.FeedWanglerUrls;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by biniam on 12/23/17.
 */

public class FeedWranglerClient {

    public static final String API_BASE_URL = FeedWanglerUrls.BASE_URL;
    private static Retrofit retrofit = null;


    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .client(FeedWranglerAuthenticationInterceptor.customHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Only for login purposes because it uses pure okHttpClient with out interceptor .
     *
     * @return
     */
    public static Retrofit getRetrofitLogin() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .client(new OkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
