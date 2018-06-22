package com.biniam.rss.models.feedly;

import java.util.List;

/**
 * Created by biniam on 5/23/17.
 */

public class FeedlyUnreadCount {

    /**
     * updated : 1367539057683
     * unreadcounts : [{"updated":1367539068016,"count":605,"id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/global.all"},{"updated":1367539068016,"count":601,"id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/design"},{"updated":1367539068016,"count":508,"id":"feed/http://www.autoblog.com/rss.xml"},{"updated":1367539068016,"count":3,"id":"feed/http://feeds.feedburner.com/BakingObsession"},{"updated":1367539068016,"count":2,"id":"feed/http://vimeo.com/mattrunks/videos/rss"},{"updated":1367539068016,"count":1,"id":"feed/http://feeds.feedburner.com/DorieGreenspan"},{"updated":1367539068016,"count":3,"id":"feed/http://chasingdelicious.com/feed/"}]
     */

    private long updated;
    private List<UnreadcountsBean> unreadcounts;

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public List<UnreadcountsBean> getUnreadcounts() {
        return unreadcounts;
    }

    public void setUnreadcounts(List<UnreadcountsBean> unreadcounts) {
        this.unreadcounts = unreadcounts;
    }

    public static class UnreadcountsBean {
        /**
         * updated : 1367539068016
         * count : 605
         * id : user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/global.all
         */

        private long updated;
        private int count;
        private String id;

        public long getUpdated() {
            return updated;
        }

        public void setUpdated(long updated) {
            this.updated = updated;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
