package com.biniisu.leanrss.models.feedWragler;

/**
 * Created by biniam on 1/2/18.
 */

public class FeedWranglerFeed {

    /**
     * feed : {"title":"Cult of Mac","feed_id":339,"feed_url":"http://cultofmac.com.feedsportal.com/c/33797/f/606249/index.rss","site_url":null}
     * error : null
     * result : success
     */

    private FeedBean feed;
    private Object error;
    private String result;

    public FeedBean getFeed() {
        return feed;
    }

    public void setFeed(FeedBean feed) {
        this.feed = feed;
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

    public static class FeedBean {
        /**
         * title : Cult of Mac
         * feed_id : 339
         * feed_url : http://cultofmac.com.feedsportal.com/c/33797/f/606249/index.rss
         * site_url : null
         */

        private String title;
        private int feed_id;
        private String feed_url;
        private Object site_url;

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

        public Object getSite_url() {
            return site_url;
        }

        public void setSite_url(Object site_url) {
            this.site_url = site_url;
        }
    }
}
