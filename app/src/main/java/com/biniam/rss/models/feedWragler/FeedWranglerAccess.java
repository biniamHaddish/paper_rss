package com.biniam.rss.models.feedWragler;

import java.util.List;

/**
 * Created by biniam on 12/23/17.
 */

public class FeedWranglerAccess {


    /**
     * access_token : jvjvjhvjvhkghgh
     * user : {"email":"test","account_status":"active","read_later_service":"none"}
     * feeds : [{"title":"Daring Fireball","feed_id":290,"feed_url":"http://daringfireball.net/index.xml","site_url":"http://daringfireball.net/"}]
     * error : null
     * result : success
     */

    private String access_token;
    private UserBean user;
    private Object error;
    private String result;
    private List<FeedsBean> feeds;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
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

    public List<FeedsBean> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<FeedsBean> feeds) {
        this.feeds = feeds;
    }

    public static class UserBean {
        /**
         * email : test
         * account_status : active
         * read_later_service : none
         */

        private String email;
        private String account_status;
        private String read_later_service;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAccount_status() {
            return account_status;
        }

        public void setAccount_status(String account_status) {
            this.account_status = account_status;
        }

        public String getRead_later_service() {
            return read_later_service;
        }

        public void setRead_later_service(String read_later_service) {
            this.read_later_service = read_later_service;
        }
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
