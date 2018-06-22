package com.biniisu.leanrss.persistence.db.feedbin.schema;

/**
 * Created by biniam on 7/12/17.
 */

public class FeedbinTaggedSubscriptionsTable {

    public static final String TABLE_NAME="feedbin_tagged_subscription";
    public static final String TAG_ID="id";
    public static final String TAG_NAME="tag_name";
    public static final String FEED_ID="feed_id";


    public static final String[] projection = new String[]{
            TAG_ID,
            FEED_ID,
            TAG_NAME
    };

    /*Create Tagged SubscriptionEntity Table */

    public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS "  + TABLE_NAME +
            " (" +
            TAG_ID+" INTEGER PRIMARY KEY, " +
            FEED_ID +" INTEGER NOT NULL, "+
            TAG_NAME+ " TEXT NOT NULL, " +
            "FOREIGN KEY (" +
            FEED_ID +
            ")" +
            "REFERENCES " +
            FeedbinFeedSubscriptionTable.TABLE_NAME +
            "(" +
            FeedbinFeedSubscriptionTable.FEED_ID +
            "));";
}
