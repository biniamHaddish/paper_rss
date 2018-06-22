package com.biniisu.leanrss.connectivity.feedly;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.biniisu.leanrss.models.feedly.FeedlyOAuthData;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by biniam on 4/30/17.
 */

public class FeedlyOAuthAuthenticationActivity extends AppCompatActivity {

    public static final String FEEDLY_AUTH_PREF = "FEEDLY_AUTH_PREF";
    private final Gson gson = new Gson();
    public SharedPreferences feedlyOAuthPrefData;
    public FeedlyConstants feedlyConstants;
    // connect to feedly and get AccessToken
    private Intent mIntent;
    private Context context;
    private String feedly_accessToken = "";
    private String feedly_refreshToken = "";
    private String feedly_tokenType = "";
    private long feedly_expireTime = 0;
    private String feedly_Auth_Id = "";
    private String feedly_user_plan = "";
    private boolean isUserAuthenticated = false;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.feedly_auth);
//
//        context = FeedlyOAuthAuthenticationActivity.this;
//        mIntent = getIntent();
//        feedlyConstants = new FeedlyConstants();
//        // feedly pref init.
//        feedlyOAuthPrefData = getSharedPreferences(FEEDLY_AUTH_PREF, 0);
//        feedly_accessToken = feedlyOAuthPrefData.getString(getString(R.string.feedly_accessToken), null);
//        feedly_refreshToken = feedlyOAuthPrefData.getString(getString(R.string.feedly_refreshToken), null);
//        feedly_tokenType = feedlyOAuthPrefData.getString(getString(R.string.feedly_tokenType), null);
//        feedly_Auth_Id = feedlyOAuthPrefData.getString(getString(R.string.feedly_Auth_Id), null);
//        feedly_user_plan = feedlyOAuthPrefData.getString(getString(R.string.feedly_user_plan), null);
//        feedly_expireTime = feedlyOAuthPrefData.getLong(getString(R.string.feedly_expiry_time), 0);
//        isUserAuthenticated = feedlyOAuthPrefData.getBoolean(getString(R.string.isfeedlyUserAuthenticated), false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        FeedlyOAuthDataFetcher feedlyOAuthDataFetcher = new FeedlyOAuthDataFetcher(context);
        feedlyOAuthDataFetcher.execute();
    }

    public FeedlyOAuthData getOAuthData() {

        Response response = null;
        final Uri uri = mIntent.getData();
        if (uri.getQueryParameter("state").matches(FeedlyConstants.CSRF_PROTECTION_STRING)) {
            final FormBody formBody = new FormBody.Builder()
                    .addEncoded("code", uri.getQueryParameter("code"))
                    .addEncoded("client_id", FeedlyConstants.FEEDLY_CLIENT_ID)
                    .addEncoded("client_secret", FeedlyConstants.FEEDLY_CLIENT_SECRET)
                    .addEncoded("redirect_uri", FeedlyConstants.FEEDLY_REDIRECT_URI)
                    .addEncoded("state", FeedlyConstants.CSRF_PROTECTION_STRING)
                    .addEncoded("grant_type", "authorization_code")
                    .build();
            Log.d("Tokens", "post data: " + formBody.toString());
            Request request = new Request.Builder()
                    .header("content-type", "application/x-www-form-urlencoded")
                    .url(FeedlyConstants.getFeedlyOauthTokenUrl())
                    .post(formBody)
                    .build();
            try {
                response = client.newCall(request).execute();
                if (response != null && response.code() == 200) {
                   /* Log.d("feedlyOAuthData", "responseCode="+String.valueOf(response.code()));*/
                    String tokens = response.body().string();
                    Log.d("feedlyOAuthData", "Feedly tokens=" + tokens);
                    FeedlyOAuthData feedlyOAuthData = gson.fromJson(tokens, FeedlyOAuthData.class);
                    if (feedlyOAuthData != null) {
                        return feedlyOAuthData;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Feedly_AccessToken", "Feedly_AccessToken_Error=>\t" + e.getMessage());
            } finally {
                if (response != null)
                    response.body().close();
            }
        }
        return null;
    }

    private void saveFeedlyOAuthToPref() {
        // make sure the Access Token Does not expire before requesting
        FeedlyOAuthData feedlyOAuthData = null;
        feedlyOAuthData = getOAuthData();
        if (feedlyOAuthData != null) {

            feedlyOAuthPrefData.edit().putString(FeedlyConstants.FEEDLY_REFRESH_TOKEN_PREF_KEY, feedlyOAuthData.getRefresh_token()).apply();
            feedlyOAuthPrefData.edit().putString(FeedlyConstants.FEEDLY_ACCESS_TOKEN_PREF_KEY, feedlyOAuthData.getAccess_token()).apply();
            feedlyOAuthPrefData.edit().putString(FeedlyConstants.FEEDLY_TOKEN_TYPE_PREF_KEY, feedlyOAuthData.getToken_type()).apply();
            feedlyOAuthPrefData.edit().putLong(FeedlyConstants.FEEDLY_EXPIRY_TIME_PREF_KEY, feedlyOAuthData.getExpires_in()).apply();
            feedlyOAuthPrefData.edit().putString(FeedlyConstants.FEEDLY_AUTH_ID_PREF_KEY, feedlyOAuthData.getId()).apply();
            feedlyOAuthPrefData.edit().putString(FeedlyConstants.FEEDLY_USER_PLAN_PREF_KEY, feedlyOAuthData.getPlan()).apply();
            isUserAuthenticated = true;
            feedlyOAuthPrefData.edit().putBoolean(FeedlyConstants.FEEDLY_IS_USER_AUTHENTICATED_PREF_KEY, isUserAuthenticated).apply();
            //Utils.showToast(context, "User Authenticated !");
            //Utils.showToast(context, "UserID" + getFeedly_user_plan());
        }
    }

    public String getFeedly_accessToken() {
        return feedly_accessToken;
    }

    public String getFeedly_refreshToken() {
        return feedly_refreshToken;
    }

    public String getFeedly_tokenType() {
        return feedly_tokenType;
    }

    public long getFeedly_expireTime() {
        return feedly_expireTime;
    }

    public boolean isUserAuthenticated() {
        return isUserAuthenticated;
    }

    public String getFeedly_Auth_Id() {
        return feedly_Auth_Id;
    }

    public String getFeedly_user_plan() {
        return feedly_user_plan;
    }

    public class FeedlyOAuthDataFetcher extends AsyncTask<Void, String, Boolean> {
        ProgressDialog progressDialog;
        Context xcontext;

        FeedlyOAuthDataFetcher(Context context) {
            this.progressDialog = new ProgressDialog(context);
            this.xcontext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Authenticating user....");
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }


        @Override
        protected Boolean doInBackground(Void... voids) {
            saveFeedlyOAuthToPref();
            return false;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

        }


    }
}
