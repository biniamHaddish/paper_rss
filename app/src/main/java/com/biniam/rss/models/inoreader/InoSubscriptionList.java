package com.biniam.rss.models.inoreader;

import android.support.annotation.Keep;

import java.util.List;

/**
 * Created by biniam on 5/1/17.
 */
@Keep
public class InoSubscriptionList {

    private List<SubscriptionsBean> subscriptions;

    public List<SubscriptionsBean> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<SubscriptionsBean> subscriptions) {
        this.subscriptions = subscriptions;
    }
@Keep
    public static class SubscriptionsBean {

        /**
         * id : feed/http://www.theanimationblog.com/feed/
         * title : The Animation Blog.com | Est. 2007
         * categories : [{"id":"user/1005921515/label/Animation","label":"Animation"}]
         * sortid : 00DA6134
         * firstitemmsec : 1424501776942006
         * url : http://www.theanimationblog.com/feed/
         * htmlUrl : http://www.theanimationblog.com/
         * iconUrl :
         */

        private String id;
        private String title;
        private String sortid;
        private long firstitemmsec;
        private String url;
        private String htmlUrl;
        private String iconUrl;
        private List<CategoriesBean> categories;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSubId() {
            return id.replace("/", "_");
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSortid() {
            return sortid;
        }

        public void setSortid(String sortid) {
            this.sortid = sortid;
        }

        public long getFirstitemmsec() {
            return firstitemmsec;
        }

        public void setFirstitemmsec(long firstitemmsec) {
            this.firstitemmsec = firstitemmsec;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

        public List<CategoriesBean> getCategories() {
            return categories;
        }

        public void setCategories(List<CategoriesBean> categories) {
            this.categories = categories;
        }

        @Keep
        public static class CategoriesBean {
            /**
             * id : user/1005921515/label/Animation
             * label : Animation
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
    }
}
