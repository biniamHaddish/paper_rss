package com.biniisu.leanrss.models.feedly;

import java.util.List;

/**
 * Created by biniam on 5/9/17.
 */

public class FeedlySubscription {

    /**
     * website : http://design-milk.com
     * sortid : 26152F8F
     * updated : 1367539068016
     * categories : [{"label":"design","id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/design"},{"label":"must reads","id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/global.must"}]
     * title : Design Milk
     * id : feed/http://feeds.feedburner.com/design-milk
     * visualUrl : http://pbs.twimg.com/profile_images/1765276661/DMLogoTM-carton-icon-facebook-twitter_bigger.jpg
     * added : 1367539068016
     */

    private String website;
    private String sortid;
    private long updated;
    private String title;
    private String id;
    private String visualUrl;
    private long added;
    private List<CategoriesBean> categories;

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getSortid() {
        return sortid;
    }

    public void setSortid(String sortid) {
        this.sortid = sortid;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
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

    public String getVisualUrl() {
        return visualUrl;
    }

    public void setVisualUrl(String visualUrl) {
        this.visualUrl = visualUrl;
    }

    public long getAdded() {
        return added;
    }

    public void setAdded(long added) {
        this.added = added;
    }

    public List<CategoriesBean> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoriesBean> categories) {
        this.categories = categories;
    }

    public static class CategoriesBean {
        /**
         * label : design
         * id : user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/design
         */

        private String label;
        private String id;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
