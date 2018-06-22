package com.biniisu.leanrss.connectivity.feedbin.retrofitClient;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by biniam on 11/12/17.
 */

public class AuthenticationInterceptor implements Interceptor {

    private String authToken;

    public AuthenticationInterceptor(String token) {
        this.authToken = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header("Authorization", authToken);

        Log.d("Authentication", String.format("intercept: token is %s", authToken));

        Request request = builder.build();
        return chain.proceed(request);
    }
}