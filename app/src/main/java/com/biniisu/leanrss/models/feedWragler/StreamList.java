package com.biniisu.leanrss.models.feedWragler;

import java.util.List;

/**
 * Created by biniam on 1/3/18.
 */

public class StreamList {


    /**
     * streams : [{"stream_id":12,"title":"Simple Stream","all_feeds":true,"only_unread":false,"search_term":"Apple","feeds":[]},{"stream_id":11,"title":"Complex Stream","all_feeds":false,"only_unread":true,"search_term":"iPhone","feeds":[{"title":"Daring Fireball","feed_id":290,"feed_url":"http://daringfireball.net/index.xml"}]}]
     * error : null
     * result : success
     */

    private Object error;
    private String result;
    private List<StreamsBean> streams;

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

    public List<StreamsBean> getStreams() {
        return streams;
    }

    public void setStreams(List<StreamsBean> streams) {
        this.streams = streams;
    }

    public static class StreamsBean {
        /**
         * stream_id : 12
         * title : Simple Stream
         * all_feeds : true
         * only_unread : false
         * search_term : Apple
         * feeds : []
         */

        private int stream_id;
        private String title;
        private boolean all_feeds;
        private boolean only_unread;
        private String search_term;
        private List<?> feeds;

        public int getStream_id() {
            return stream_id;
        }

        public void setStream_id(int stream_id) {
            this.stream_id = stream_id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isAll_feeds() {
            return all_feeds;
        }

        public void setAll_feeds(boolean all_feeds) {
            this.all_feeds = all_feeds;
        }

        public boolean isOnly_unread() {
            return only_unread;
        }

        public void setOnly_unread(boolean only_unread) {
            this.only_unread = only_unread;
        }

        public String getSearch_term() {
            return search_term;
        }

        public void setSearch_term(String search_term) {
            this.search_term = search_term;
        }

        public List<?> getFeeds() {
            return feeds;
        }

        public void setFeeds(List<?> feeds) {
            this.feeds = feeds;
        }
    }
}
