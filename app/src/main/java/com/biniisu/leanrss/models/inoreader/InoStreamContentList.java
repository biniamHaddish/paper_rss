package com.biniisu.leanrss.models.inoreader;

import android.support.annotation.Keep;

import java.util.List;

/**
 * Created by biniam on 5/2/17.
 */
@Keep
public class InoStreamContentList {

    /**
     * direction : ltr
     * id : user/-/label/Google
     * title : Reading List
     * content :
     * self : {"href":"https://www.inoreader.com/reader/api/0/stream/contents/user%2F-%2Flabel%2FGoogle?r=o&n=3"}
     * updated : 1424637593
     * updatedUsec : 1424637593264558
     * items : [{"crawlTimeMsec":"1422046342882","timestampUsec":"1422046342881684","id":"tag:google.com,2005:reader/item/00000000f8b9270e","categories":["user/1005921515/state/com.google/reading-list","user/1005921515/state/com.google/read","user/1005921515/label/Google"],"title":"Through the Google lens: Search trends January 16-22","published":1422046320,"updated":1422669611,"canonical":[{"href":"http://feedproxy.google.com/~r/blogspot/MKuf/~3/_Hkdwh7yKMo/through-google-lens-search-trends_23.html"}],"alternate":[{"href":"http://feedproxy.google.com/~r/blogspot/MKuf/~3/_Hkdwh7yKMo/through-google-lens-search-trends_23.html","type":"text/html"}],"summary":{"direction":"ltr","content":"..."},"author":"Emily Wood","likingUsers":[],"comments":[],"commentsNum":-1,"annotations":[],"origin":{"streamId":"feed/http://feeds.feedburner.com/blogspot/MKuf","title":"The Official Google Blog","htmlUrl":"http://googleblog.blogspot.com/"}},{"crawlTimeMsec":"1422263983452","timestampUsec":"1422263983452401","id":"tag:google.com,2005:reader/item/00000000f9ccc3f9","categories":["user/1005921515/state/com.google/reading-list","user/1005921515/state/com.google/read","user/1005921515/label/Google"],"title":"Google Maps Engine deprecated","published":1422262271,"updated":1422538193,"canonical":[{"href":"http://feedproxy.google.com/~r/GoogleEarthBlog/~3/HqKBr0Se8K8/google-maps-engine-deprecated.html"}],"alternate":[{"href":"http://feedproxy.google.com/~r/GoogleEarthBlog/~3/HqKBr0Se8K8/google-maps-engine-deprecated.html","type":"text/html"}],"summary":{"direction":"ltr","content":"..."},"author":"Timothy Whitehead","likingUsers":[],"comments":[],"commentsNum":-1,"annotations":[],"origin":{"streamId":"feed/http://feeds.feedburner.com/GoogleEarthBlog","title":"Google Earth Blog","htmlUrl":"http://www.gearthblog.com/"}},{"crawlTimeMsec":"1422283522174","timestampUsec":"1422283522173992","id":"tag:google.com,2005:reader/item/00000000f9efb84d","categories":["user/1005921515/state/com.google/reading-list","user/1005921515/state/com.google/read","user/1005921515/label/Google"],"title":"Strava maps runs, rides and fitness data using the Maps API","published":1422283440,"updated":1422554242,"canonical":[{"href":"http://feedproxy.google.com/~r/GoogleforWork/~3/-GpQzKk4LJY/mapping-runs-rides-and-fitness-data-using-the-maps-API.html"}],"alternate":[{"href":"http://feedproxy.google.com/~r/GoogleforWork/~3/-GpQzKk4LJY/mapping-runs-rides-and-fitness-data-using-the-maps-API.html","type":"text/html"}],"summary":{"direction":"ltr","content":"..."},"author":"Jane Smith","likingUsers":[],"comments":[],"commentsNum":-1,"annotations":[],"origin":{"streamId":"feed/http://feeds.feedburner.com/OfficialGoogleEnterpriseBlog","title":"Google Enterprise Blog","htmlUrl":"http://googleforwork.blogspot.com/"}}]
     * continuation : trMnkg7wWT62
     */

    private String direction;
    private String id;
    private String title;
    private String description;
    private SelfBean self;
    private int updated;
    private String updatedUsec;
    private String continuation;
    private List<ItemsBean> items;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SelfBean getSelf() {
        return self;
    }

