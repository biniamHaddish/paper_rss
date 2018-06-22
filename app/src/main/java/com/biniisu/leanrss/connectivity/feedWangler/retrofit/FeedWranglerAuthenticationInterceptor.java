package com.biniisu.leanrss.connectivity.feedWangler.retrofit;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by biniam on 12/23/17.
 */

public class FeedWranglerAuthenticationInterceptor {

    private static final String TAG = FeedWranglerAuthenticationInterceptor.class.getSimpleName();
    static OkHttpClient customHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            spitAccessToken();// calling the token spit
            if (spitAccessToken().isEmpty()) {
                Log.e(TAG, "Access token is empty or Already added to the Header....");
                return chain.proceed(chain.request());
            }
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();

            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter("access_token", spitAccessToken())
                    .build();

            // Request customization: add request headers
            Request.Builder requestBuilder = original.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .url(url);
            Request request = requestBuilder.build();
            return chain.proceed(request);
        }
            }).build();
    private static HashMap<String, String> Access_Token = new HashMap();

    /**
     * @return
     */
    private static String spitAccessToken() {
        String accessToken = "";
//        new SecureAccountManager
//                .Builder(LeanRssApp.getInstance()).build()
//                .deObfuscateAcessToken()
//                .observeOn(Schedulers.computation())
//                .subscribe(new Consumer<String>() {
//                    @Override
//                    public void accept(String encrypted) throws Exception {
//                        getAccess(encrypted.toString());
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        Log.d(TAG, " on Error:" + throwable.getMessage());
//                    }
//                });
//        if (Access_Token.get("access_token") != null) {
//            accessToken = Access_Token.get("access_token");
//        }
        return accessToken;
    }

    /**
     * @param s
     * @return
     */
    private static String getAccess(String s) {
        Access_Token.put("access_token", s);
        return s;
    }
}


