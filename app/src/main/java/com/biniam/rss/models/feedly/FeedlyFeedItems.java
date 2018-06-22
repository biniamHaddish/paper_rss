package com.biniam.rss.models.feedly;

import java.util.List;

/**
 * Created by biniam on 5/11/17.
 */

public class FeedlyFeedItems {

    /**
     * website : http://www.engadget.com/
     * velocity : 180.3
     * topics : ["tech","gadgets"]
     * featured : false
     * sponsored : false
     * title : Engadget
     * id : feed/http://feeds.engadget.com/weblogsinc/engadget
     * state : alive
     * subscribers : 123
     * curated : false
     * language : en
     */

    private String website;
    private double velocity;
    private boolean featured;
    private boolean sponsored;
    private String title;
    private String id;
    private String state;
    private int subscribers;
    private boolean curated;
    private String language;
    private List<String> topics;

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

    public boolean isSponsored() {
        return sponsored;
    }

    public void setSponsored(boolean sponsored) {
        this.sponsored = sponsored;
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

    public boolean isCurated() {
        return curated;
    }

    public void setCurated(boolean curated) {
        this.curated = curated;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {

        this.topics = topics;
    }
}
