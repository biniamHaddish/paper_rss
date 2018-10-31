package com.biniam.rss.persistence.db.roomentities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.biniam.rss.persistence.db.PaperDatabase;

import java.io.Serializable;

/**
 * Created by biniam_haddish on 2/20/17.
 * <p>
 * This a room entity to represent a subscriptions folder
 */

@Keep
@Entity(tableName = PaperDatabase.TAGS_TABLE,
        foreignKeys = @ForeignKey(entity = SubscriptionEntity.class, parentColumns = "id", childColumns = "subscriptionId", onDelete = ForeignKey.CASCADE),
        primaryKeys = {"subscriptionId", "name"},
        indices = @Index("subscriptionId"))
public class TagEntity implements Serializable {

    @NonNull
    public String subscriptionId;
    @NonNull
    public String name;
    public String serverId;  // This id represents the id this tag is represented with in services such as feedbin

    public TagEntity() {
    }

    @Ignore
    public TagEntity(@NonNull String subscriptionId, @NonNull String name) {
        this.subscriptionId = subscriptionId;
        this.name = name;
    }

    @Ignore
    public TagEntity(@NonNull String serverId, @NonNull String subscriptionId, @NonNull String name) {
        this.serverId = serverId;
        this.subscriptionId = subscriptionId;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
