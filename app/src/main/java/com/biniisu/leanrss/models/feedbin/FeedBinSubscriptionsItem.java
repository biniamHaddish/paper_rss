package com.biniisu.leanrss.models.feedbin;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by biniam on 2/27/17.
 */

@Keep
public class FeedBinSubscriptionsItem implements Serializable {
    /* subscription Items */
    @SerializedName("id")
    private int id;
    //@SerializedName("feedId")
    private int feed_id;
    private String created_at;
    private String title;
    private String feed_url;
    private String site_url;
    private String tag_name;
    // only used when you get the data from localDatabase
    private long created_at_timestamp;
    private long lastUpdatedTimestamp;

    public FeedBinSubscriptionsItem(int id, int feed_id, String created_at, String title, String feed_url, String site_url) {
        this.id = id;
        this.feed_id=feed_id;
        this.created_at = created_at;
        this.title = title;
        this.feed_url = feed_url;
        this.site_url = site_url;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getTitle() {
        return title;
    }

    public String getFeed_url() {
        return feed_url;
    }

    public String getSite_url() {
        return site_url;
    }

    public long getCreated_at_timestamp() {
        return created_at_timestamp;
    }

    // TODO: 8/23/17 add last updated timestamp, icon url, and icon path

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getFeed_id() {
        return feed_id;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
