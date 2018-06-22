package com.biniam.rss.connectivity.feedbin.feedbinUtils;

/**
 * Created by biniam on 2/3/17.
 */

public class FeedbinUrls {
    private static final String BASE_URL = "https://api.feedbin.com/v2/";
    private static final String Authentication_URL = BASE_URL + "authentication.json";
    private static final String SUBSCRIPTION_ADD_URL = BASE_URL + "subscriptions/";
    private static final String SUBSCRIPTION_URL = BASE_URL + "subscriptions.json";
    private static final String ENTRIES_URL = BASE_URL + "entries.json";
    private static final String ENTRIES = BASE_URL + "entries/";
    private static final String TAG_DELETE_URL = BASE_URL + "taggings/";
    private static final String SUBSCRIPTION_UPDATE = BASE_URL + "subscriptions/";
    private static final String FEED_URL = "https://api.feedbin.mefeeds/";
    private static final String TAG_URL = BASE_URL + "taggings.json";
    private static final String TAG_CREATE_URL = BASE_URL + "taggings.json";
    private static final String UNREAD_ENTRIES_URL = BASE_URL + "unread_entries.json";
    private static final String UNREAD_ENTRIES_IDS = BASE_URL + "entries.json?ids=";
    private static final String STARRED_URL = BASE_URL + "starred_entries.json";
    private static final String STARRED_DELETE_URL = BASE_URL + "starred_entries/delete.json";
    private static final String SINGLE_TAG = BASE_URL + "taggings/";

    public FeedbinUrls() {
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getSubscriptionAddUrl() {
        return SUBSCRIPTION_ADD_URL;
    }

    public static String getSubscriptionUrl() {
        return SUBSCRIPTION_URL;
    }

    public static String getTagDeleteUrl() {
        return TAG_DELETE_URL;
    }

    public static String getSubscriptionUpdate() {
        return SUBSCRIPTION_UPDATE;
    }

    public static String getFeedUrl() {
        return FEED_URL;
    }

    public static String getENTRIES() {
        return ENTRIES;
    }

    public static String getSingleTag() {
        return SINGLE_TAG;
    }

    public static String getTagUrl() {
        return TAG_URL;
    }

    public static String getTagCreateUrl() {
        return TAG_CREATE_URL;
    }

    public static String getUnreadEntriesUrl() {
        return UNREAD_ENTRIES_URL;
    }

    public static String getUnreadEntriesIds() {
        return UNREAD_ENTRIES_IDS;
    }

    public static String getEntriesUrl() {
        return ENTRIES_URL;
    }

    public static String getStarredUrl() {
        return STARRED_URL;
    }

    public static String getStarredDeleteUrl() {
        return STARRED_DELETE_URL;
    }

    public static String getAuthentication_URL() {
        return Authentication_URL;
    }
}
