package com.biniam.rss.connectivity.inoreader.inoReaderApi;

import android.content.Context;

import com.biniam.rss.connectivity.inoreader.InoAccountAuthenticator;
import com.biniam.rss.persistence.preferences.InoReaderAccountPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by biniam on 2/19/18.
 * <p>
 * interceptor for Auth_2.0 Authentication
 */

public class HeaderInterceptor extends OkHttpClient implements Interceptor {

    public static final String TAG = HeaderInterceptor.class.getSimpleName();
    private InoReaderAccountPreferences inoReaderAccountPreferences;
    private InoAccountAuthenticator inoAccountAuthenticator;


    private long expireTime;
    private String tokenType;
    private String accessToken;

    public HeaderInterceptor(Context context) {

        inoReaderAccountPreferences = new InoReaderAccountPreferences(context);
        inoAccountAuthenticator = InoAccountAuthenticator.getInstance(context);
        //inoreader pref values
        expireTime = inoReaderAccountPreferences.expireTime;
        tokenType = inoReaderAccountPreferences.tokenType;
        accessToken = inoReaderAccountPreferences.accessToken;
        if (isAccessTokenExpired()) {
            inoAccountAuthenticator.renewToken();
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder;
        Request request = chain.request();
        builder = request.newBuilder()
                .addHeader("Authorization",
                        tokenType + " " + accessToken);
        return chain.proceed(builder.build());
    }

    /**
     * will check to see if the Expire time is still Valid
     */
    private boolean isAccessTokenExpired() {
        return !(expireTime > 0 && (System.currentTimeMillis() / 1000) > expireTime);
    }

}
