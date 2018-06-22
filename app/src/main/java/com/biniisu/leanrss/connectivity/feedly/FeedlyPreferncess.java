package com.biniisu.leanrss.connectivity.feedly;

import android.content.Context;
import android.content.SharedPreferences;

import com.biniisu.leanrss.R;

/**
 * Created by biniam on 5/12/17.
 */

public class FeedlyPreferncess {

    public static final String FEEDLY_AUTH_PREF = "FEEDLY_AUTH_PREF";
    public SharedPreferences feedlyOAuthPrefData;
    private String feedly_accessToken = "";
    private String feedly_refreshToken = "";
    private String feedly_tokenType = "";
    private long feedly_expireTime = 0;
    private Context cxt;

    public FeedlyPreferncess(Context context) {
        context = cxt;
        feedlyOAuthPrefData = context.getSharedPreferences(FEEDLY_AUTH_PREF, 0);
        feedly_accessToken = feedlyOAuthPrefData.getString(FeedlyConstants.FEEDLY_ACCESS_TOKEN_PREF_KEY, null);
        feedly_refreshToken = feedlyOAuthPrefData.getString(FeedlyConstants.FEEDLY_REFRESH_TOKEN_PREF_KEY, null);
        feedly_tokenType = feedlyOAuthPrefData.getString(FeedlyConstants.FEEDLY_TOKEN_TYPE_PREF_KEY, null);
        feedly_expireTime = feedlyOAuthPrefData.getLong(FeedlyConstants.FEEDLY_EXPIRY_TIME_PREF_KEY, 0);
    }


}
