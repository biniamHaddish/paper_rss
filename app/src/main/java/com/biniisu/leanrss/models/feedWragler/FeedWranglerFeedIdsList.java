package com.biniisu.leanrss.models.feedWragler;

import java.util.List;

/**
 * Created by biniam on 1/4/18.
 */

public class FeedWranglerFeedIdsList {

    /**
     * count : 3
     * feed_items : [{"feed_item_id":8194},{"feed_item_id":8192},{"feed_item_id":8190}]
     * error : null
     * result : success
     */

    private int count;
    private Object error;
    private String result;
    private List<FeedItemsBean> feed_items;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<FeedItemsBean> getFeed_items() {
        return feed_items;
    }

    public void setFeed_items(List<FeedItemsBean> feed_items) {
        this.feed_items = feed_items;
    }

    public static class FeedItemsBean {
        /**
         * feed_item_id : 8194
         */

        private long feed_item_id;

        public long getFeed_item_id() {
            return feed_item_id;
        }

        public void setFeed_item_id(long feed_item_id) {
            this.feed_item_id = feed_item_id;
        }
    }
}
