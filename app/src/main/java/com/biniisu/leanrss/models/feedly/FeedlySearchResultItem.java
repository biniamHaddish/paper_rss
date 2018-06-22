package com.biniisu.leanrss.models.feedly;

import java.util.List;

/**
 * Created by biniam on 5/14/17.
 */

public class FeedlySearchResultItem {


    /**
     * hint : apple
     * related : ["tech","technology","osx","macintosh","mac"]
     * results : [{"website":"http://daringfireball.net/","velocity":47.8,"featured":true,"title":"Daring Fireball","feedId":"feed/http://daringfireball.net/index.xml","curated":true,"subscribers":359471},{"website":"http://www.macrumors.com","velocity":36.4,"featured":true,"title":"MacRumors","feedId":"feed/http://www.macrumors.com/macrumors.xml","curated":true,"subscribers":322765},{"website":"http://gigaom.com","velocity":22.9,"featured":true,"title":"The Apple Blog","feedId":"feed/http://theappleblog.com/feed/","curated":true,"subscribers":118576}]
     */

    private String hint;
    private List<String> related;
    private List<ResultsBean> results;

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public List<String> getRelated() {
        return related;
    }

    public void setRelated(List<String> related) {
        this.related = related;
    }

    public List<ResultsBean> getResults() {
        return results;
    }

    public void setResults(List<ResultsBean> results) {
        this.results = results;
    }

    public static class ResultsBean {
        /**
         * website : http://daringfireball.net/
         * velocity : 47.8
         * featured : true
         * title : Daring Fireball
         * feedId : feed/http://daringfireball.net/index.xml
         * curated : true
         * subscribers : 359471
         */

        private String website;
        private double velocity;
        private boolean featured;
        private String title;
        private String feedId;
        private boolean curated;
        private int subscribers;

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

        public String getFeedId() {
            return feedId;
        }

        public void setFeedId(String feedId) {
            this.feedId = feedId;
        }

        public boolean isCurated() {
            return curated;
        }

        public void setCurated(boolean curated) {
            this.curated = curated;
        }

        public int getSubscribers() {
            return subscribers;
        }

        public void setSubscribers(int subscribers) {
            this.subscribers = subscribers;
        }
    }
}
