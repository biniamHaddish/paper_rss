package com.biniisu.leanrss.models.feedly;

import java.util.List;

/**
 * Created by biniam on 5/21/17.
 */

public class FeedlyDynamicEntryList {

    /**
     * engagement : 12
     * author : Nathan Ingraham
     * alternate : [{"type":"text/html","href":"http://www.theverge.com/2013/4/17/4236096/nbc-heroes-may-get-a-second-lease-on-life-on-xbox-live"}]
     * tags : [{"label":"inspiration","id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/tag/inspiration"}]
     * origin : {"streamId":"feed/http://www.theverge.com/rss/full.xml","htmlUrl":"http://www.theverge.com/","title":"The Verge -  All Posts"}
     * unread : true
     * updated : 1367539068016
     * published : 1367539068016
     * crawled : 1367539068016
     * title : NBC's reviled sci-fi drama 'Heroes' may get a second lease on life as Xbox Live exclusive
     * categories : [{"label":"tech","id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/tech"}]
     * content : {"direction":"ltr","content":"..."}
     * keywords : ["NBC","Sci-fi"]
     * id : entryId
     * engagementRate : 1.23
     */

    private int engagement;
    private String author;
    private OriginBean origin;
    private boolean unread;
    private long updated;
    private long published;
    private long crawled;
    private String title;
    private ContentBean content;
    private String id;
    private double engagementRate;
    private List<AlternateBean> alternate;
    private List<TagsBean> tags;
    private List<CategoriesBean> categories;
    private List<String> keywords;

    public int getEngagement() {
        return engagement;
    }

    public void setEngagement(int engagement) {
        this.engagement = engagement;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public OriginBean getOrigin() {
        return origin;
    }

    public void setOrigin(OriginBean origin) {
        this.origin = origin;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public long getPublished() {
        return published;
    }

    public void setPublished(long published) {
        this.published = published;
    }

    public long getCrawled() {
        return crawled;
    }

    public void setCrawled(long crawled) {
        this.crawled = crawled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ContentBean getContent() {
        return content;
    }

    public void setContent(ContentBean content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getEngagementRate() {
        return engagementRate;
    }

    public void setEngagementRate(double engagementRate) {
        this.engagementRate = engagementRate;
    }

    public List<AlternateBean> getAlternate() {
        return alternate;
    }

    public void setAlternate(List<AlternateBean> alternate) {
        this.alternate = alternate;
    }

    public List<TagsBean> getTags() {
        return tags;
    }

    public void setTags(List<TagsBean> tags) {
        this.tags = tags;
    }

    public List<CategoriesBean> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoriesBean> categories) {
        this.categories = categories;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public static class OriginBean {
        /**
         * streamId : feed/http://www.theverge.com/rss/full.xml
         * htmlUrl : http://www.theverge.com/
         * title : The Verge -  All Posts
         */

        private String streamId;
        private String htmlUrl;
        private String title;

        public String getStreamId() {
            return streamId;
        }

        public void setStreamId(String streamId) {
            this.streamId = streamId;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static class ContentBean {
        /**
         * direction : ltr
         * content : ...
         */

        private String direction;
        private String content;

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class AlternateBean {
        /**
         * type : text/html
         * href : http://www.theverge.com/2013/4/17/4236096/nbc-heroes-may-get-a-second-lease-on-life-on-xbox-live
         */

        private String type;
        private String href;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }

    public static class TagsBean {
        /**
         * label : inspiration
         * id : user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/tag/inspiration
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

    public static class CategoriesBean {
        /**
         * label : tech
         * id : user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/tech
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
