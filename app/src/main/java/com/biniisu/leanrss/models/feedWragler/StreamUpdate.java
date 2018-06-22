package com.biniisu.leanrss.models.feedWragler;

import java.util.List;

/**
 * Created by biniam on 1/3/18.
 */

public class StreamUpdate {

    /**
     * result : success
     * stream : {"stream_id":25,"title":"ExampleUpdated","all_feeds":true,"only_unread":true,"search_term":null,"feeds":[]}
     * error : null
     */

    private String result;
    private StreamBean stream;
    private Object error;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public StreamBean getStream() {
        return stream;
    }

    public void setStream(StreamBean stream) {
        this.stream = stream;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public static class StreamBean {
        /**
         * stream_id : 25
         * title : ExampleUpdated
         * all_feeds : true
         * only_unread : true
         * search_term : null
         * feeds : []
         */

        private int stream_id;
        private String title;
        private boolean all_feeds;
        private boolean only_unread;
        private Object search_term;
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

        public Object getSearch_term() {
            return search_term;
        }

        public void setSearch_term(Object search_term) {
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
