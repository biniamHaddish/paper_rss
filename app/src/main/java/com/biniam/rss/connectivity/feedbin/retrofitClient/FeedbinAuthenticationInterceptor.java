package com.biniam.rss.connectivity.feedbin.retrofitClient;

import android.support.annotation.NonNull;
import android.util.Log;

import com.biniam.rss.utils.Constants;
import com.biniam.rss.utils.ReadablyApp;
import com.biniam.rss.utils.SecureAccountUtil.RxSecureStorage;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Cache;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by biniam on 12/15/17.
 */

public class FeedbinAuthenticationInterceptor {

    public static final String TAG = FeedbinAuthenticationInterceptor.class.getSimpleName();
    // Used for http caching
    private final static int CACHE_SIZE_BYTES = 1024 * 1024 * 2;
    private static HashMap<String, String> UserCredentialMap = new HashMap();

    static OkHttpClient customHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            spitCredentials();

            if (UserCredentialMap.isEmpty()) {
                Log.e(TAG, "Authorization header is already present or Credentials is empty....");
                return chain.proceed(chain.request());
            }

            Log.d(TAG, String.format("intercept: url is %s", chain.call().request().url().toString()));
            Request authorisedRequest = chain.request()
                    .newBuilder()
                    .addHeader("Authorization", Credentials.basic(UserCredentialMap.get(Constants.ENCRYPTED_KEY), UserCredentialMap.get(Constants.ENCRYPTED_KEY_PASS)))
                    .addHeader("Content-Type", "application/json")
                    .build();
            Log.e(TAG, "Authorization header is added to the url....");
            return chain.proceed(authorisedRequest);
        }
    }).cache(new Cache(ReadablyApp.getInstance().getApplicationContext().getCacheDir(), CACHE_SIZE_BYTES)).build();

    @NonNull
    private static void spitCredentials() {

        RxSecureStorage rxsecur = RxSecureStorage.create(ReadablyApp.getInstance(), ReadablyApp.getInstance().getPackageName());
        rxsecur.getString(Constants.ENCRYPTED_KEY)
                .subscribe(s -> {
                    //Log.d(TAG, String.format("username decrypted: %s\t", s));
                    UserCredentialMap.put(Constants.ENCRYPTED_KEY, s);
                });
        rxsecur.getString(Constants.ENCRYPTED_KEY_PASS)
                .subscribe(s -> {
                    //Log.d(TAG, String.format("password decrypted: %s\t", s));
                    UserCredentialMap.put(Constants.ENCRYPTED_KEY_PASS, s);
                });
    }
}

