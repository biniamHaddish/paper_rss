package com.biniam.rss.models.feedWragler;

import java.util.List;

/**
 * Created by biniam on 1/2/18.
 */

public class FeedWranglerMultipleFeeds {


    /**
     * possible_feeds : [{"feed_url":"http://feeds.macrumors.com/MacRumors-All","title":"All Mac Rumors Headlines"},{"feed_url":"http://feeds.macrumors.com/MacRumors-Front","title":"Front Page Only"},{"feed_url":"http://feeds.macrumors.com/MacRumors-Mac","title":"Mac Blog Only"},{"feed_url":"http://feeds.macrumors.com/MacRumors-iPhone","title":"iPhone/iOS Blog Only"}]
     * error : null
     * result : success
     */

    private Object error;
    private String result;
    private List<PossibleFeedsBean> possible_feeds;

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

    public List<PossibleFeedsBean> getPossible_feeds() {
        return possible_feeds;
    }

    public void setPossible_feeds(List<PossibleFeedsBean> possible_feeds) {
        this.possible_feeds = possible_feeds;
    }

    public static class PossibleFeedsBean {
        /**
         * feed_url : http://feeds.macrumors.com/MacRumors-All
         * title : All Mac Rumors Headlines
         */

        private String feed_url;
        private String title;

        public String getFeed_url() {
            return feed_url;
        }

        public void setFeed_url(String feed_url) {
            this.feed_url = feed_url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
