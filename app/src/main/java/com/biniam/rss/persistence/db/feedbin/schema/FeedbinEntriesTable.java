package com.biniam.rss.persistence.db.feedbin.schema;
/**
 * Created by biniam on 7/3/17.
 */

public class FeedbinEntriesTable {
    /*will carry all the Entries in here */

    public static final String TABLE_NAME          ="feedbin_feed_entries_tbl";
    public static final String FEEDBIN_ENTRY_ID    ="entry_id";
    public static final String FEED_ID             ="feed_id";
    public static final String FEED_TITLE          ="title";
    public static final String FEED_URL            ="feed_url";
    public static final String FEED_SITE_URL       ="site_url";
    public static final String AUTHOR              ="author";
    public static final String CONTENT             ="content";
    public static final String SUMMARY             ="summary";
    public static final String PUBLISHED           ="published";
    public static final String CREATED_AT          ="created_at";
    public static final String READ_ENTRY = "read_entry";
    public static final String STARRED_ENTRY = "starred_entry";



    public static final String[] projection = new String[]{
            FEEDBIN_ENTRY_ID,
            FEED_ID,
            FEED_TITLE,
            FEED_URL,
            FEED_SITE_URL,
            AUTHOR,
            CONTENT,
            SUMMARY,
            PUBLISHED,
            CREATED_AT,
            READ_ENTRY,
            STARRED_ENTRY
    };
    public static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +

            " (" +
            FEEDBIN_ENTRY_ID + " INTEGER PRIMARY KEY, " +
            FEED_ID + " INTEGER NOT NULL, " +
            FEED_TITLE + " TEXT NOT NULL, " +
            FEED_URL+ " TEXT NOT NULL, " +
            FEED_SITE_URL + " TEXT, " +
            AUTHOR + " TEXT, " +
            CONTENT+ " TEXT NOT NULL, " +
            SUMMARY+ " TEXT NOT NULL, " +
            PUBLISHED + " INTEGER NOT NULL, " +
            CREATED_AT + " INTEGER NOT NULL, " +
            READ_ENTRY + " INTEGER  DEFAULT 0, " +
            STARRED_ENTRY + " INTEGER DEFAULT 0 " +
            ")";
}
