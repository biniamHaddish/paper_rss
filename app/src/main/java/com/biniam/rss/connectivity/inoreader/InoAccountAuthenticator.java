package com.biniam.rss.connectivity.inoreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.util.Log;

import com.biniam.rss.connectivity.inoreader.inoReaderApi.OAuthApi;
import com.biniam.rss.models.inoreader.TokenResponses;
import com.biniam.rss.persistence.preferences.InoReaderAccountPreferences;
import com.biniam.rss.utils.Constants;
import com.biniam.rss.utils.ReadablyApp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by biniam_Haddish on 10/19/17.
 */

public class InoAccountAuthenticator {

    public static final String RDIRECT_URL = "readably://oauth";
    private static final String TAG = InoAccountAuthenticator.class.getSimpleName();
    private static final String InoReaderUrl = "https://www.inoreader.com/oauth2/auth";
    private static String OPTIONAL_SCOPES = "read write";
    private static Context mContext;
    private static InoAccountAuthenticator inoAccountAuthenticator;
    public String accessToken;
    public String refreashToken;
    public long expireTime;
    private InoReaderAccountPreferences inoReaderAccountPreferences;

    /**
     * constructor
     *
     * @param context
     */
    private InoAccountAuthenticator(Context context) {
        mContext = context;
        inoReaderAccountPreferences = new InoReaderAccountPreferences(context);

        // inoReader prefers
        accessToken = inoReaderAccountPreferences.accessToken;
        refreashToken = inoReaderAccountPreferences.refreshToken;
        expireTime = inoReaderAccountPreferences.expireTime;
    }

    /**
     * @param context
     * @return
     */
    public static InoAccountAuthenticator getInstance(Context context) {
        if (inoAccountAuthenticator == null) {
            inoAccountAuthenticator = new InoAccountAuthenticator(context);
        }
        return inoAccountAuthenticator;
    }

    /**
     * return login Url encoded
     * @return
     */
    public String loginUrl() {
        try {
            return InoReaderUrl +
                    "?client_id=" + URLEncoder.encode(Constants.InoReader_APP_ID, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(RDIRECT_URL, "UTF-8") +
                    "&state=" + URLEncoder.encode(Constants.CSRF_STRING, "UTF-8") +
                    "&scope=" + URLEncoder.encode(OPTIONAL_SCOPES, "UTF-8") +
                    "&response_type=code";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d("encoding", e.getMessage().toString());
        }
        return "";
    }

    /**
     * open chrome tabs
     * @param url
     */
    public void openWebPage(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        customTabsIntent.launchUrl(ReadablyApp.getInstance(), Uri.parse(url));
    }

    /**
     * It will renew the access Token when if expired
     */
    public void renewToken() {

        new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver completableObserver) {

                HashMap<String, String> queryMap = new HashMap<>();
                queryMap.put("client_id", Constants.InoReader_APP_ID);
                queryMap.put("client_secret", Constants.CLIENT_SECRET);
                queryMap.put("grant_type", "refresh_token");
                queryMap.put("refresh_token", refreashToken);

                OkHttpClient client = new OkHttpClient.Builder()
                        .build();

                Retrofit resetOauth = new Retrofit.Builder()
                        .baseUrl("https://www.inoreader.com")
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();


                OAuthApi oAuthApi = resetOauth.create(OAuthApi.class);
                try {
                    TokenResponses tokenResponse = oAuthApi.oauthToken(queryMap).execute().body();
                    if (tokenResponse != null) {
                        inoReaderAccountPreferences.accessToken = tokenResponse.getAccessToken();
                        inoReaderAccountPreferences.refreshToken = tokenResponse.getRefreshToken();
                        inoReaderAccountPreferences.tokenType = tokenResponse.getTokenType();
                        inoReaderAccountPreferences.expireTime = tokenResponse.getExpiresIn();
                        inoReaderAccountPreferences.isAuthenticated = true;

                        // setting  up the data from InoReader Service to preference file
                        inoReaderAccountPreferences.setString(InoReaderConstants.ACCESS_TOKEN, tokenResponse.getAccessToken());
                        inoReaderAccountPreferences.setString(InoReaderConstants.REFRESH_TOKEN, tokenResponse.getRefreshToken());
                        inoReaderAccountPreferences.setString(InoReaderConstants.TOKEN_TYPE, tokenResponse.getTokenType());
                        inoReaderAccountPreferences.setInt(InoReaderConstants.EXPIRES_IN, tokenResponse.getExpiresIn() + (System.currentTimeMillis() / 1000));
                        inoReaderAccountPreferences.setBoolean(InoReaderConstants.IS_AUTHENTICATED, true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.subscribeOn(Schedulers.computation())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "onError: " + throwable.getMessage());
                    }
                });
    }

}
