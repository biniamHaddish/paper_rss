package com.biniam.rss.connectivity.feedbin.retrofitClient;

import com.biniam.rss.connectivity.feedbin.feedbinUtils.FeedbinUrls;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by biniam on 12/16/17.
 */

public class RetrofitFeedbinClient {

    public static final String API_BASE_URL = FeedbinUrls.getBaseUrl();
    private static Retrofit retrofit = null;
    private static String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static Gson gson = new GsonBuilder()
            .setDateFormat(UTC_DATE_FORMAT)
            .create();

    /*retrofit Instance */
    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .client(FeedbinAuthenticationInterceptor.customHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
