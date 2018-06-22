package com.biniam.rss.connectivity.inoreader;

import com.biniam.rss.connectivity.inoreader.inoReaderApi.HeaderInterceptor;
import com.biniam.rss.utils.ReadablyApp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by biniam on 2/15/18.
 */

public class InoReaderRetrofitClient {

    public static final String API_BASE_URL = InoReaderUrls.getBaseUrl();
    private static Retrofit retrofit = null;
    private static Gson gson = new GsonBuilder().create();

    //OkHttpClient  with interceptor
    private static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new HeaderInterceptor(ReadablyApp.getInstance()))
            .build();

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
