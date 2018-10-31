package com.biniam.rss.utils;

import com.biniam.rss.BuildConfig;

/**
 * Created by biniam on 7/22/17.
 */

public class Constants {

    /*API KEYS*/

    public static final String MERCURY_API_KEY  = BuildConfig.MERCURY_API_KEY;
    public static final String YOUTUBE_API_KEY  = BuildConfig.YOUTUBE_API_KEY;
    public static final String FEEDWRANGLER_CLIENT_KEY = BuildConfig.FEEDWRANGLER_CLIENT_KEY;

    public static final String InoReader_APP_ID = BuildConfig.InoReader_APP_ID;
    public static final String CLIENT_SECRET    = BuildConfig.CLIENT_SECRET;
    public static final String CSRF_STRING      = BuildConfig.CSRF_STRING;



    /*preference*/
    public static final String CUSTOM_PREFRENCES = "CUSTOM_PREFRENCES";
    public static final String ACCOUNT_MANAGER_PREFERENCES = "ACCOUNT_MANAGER_PREFERNCES";

    //keyStore init vectors
    public static final String ENCRYPTED_KEY = "ENCRYPTED_KEY";
    public static final String ENCRYPTED_KEY_PASS = "ENCRYPTED_KEY_PASS";
    public static final String ENCRYPTED_ACCESS_TOKEN = "ENCRYPTED_ACCESS_TOKEN";

    public static final int NOTIFICATION_ID = 2340156;
    public static final int READ_RATE_PROMPT_ARTICLE_COUNT = 100;
}