    public void setSelf(SelfBean self) {
        this.self = self;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public String getUpdatedUsec() {
        return updatedUsec;
    }

    public void setUpdatedUsec(String updatedUsec) {
        this.updatedUsec = updatedUsec;
    }

    public String getContinuation() {
        return continuation;
    }

    public void setContinuation(String continuation) {
        this.continuation = continuation;
    }

    public List<ItemsBean> getItems() {
        return items;
    }

    public void setItems(List<ItemsBean> items) {
        this.items = items;
    }
    @Keep
    public static class SelfBean {
        /**
         * href : https://www.inoreader.com/reader/api/0/stream/contents/user%2F-%2Flabel%2FGoogle?r=o&n=3
         */

        private String href;

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }
    @Keep
    public static class ItemsBean {
        /**
         * crawlTimeMsec : 1422046342882
         * timestampUsec : 1422046342881684
         * id : tag:google.com,2005:reader/item/00000000f8b9270e
         * categories : ["user/1005921515/state/com.google/reading-list","user/1005921515/state/com.google/read","user/1005921515/label/Google"]
         * title : Through the Google lens: Search trends January 16-22
         * published : 1422046320
         * updated : 1422669611
         * canonical : [{"href":"http://feedproxy.google.com/~r/blogspot/MKuf/~3/_Hkdwh7yKMo/through-google-lens-search-trends_23.html"}]
         * alternate : [{"href":"http://feedproxy.google.com/~r/blogspot/MKuf/~3/_Hkdwh7yKMo/through-google-lens-search-trends_23.html","type":"text/html"}]
         * summary : {"direction":"ltr","content":"..."}
         * author : Emily Wood
         * likingUsers : []
         * comments : []
         * commentsNum : -1
         * annotations : []
         * origin : {"streamId":"feed/http://feeds.feedburner.com/blogspot/MKuf","title":"The Official Google Blog","htmlUrl":"http://googleblog.blogspot.com/"}
         */

        private String crawlTimeMsec;
        private String timestampUsec;
        private String id;
        private String title;
        private int published;
        private int updated;
        private SummaryBean summary;
        private String author;
        private int commentsNum;
        private OriginBean origin;
        private List<String> categories;
        private List<CanonicalBean> canonical;
        private List<AlternateBean> alternate;
        private List<?> likingUsers;
        private List<?> comments;
        private List<?> annotations;

        public String getCrawlTimeMsec() {
            return crawlTimeMsec;
        }

        public void setCrawlTimeMsec(String crawlTimeMsec) {
            this.crawlTimeMsec = crawlTimeMsec;
        }

        public String getTimestampUsec() {
            return timestampUsec;
        }

        public void setTimestampUsec(String timestampUsec) {
            this.timestampUsec = timestampUsec;
        }

        public String getId() {
            return id;
        }


        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getPublished() {
            return published;
        }

        public void setPublished(int published) {
            this.published = published;
        }

        public int getUpdated() {
            return updated;
        }

        public void setUpdated(int updated) {
            this.updated = updated;
        }

        public SummaryBean getSummary() {
            return summary;
        }

        public void setSummary(SummaryBean summary) {
            this.summary = summary;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public int getCommentsNum() {
            return commentsNum;
        }

        public void setCommentsNum(int commentsNum) {
            this.commentsNum = commentsNum;
        }

        public OriginBean getOrigin() {
            return origin;
        }

        public void setOrigin(OriginBean origin) {
            this.origin = origin;
        }

        public List<String> getCategories() {
            return categories;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories;
        }

        public List<CanonicalBean> getCanonical() {
            return canonical;
        }

        public void setCanonical(List<CanonicalBean> canonical) {
            this.canonical = canonical;
        }

        public List<AlternateBean> getAlternate() {
            return alternate;
        }

        public void setAlternate(List<AlternateBean> alternate) {
            this.alternate = alternate;
        }

        public List<?> getLikingUsers() {
            return likingUsers;
        }

        public void setLikingUsers(List<?> likingUsers) {
            this.likingUsers = likingUsers;
        }

        public List<?> getComments() {
            return comments;
        }

        public void setComments(List<?> comments) {
            this.comments = comments;
        }

        public List<?> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(List<?> annotations) {
            this.annotations = annotations;
        }
        @Keep
        public static class SummaryBean {
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
         @Keep
        public static class OriginBean {
            /**
             * streamId : feed/http://feeds.feedburner.com/blogspot/MKuf
             * title : The Official Google Blog
             * htmlUrl : http://googleblog.blogspot.com/
             */

            private String streamId;
            private String title;
            private String htmlUrl;

            public String getStreamId() {
                return streamId;
            }

            public void setStreamId(String streamId) {
                this.streamId = streamId;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getHtmlUrl() {
                return htmlUrl;
            }

            public void setHtmlUrl(String htmlUrl) {
                this.htmlUrl = htmlUrl;
            }
        }
         @Keep
        public static class CanonicalBean {
            /**
             * href : http://feedproxy.google.com/~r/blogspot/MKuf/~3/_Hkdwh7yKMo/through-google-lens-search-trends_23.html
             */

            private String href;

            public String getHref() {
                return href;
            }

            public void setHref(String href) {
                this.href = href;
            }
        }
        @Keep
        public static class AlternateBean {
            /**
             * href : http://feedproxy.google.com/~r/blogspot/MKuf/~3/_Hkdwh7yKMo/through-google-lens-search-trends_23.html
             * type : text/html
             */

            private String href;
            private String type;

            public String getHref() {
                return href;
            }

            public void setHref(String href) {
                this.href = href;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }
    }
}
