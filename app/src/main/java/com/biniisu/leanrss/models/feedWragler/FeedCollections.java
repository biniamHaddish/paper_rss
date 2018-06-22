package com.biniisu.leanrss.models.feedWragler;

import java.util.List;

/**
 * Created by biniam on 1/2/18.
 */

public class FeedCollections {

    /**
     * count : 2
     * feed_items : [{"feed_item_id":8194,"published_at":1367873228,"created_at":1367956808,"version_key":1367960870,"updated_at":1367960870,"url":"http://blogs.adobe.com/creative-cloud-members-coming-soon.html","title":"Photoshop CC","starred":true,"read":true,"read_later":true,"body":"...","author":"John Gruber","feed_id":290,"feed_name":"Daring Fireball"},{"feed_item_id":8045,"published_at":1367380295,"created_at":1367416779,"version_key":1367940419,"updated_at":1367940419,"url":"http://daringfireball.net/2013/04/web_apps_native_apps","title":"★ Web Apps vs. Native Apps Is Still a Thing","starred":true,"read":true,"read_later":false,"body":"...","author":"John Gruber","feed_id":290,"feed_name":"Daring Fireball"}]
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
         * published_at : 1367873228
         * created_at : 1367956808
         * version_key : 1367960870
         * updated_at : 1367960870
         * url : http://blogs.adobe.com/creative-cloud-members-coming-soon.html
         * title : Photoshop CC
         * starred : true
         * read : true
         * read_later : true
         * body : ...
         * author : John Gruber
         * feed_id : 290
         * feed_name : Daring Fireball
         */

        private int feed_item_id;
        private int published_at;
        private int created_at;
        private int version_key;
        private int updated_at;
        private String url;
        private String title;
        private boolean starred;
        private boolean read;
        private boolean read_later;
        private String body;
        private String author;
        private int feed_id;
        private String feed_name;

        public int getFeed_item_id() {
            return feed_item_id;
        }

        public void setFeed_item_id(int feed_item_id) {
            this.feed_item_id = feed_item_id;
        }

        public int getPublished_at() {
            return published_at;
        }

        public void setPublished_at(int published_at) {
            this.published_at = published_at;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getVersion_key() {
            return version_key;
        }

        public void setVersion_key(int version_key) {
            this.version_key = version_key;
        }

        public int getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(int updated_at) {
            this.updated_at = updated_at;
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

        public int getFeed_id() {
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
