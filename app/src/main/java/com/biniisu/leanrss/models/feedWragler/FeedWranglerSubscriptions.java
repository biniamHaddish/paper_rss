package com.biniisu.leanrss.models.feedWragler;

import java.util.List;

/**
 * Created by biniam on 12/26/17.
 */

public class FeedWranglerSubscriptions {


    /**
     * feeds :[{"title":"Daring Fireball",
     * "feed_id":290,"feed_url":"http://daringfireball.net/index.xml",
     * "site_url":"http://daringfireball.net/"
     * }]
     * error : null
     * result : success
     */

    private List<FeedsBean> feeds;

    public List<FeedsBean> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<FeedsBean> feeds) {
        this.feeds = feeds;
    }

    public static class FeedsBean {
        /**
         * title : Daring Fireball
         * feed_id : 290
         * feed_url : http://daringfireball.net/index.xml
         * site_url : http://daringfireball.net/
         */

        private String title;
        private int feed_id;
        private String feed_url;
        private String site_url;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getFeed_id() {
            return feed_id;
        }

        public void setFeed_id(int feed_id) {
            this.feed_id = feed_id;
        }

        public String getFeed_url() {
            return feed_url;
        }

        public void setFeed_url(String feed_url) {
            this.feed_url = feed_url;
        }

        public String getSite_url() {
            return site_url;
        }

        public void setSite_url(String site_url) {
            this.site_url = site_url;
        }
    }
}
