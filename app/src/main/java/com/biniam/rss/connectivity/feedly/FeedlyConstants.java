package com.biniam.rss.connectivity.feedly;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by biniam on 4/30/17.
 */

public class FeedlyConstants {

    public static final String FEEDLY_ACCESS_TOKEN_PREF_KEY = "feedly_accessToken";
    public static final String FEEDLY_REFRESH_TOKEN_PREF_KEY = "feedly_refreshToken";
    public static final String FEEDLY_TOKEN_TYPE_PREF_KEY = "feedly_tokenType";
    public static final String FEEDLY_EXPIRY_TIME_PREF_KEY = "feedly_expiry_time";
    public static final String FEEDLY_AUTH_ID_PREF_KEY = "feedly_Auth_Id";
    public static final String FEEDLY_USER_PLAN_PREF_KEY = "feedly_user_plan";
    public static final String FEEDLY_IS_USER_AUTHENTICATED_PREF_KEY = "isfeedlyUserAuthenticated";

    public static final String CSRF_PROTECTION_STRING = "xhdkjhfkdhfkhskhfksf98090sfjk";

    public static final String FEEDLY_BASE_URL = "https://sandbox.feedly.com"; // this is only for Development purpose
    public static final String FEEDLY_OAUTH_URL = FEEDLY_BASE_URL + "/v3/auth/auth";
    public static final String FEEDLY_OAUTH_TOKEN_URL = FEEDLY_BASE_URL + "/v3/auth/token";
    public static final String FEEDLY_USER_PROFILE_URL = FEEDLY_BASE_URL + "/v3/profile";
    public static final String FEEDLY_CATAGORIES_URL = FEEDLY_BASE_URL + "/v3/categories";//Get Method;
    public static final String FEEDLY_FEED_SEARCH_URL = FEEDLY_BASE_URL + "/v3/search/feeds";//Get Method;
    public static final String FEEDLY_FEED_URL = FEEDLY_BASE_URL + "/v3/feeds";//Get Method;
    public static final String FEEDLY_FEED_LIST_URL = FEEDLY_BASE_URL + "/v3/feeds/.mget";//POST Method;
    public static final String FEEDLY_SUBSCRIPTION_URL = FEEDLY_BASE_URL + "/v3/subscriptions";
    public static final String FEEDLY_ENTRIES_URL = FEEDLY_BASE_URL + "/v3/entries";
    public static final String FEEDLY_ENTRIES_ID_URL = FEEDLY_BASE_URL + "/v3/streams";
    public static final String FEEDLY_DYNAMIC_ENTRY_LIST_URL = FEEDLY_BASE_URL + "/v3/entries/.mget";
    public static final String FEEDLY_COUNT_URL = FEEDLY_BASE_URL + "/v3/markers/counts";
    public static final String FEEDLY_MARK_AS_READ_URL = FEEDLY_BASE_URL + "/v3/markers";
    public static final String FEEDLY_GLOBAL_RESOURCE_ID_URL = FEEDLY_BASE_URL + "/v3/#global-resource-ids";
    public static final String FEEDLY_CLIENT_ID = "sandbox";//" c3a79326-033c-467a-913e-981658bc2f68";

    //http://developer.feedly.com/v3/#global-resource-ids
    public static final String FEEDLY_CLIENT_SECRET = "PYZR7RLWRM2JW0ESBY2E";
    public static final String FEEDLY_SCOPE = "https://cloud.feedly.com/subscriptions";
    public static final String FEEDLY_REDIRECT_URI = "https://biniisurss.androidapp";
    public static final String FEEDLY_ACCESS_TOKEN = "A1vNTriZKY1h1RHWhZiwXGK1YQMercae1jGUWzrxivd_nQCvObz4_nWMeds69PRpZJ0d8gOwxCTEPAD6iqbcJviua9AEWrXw9AvZLK7JbLUPRLbebL8-Z969SP5WBmqTmIrZ_-4LfyGtzizBAHgSNEWVZVrdj6N3nh3MX9FnzNzdPjtZmHFTYLLW1UQPB_ljt35X1jRM-AH_SW0Bv5yYNKECUW1Y:feedlydev";

    // ACCESS TOKEN EXPIRES SO REFRESH IT ONCE IN A WHILE THE BELLOW IS FOR JSUT FOR ONE MONTH
    private String finalFeedlyOAuthUrl = "";

    public static String getFeedlySubscriptionUrl() {
        return FEEDLY_SUBSCRIPTION_URL;
    }

    public static String getFeedlyBaseUrl() {
        return FEEDLY_BASE_URL;
    }

    public static String getFeedlyUserProfileUrl() {
        return FEEDLY_USER_PROFILE_URL;
    }

    public static String getFeedlyDynamicEntryListUrl() {
        return FEEDLY_DYNAMIC_ENTRY_LIST_URL;
    }

    public static String getFeedlyCountUrl() {
        return FEEDLY_COUNT_URL;
    }

    public static String getFeedlyClientId() {
        return FEEDLY_CLIENT_ID;
    }

    public static String getFeedlyClientSecret() {
        return FEEDLY_CLIENT_SECRET;
    }

    public static String getFeedlyScope() {
        return FEEDLY_SCOPE;
    }

    public static String getFeedlyRedirectUri() {
        return FEEDLY_REDIRECT_URI;
    }

    public static String getFeedlyAccessToken() {
        return FEEDLY_ACCESS_TOKEN;
    }

    public static String getFeedlyFeedListUrl() {
        return FEEDLY_FEED_LIST_URL;
    }

    public static String getFeedlyEntriesUrl() {
        return FEEDLY_ENTRIES_URL;
    }

    public static String getFeedlyGlobalResourceIdUrl() {
        return FEEDLY_GLOBAL_RESOURCE_ID_URL;
    }

    public static String getFeedlyFeedUrl() {
        return FEEDLY_FEED_URL;
    }

    public static String getFeedlyOauthTokenUrl() {
        return FEEDLY_OAUTH_TOKEN_URL;
    }

    public static String getFeedlyOauthUrl() {
        return FEEDLY_OAUTH_URL;
    }

    public static String getFeedlyCatagoriesUrl() {
        return FEEDLY_CATAGORIES_URL;
    }

    public static String getFeedlyEntriesIdUrl() {
        return FEEDLY_ENTRIES_ID_URL;
    }

    public static String getFeedlyMarkAsReadUrl() {
        return FEEDLY_MARK_AS_READ_URL;
    }

    public static String getFeedlyFeedSearchUrl() {
        return FEEDLY_FEED_SEARCH_URL;
    }

    public String spitFeedlyOAuthUrl() {
        try {
            finalFeedlyOAuthUrl = getFeedlyOauthUrl() +
                    "?client_id=" + URLEncoder.encode(FEEDLY_CLIENT_ID, "utf-8") +
                    "&redirect_uri=" + URLEncoder.encode(FEEDLY_REDIRECT_URI, "utf-8") +
                    "&response_type=code" +
                    "&scope=" + URLEncoder.encode(FEEDLY_SCOPE, "utf-8") +
                    "&state=" + URLEncoder.encode(CSRF_PROTECTION_STRING, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d("encoding", e.getMessage().toString());
        }
        return finalFeedlyOAuthUrl;

    }

}
