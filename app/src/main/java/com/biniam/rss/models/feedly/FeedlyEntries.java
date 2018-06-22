package com.biniam.rss.models.feedly;

import java.util.List;

/**
 * Created by biniam on 5/17/17.
 */

public class FeedlyEntries {

    /**
     * engagement : 1476
     * tags : [{"id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/tag/global.saved"},{"label":"Microsoft","id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/tag/Microsoft"}]
     * author : Sarah Perez
     * alternate : [{"type":"text/html","href":"http://feedproxy.google.com/~r/Techcrunch/~3/iEm1aA_M_dw/"}]
     * unread : false
     * origin : {"streamId":"feed/http://feeds.feedburner.com/Techcrunch","htmlUrl":"http://techcrunch.com","title":"TechCrunch"}
     * serverId : http://techcrunch.com/?p=1261251
     * updated : 1452614967000
     * published : 1452614967000
     * visual : {"url":"https://tctechcrunch2011.files.wordpress.com/%2F2016%2F01%2Fmicrosoft-internet-explorer-10.png","width":3000,"contentType":"image/png","height":1687}
     * categories : [{"label":"Tech","id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/Tech"},{"label":"Must Read","id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/global.must"}]
     * canonical : [{"type":"text/html","href":"http://techcrunch.com/2016/01/12/microsoft-today-ends-support-for-windows-8-old-versions-of-internet-explorer/?ncid=rss"}]
     * crawled : 1452614994867
     * title : Microsoft Today Ends Support For Windows 8, Old Versions Of Internet Explorer
     * thumbnail : [{"url":"https://tctechcrunch2011.files.wordpress.com/2016/01/microsoft-internet-explorer-10.png"}]
     * engagementRate : 7.88
     * enclosure : [{"type":"image/png","href":"https://tctechcrunch2011.files.wordpress.com/2016/01/microsoft-internet-explorer-10.png"},{"href":"http://tctechcrunch2011.files.wordpress.com/2016/01/microsoft-internet-explorer-10.png?w=150"},{"href":"http://2.gravatar.com/avatar/5225bb627e112543aa03bf3b2958be3f?s=96&d=identicon&r=G"},{"href":"https://tctechcrunch2011.files.wordpress.com/2016/01/edge_phase2_banner_cortana_1400px.jpg?w=680"}]
     * summary : {"direction":"ltr","content":"<img height=\"382\" alt=\"Microsoft-Internet-Explorer-10\" width=\"680\" class=\"wp-post-image\" src=\"https://tctechcrunch2011.files.wordpress.com/2016/01/microsoft-internet-explorer-10.png?w=680\"> Microsoft\u2019s push towards Windows 10 continues. Today, Microsoft is ending support for Windows 8, as well as older versions of its Internet Explorer web browser, IE 8, IE 9, and IE 10. For end users, that doesn\u2019t mean the software instantly becomes non-functional, but that it will longer be updated with bug fixes or other security patches."}
     * fingerprint : 17c5dd0d
     * keywords : ["TC","Microsoft","Internet-Explorer","Windows 10","Microsoft Edge","PCs"]
     * id : Xne8uW/IUiZhV1EuO2ZMzIrc2Ak6NlhGjboZ+Yk0rJ8=_1523699cbb3:2aa0463:e47a7aef
     * recrawled : 1452618026719
     */

    private int engagement;
    private String author;
    private boolean unread;
    private OriginBean origin;
    private String originId;
    private long updated;
    private long published;
    private VisualBean visual;
    private long crawled;
    private String title;
    private double engagementRate;
    private SummaryBean summary;
    private String fingerprint;
    private String id;
    private long recrawled;
    private List<TagsBean> tags;
    private List<AlternateBean> alternate;
    private List<CategoriesBean> categories;
    private List<CanonicalBean> canonical;
    private List<ThumbnailBean> thumbnail;
    private List<EnclosureBean> enclosure;
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

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public OriginBean getOrigin() {
        return origin;
    }

    public void setOrigin(OriginBean origin) {
        this.origin = origin;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
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

    public VisualBean getVisual() {
        return visual;
    }

    public void setVisual(VisualBean visual) {
        this.visual = visual;
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

    public double getEngagementRate() {
        return engagementRate;
    }

    public void setEngagementRate(double engagementRate) {
        this.engagementRate = engagementRate;
    }

    public SummaryBean getSummary() {
        return summary;
    }

    public void setSummary(SummaryBean summary) {
        this.summary = summary;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getRecrawled() {
        return recrawled;
    }

    public void setRecrawled(long recrawled) {
        this.recrawled = recrawled;
    }

    public List<TagsBean> getTags() {
        return tags;
    }

    public void setTags(List<TagsBean> tags) {
        this.tags = tags;
    }

    public List<AlternateBean> getAlternate() {
        return alternate;
    }

    public void setAlternate(List<AlternateBean> alternate) {
        this.alternate = alternate;
    }

    public List<CategoriesBean> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoriesBean> categories) {
        this.categories = categories;
    }

    public List<CanonicalBean> getCanonical() {
        return canonical;
    }

    public void setCanonical(List<CanonicalBean> canonical) {
        this.canonical = canonical;
    }

    public List<ThumbnailBean> getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(List<ThumbnailBean> thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<EnclosureBean> getEnclosure() {
        return enclosure;
    }

    public void setEnclosure(List<EnclosureBean> enclosure) {
        this.enclosure = enclosure;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public static class OriginBean {
        /**
         * streamId : feed/http://feeds.feedburner.com/Techcrunch
         * htmlUrl : http://techcrunch.com
         * title : TechCrunch
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

    public static class VisualBean {
        /**
         * url : https://tctechcrunch2011.files.wordpress.com/%2F2016%2F01%2Fmicrosoft-internet-explorer-10.png
         * width : 3000
         * contentType : image/png
         * height : 1687
         */

        private String url;
        private int width;
        private String contentType;
        private int height;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public static class SummaryBean {
        /**
         * direction : ltr
         * content : <img height="382" alt="Microsoft-Internet-Explorer-10" width="680" class="wp-post-image" src="https://tctechcrunch2011.files.wordpress.com/2016/01/microsoft-internet-explorer-10.png?w=680"> Microsoft’s push towards Windows 10 continues. Today, Microsoft is ending support for Windows 8, as well as older versions of its Internet Explorer web browser, IE 8, IE 9, and IE 10. For end users, that doesn’t mean the software instantly becomes non-functional, but that it will longer be updated with bug fixes or other security patches.
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

    public static class TagsBean {
        /**
         * id : user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/tag/global.saved
         * label : Microsoft
         */

        private String id;
        private String label;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public static class AlternateBean {
        /**
         * type : text/html
         * href : http://feedproxy.google.com/~r/Techcrunch/~3/iEm1aA_M_dw/
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

    public static class CategoriesBean {
        /**
         * label : Tech
         * id : user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/Tech
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

    public static class CanonicalBean {
        /**
         * type : text/html
         * href : http://techcrunch.com/2016/01/12/microsoft-today-ends-support-for-windows-8-old-versions-of-internet-explorer/?ncid=rss
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

    public static class ThumbnailBean {
        /**
         * url : https://tctechcrunch2011.files.wordpress.com/2016/01/microsoft-internet-explorer-10.png
         */

        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class EnclosureBean {
        /**
         * type : image/png
         * href : https://tctechcrunch2011.files.wordpress.com/2016/01/microsoft-internet-explorer-10.png
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
}
