package com.biniisu.leanrss.persistence.db.roomentities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.biniisu.leanrss.persistence.db.ReadablyDatabase;

import java.io.Serializable;

/**
 * Created by biniam_Haddish on 5/12/17.
 * <p>
 * This is a room entity that represents a feed item
 */

@Keep
@Entity(tableName = ReadablyDatabase.FEED_ITEMS_TABLE, foreignKeys = @ForeignKey(entity = SubscriptionEntity.class, parentColumns = "id", childColumns = "subscriptionId", onDelete = ForeignKey.CASCADE))
public class FeedItemEntity implements Serializable {

    @PrimaryKey
    @NonNull
    public String id;
    public String title;
    public String subscriptionId;
    public String excerpt;
    public String author;
    public String content;
    public String link;
    public String fullArticle;
    public String subscriptionName;
    public String leadImgPath;

    public long createdAt;
    public long published;
    public boolean read;
    public boolean favorite;
    public long syncedAt;
    public long modifiedAt;


    public FeedItemEntity(String title, String subscriptionId, String id, long published, String content, String excerpt, String link, String subscriptionName) {
        this.title = title;
        this.subscriptionId = subscriptionId;
        this.id = id;
        this.published = published;
        this.content = content;
        this.link = link;
        this.excerpt = excerpt;
        this.subscriptionName = subscriptionName;
    }

    public boolean hasFullArticle() {
        return fullArticle != null && !fullArticle.isEmpty();
    }
}
