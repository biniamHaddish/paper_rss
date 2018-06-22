package com.biniam.rss.connectivity.inoreader;

/**
 * Created by biniam on 3/23/17.
 */

public class InoReaderUrls {

    private static final String BASE_URL = "https://www.inoreader.com";
    private static final String TOKEN_URL = "oauth2/token";
    private static final String USER_INFO_URL = "https://www.inoreader.com/reader/api/0/user-info";
    private static final String EDIT_SUBSCRIPTION_URL = "https://www.inoreader.com/reader/api/0/subscription/edit";
    private static final String UNREAD_COUNT_URL = "https://www.inoreader.com/reader/api/0/unread-count";
    private static final String STREAM_CONTENT_URL = "https://www.inoreader.com/reader/api/0/stream/contents";
    private static final String ITEAM_ID_URL = "https://www.inoreader.com/reader/api/0/stream/items/ids";
    private static final String TAG_FOLDER_LIST_URL = "https://www.inoreader.com/reader/api/0/tag/list";
    private static final String RENAME_TAGE_URL = "https://www.inoreader.com/reader/api/0/rename-tag";
    private static final String DELETE_TAGE_URL = "https://www.inoreader.com/reader/api/0/disable-tag";
    private static final String SUBSCRIPTION_LIST_URL = "https://www.inoreader.com/reader/api/0/subscription/list";
    private static final String SUBSCRIPTION_URL = "https://www.inoreader.com/reader/api/0/subscription/quickadd";
    private static final String QUERY_All_ARTICLES = "user/-/state/com.google/reading-list";

    private static final String QUERY_READ_ARTICLES = "user/-/state/com.google/read";// ARTICLES THAT HAS BEEN READ
    private static final String QUERY_STARRED_ARTICLES = "user/-/state/com.google/starred";
    private static final String QUERY_BROADCASTED_ARTICLES = "user/-/state/com.google/broadcast";
    private static final String QUERY_LIKED_ARTICLES = "user/-/state/com.google/like";


    public static String getDeleteTageUrl() {
        return DELETE_TAGE_URL;
    }

    public static String getRenameTageUrl() {
        return RENAME_TAGE_URL;
    }

    public static String getQUERY_All_ARTICLES() {
        return QUERY_All_ARTICLES;
    }

    public static String getQueryReadArticles() {
        return QUERY_READ_ARTICLES;
    }

    public static String getQueryStarredArticles() {
        return QUERY_STARRED_ARTICLES;
    }

    public static String getQueryBroadcastedArticles() {
        return QUERY_BROADCASTED_ARTICLES;
    }

    public static String getQueryLikedArticles() {
        return QUERY_LIKED_ARTICLES;
    }

    public static String getStreamContentUrl() {
        return STREAM_CONTENT_URL;
    }

    public static String getIteamIdUrl() {
        return ITEAM_ID_URL;
    }

    public static String getTagFolderListUrl() {
        return TAG_FOLDER_LIST_URL;
    }

    public static String getUnreadCountUrl() {
        return UNREAD_COUNT_URL;
    }

    public static String getSubscriptionListUrl() {
        return SUBSCRIPTION_LIST_URL;
    }

    public static String getUserInfoUrl() {
        return USER_INFO_URL;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getEditSubscriptionUrl() {
        return EDIT_SUBSCRIPTION_URL;
    }

    public static String getSubscriptionUrl() {
        return SUBSCRIPTION_URL;
    }

    public static String getTokenUrl() {
        return TOKEN_URL;
    }

}
