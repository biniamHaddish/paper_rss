package com.biniam.rss.models.feedWragler;

import java.util.List;

/**
 * Created by biniam on 1/3/18.
 */

public class StreamItems {

    /**
     * count : 2
     * feed_items : [{"feed_item_id":8046,"published_at":1367362441,"created_at":1367416779,"updated_at":1367864007,"version_key":1367864007,"url":"https://medium.com/what-i-learned-building/9216e1c9da7d","title":"The McDonald\u2019s Theory of Bad Ideas","starred":false,"read":true,"read_later":false,"body":"...","author":"John Gruber","feed_id":290,"feed_name":"Daring Fireball"},{"feed_item_id":8045,"published_at":1367380295,"created_at":1367416779,"version_key":1367867655,"updated_at":1367867655,"url":"http://daringfireball.net/2013/04/web_apps_native_apps","title":"★ Web Apps vs. Native Apps Is Still a Thing","starred":true,"read":true,"read_later":false,"body":"...","author":"John Gruber","feed_id":290,"feed_name":"Daring Fireball"}]
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
         * feed_item_id : 8046
         * published_at : 1367362441
         * created_at : 1367416779
         * updated_at : 1367864007
         * version_key : 1367864007
         * url : https://medium.com/what-i-learned-building/9216e1c9da7d
         * title : The McDonald’s Theory of Bad Ideas
         * starred : false
         * read : true
         * read_later : false
         * body : ...
         * author : John Gruber
         * feed_id : 290
         * feed_name : Daring Fireball
         */

        private long feed_item_id;
        private long published_at;
        private long created_at;
        private long updated_at;
        private long version_key;
        private String url;
        private String title;
        private boolean starred;
        private boolean read;
        private boolean read_later;
        private String body;
        private String author;
        private long feed_id;
        private String feed_name;

        public long getFeed_item_id() {
            return feed_item_id;
        }

        public void setFeed_item_id(long feed_item_id) {
            this.feed_item_id = feed_item_id;
        }

        public long getPublished_at() {
            return published_at;
        }

        public void setPublished_at(long published_at) {
            this.published_at = published_at;
        }

        public long getCreated_at() {
            return created_at;
        }

        public void setCreated_at(long created_at) {
            this.created_at = created_at;
        }

        public long getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(long updated_at) {
            this.updated_at = updated_at;
        }

        public long getVersion_key() {
            return version_key;
        }

        public void setVersion_key(long version_key) {
            this.version_key = version_key;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isStarred() {
            return starred;
        }

        public void setStarred(boolean starred) {
            this.starred = starred;
        }

        public boolean isRead() {
            return read;
        }

        public void setRead(boolean read) {
            this.read = read;
        }

        public boolean isRead_later() {
            return read_later;
        }

        public void setRead_later(boolean read_later) {
            this.read_later = read_later;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public long getFeed_id() {
            return feed_id;
        }

        public void setFeed_id(int feed_id) {
            this.feed_id = feed_id;
        }

        public String getFeed_name() {
            return feed_name;
        }

        public void setFeed_name(String feed_name) {
            this.feed_name = feed_name;
        }
    }
}
