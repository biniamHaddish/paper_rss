package com.biniisu.leanrss.models.feedbin;

import android.support.annotation.Keep;

import java.io.Serializable;

/**
 * Created by biniam on 3/1/17.
 */

@Keep
public class FeedBinTaggingsItem implements Serializable {
    /**
     * id : 4
     * feed_id : 1
     * name : Tech
     */

    private int id;
    private int feed_id;
    private String name;

    public FeedBinTaggingsItem(int id, int feed_id, String name) {
        this.id = id;
        this.feed_id = feed_id;
        this.name = name;
    }


    public int getTagId() {
        return id;
    }

    public String getSubscriptionId() {
        return String.valueOf(feed_id);
    }

    public String getTagName() {
        return name;
    }

    public void setTagName(String name) {
        this.name = name;
    }
}
