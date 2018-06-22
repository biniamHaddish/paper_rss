package com.biniam.rss.models.feedly;

/**
 * Created by biniam on 5/9/17.
 */

public class FeedlyCatagories {
    /**
     * label : tech
     * id : user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/tech
     * content : Best tech websites
     */

    private String label;
    private String id;
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
