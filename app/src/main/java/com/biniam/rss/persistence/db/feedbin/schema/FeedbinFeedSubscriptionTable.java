package com.biniam.rss.persistence.db.feedbin.schema;

/**
 * Created by biniam on 6/16/17.
 */

public class FeedbinFeedSubscriptionTable {

    public static final String TABLE_NAME="feedbin_feed_subscription";
    public static final String SUBSCRIPTION_ID="id";
    public static final String FEED_ID="feed_id";
    public static final String CREATED_AT="created_at";
    public static final String TITLE="title";
    public static final String FEED_URL="feed_url";
    public static final String SITE_URL="site_url";
    public static final String TAG_NAME = "tag_name";


    public static final String[] projection = new String[]{
            SUBSCRIPTION_ID,
            FEED_ID,
            CREATED_AT,
            TITLE,
            FEED_URL,
            SITE_URL,
            TAG_NAME
    };
    /*creating subscription table */
    public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            SUBSCRIPTION_ID + " INTEGER PRIMARY KEY, " +
            FEED_ID + " INTEGER NOT NULL, " +
            CREATED_AT + " INTEGER, " +
            TITLE + " TEXT NOT NULL, " +
            FEED_URL+" TEXT NOT NULL, " +
            SITE_URL + " TEXT NOT NULL, " +
            TAG_NAME + " TEXT, " +
            "FOREIGN KEY (" +
            TAG_NAME + ") " +
            "REFERENCES " +
            FeedbinTaggedSubscriptionsTable.TABLE_NAME +
            "(" +
            FeedbinTaggedSubscriptionsTable.TAG_NAME + "));";
}
