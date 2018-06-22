package com.biniisu.leanrss.persistence.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.biniisu.leanrss.persistence.db.roomentities.FeedItemEntity;
import com.biniisu.leanrss.persistence.db.roomentities.SubscriptionEntity;
import com.biniisu.leanrss.persistence.db.roomentities.TagEntity;

/**
 * Created by biniam_Haddish on 11/27/17.
 * <p>
 * Room database wrapper to create all tables
 */

@Database(entities = {SubscriptionEntity.class, FeedItemEntity.class, TagEntity.class}, version = 2, exportSchema = false)
public abstract class ReadablyDatabase extends RoomDatabase {

    public static final String FEED_ITEMS_TABLE = "feed_items";
    public static final String SUBSCRIPTIONS_TABLE = "subscriptions";
    public static final String TAGS_TABLE = "tags";

    public abstract ReadablyDAO dao();


}
