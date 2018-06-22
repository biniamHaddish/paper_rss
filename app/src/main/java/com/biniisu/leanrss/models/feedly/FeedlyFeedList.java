package com.biniisu.leanrss.models.feedly;

import java.util.List;

/**
 * Created by biniam on 5/11/17.
 */

public class FeedlyFeedList {

    /**
     * website : http://www.engadget.com/
     * velocity : 180.3
     * topics : ["tech","gadgets"]
     * featured : true
     * title : Engadget
     * id : feed/http://feeds.engadget.com/weblogsinc/engadget
     * state : alive
     * subscribers : 123
     * suggestions : [{"title":"Yatzer","id":"feed/http://www.yatzer.com/feed/index.php"}]
     */

    private String website;
    private double velocity;
    private boolean featured;
    private String title;
    private String id;
    private String state;
    private int subscribers;
    private List<String> topics;
    private List<SuggestionsBean> suggestions;

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(int subscribers) {
        this.subscribers = subscribers;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<SuggestionsBean> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<SuggestionsBean> suggestions) {
        this.suggestions = suggestions;
    }

    public static class SuggestionsBean {
        /**
         * title : Yatzer
         * id : feed/http://www.yatzer.com/feed/index.php
         */

        private String title;
        private String id;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
