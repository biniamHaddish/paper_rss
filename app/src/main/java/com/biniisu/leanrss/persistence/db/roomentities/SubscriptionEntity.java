package com.biniisu.leanrss.persistence.db.roomentities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.biniisu.leanrss.persistence.db.ReadablyDatabase;

import java.io.Serializable;

/**
 * Created by biniam_Haddish on 12/5/17.
 * <p>
 * This is a room entity that represents and a feed subscription
 */

@Keep
@Entity(tableName = ReadablyDatabase.SUBSCRIPTIONS_TABLE, indices = @Index("id"))
public class SubscriptionEntity implements Serializable, Parcelable {

    public static final Creator<SubscriptionEntity> CREATOR = new Creator<SubscriptionEntity>() {
        @Override
        public SubscriptionEntity createFromParcel(Parcel in) {
            return new SubscriptionEntity(in);
        }

        @Override
        public SubscriptionEntity[] newArray(int size) {
            return new SubscriptionEntity[size];
        }
    };
    @PrimaryKey
    @NonNull
    public String id;
    public String title;
    public String siteLink;
    public String rssLink;
    public String iconUrl;
    public long createdTimestamp;
    public long lastUpdatedTimestamp;

    public SubscriptionEntity() {
    }

    @Ignore
    protected SubscriptionEntity(Parcel in) {
        id = in.readString();
        title = in.readString();
        siteLink = in.readString();
        rssLink = in.readString();
        iconUrl = in.readString();
        createdTimestamp = in.readLong();
        lastUpdatedTimestamp = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(siteLink);
        parcel.writeString(rssLink);
        parcel.writeString(iconUrl);
        parcel.writeLong(createdTimestamp);
        parcel.writeLong(lastUpdatedTimestamp);
    }
}
