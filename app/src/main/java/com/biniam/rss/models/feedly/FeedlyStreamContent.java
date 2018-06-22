package com.biniam.rss.models.feedly;

import java.util.List;

/**
 * Created by biniam on 5/20/17.
 */

public class FeedlyStreamContent {


    /**
     * alternate : [{"type":"text/html","href":"http://www.theverge.com/"}]
     * direction : ltr
     * self : [{" href":"https => //cloud.feedly.com/reader/3/stream/contents/feed%2Fhttp%3A%2F%2Fwww.theverge.com%2Frss%2Ffull.xml?n=20&unreadOnly=true"}]
     * updated : 1367539068016
     * items : [{"engagement":15,"author":"Nathan Ingraham","alternate":[{"type":"text/html","href":"http://www.theverge.com/2013/4/17/4236096/nbc-heroes-may-get-a-second-lease-on-life-on-xbox-live"}],"tags":[{"label":"inspiration","id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/tag/inspiration"}],"origin":{"streamId":"feed/http://www.theverge.com/rss/full.xml","htmlUrl":"http://www.theverge.com/","title":"The Verge -  All Posts"},"unread":true,"updated":1367539068016,"published":1367539068016,"crawled":1367539068016,"title":"NBC's reviled sci-fi drama 'Heroes' may get a second lease on life as Xbox Live exclusive","categories":[{"label":"tech","id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/tech"}],"content":{"direction":"ltr","content":"..."},"id":"gRtwnDeqCDpZ42bXE9Sp7dNhm4R6NsipqFVbXn2XpDA=_13fb9d6f274:2ac9c5:f5718180"},{"engagement":39,"author":"T.C. Sottek","alternate":[{"type":"text/html","href":"http://www.theverge.com/2013/4/17/4236136/senate-rejects-gun-control-amendment"}],"tags":[{"label":"inspiration","id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/tag/inspiration"}],"origin":{"streamId":"feed/http://www.theverge.com/rss/full.xml","htmlUrl":"http://www.theverge.com/","title":"The Verge -  All Posts"},"unread":true,"updated":1367539068016,"published":1367539068016,"crawled":1367539068016,"title":"Senate rejects bipartisan gun control measure for background checks despite broad public support","categories":[{"label":"tech","id":"user/c805fcbf-3acf-4302-a97e-d82f9d7c897f/category/tech"}],"content":{"direction":"ltr","content":"...html content..."},"id":"gRtwnDeqCDpZ42bXE9Sp7dNhm4R6NsipqFVbXn2XpDA=_13fb9d6f274:2ac9c5:f5718182"}]
     * title : The Verge -  All Posts
     * id : feed/http => //www.theverge.com/rss/full.xml
     * continuation : gRtwnDeqCDpZ42bXE9Sp7dNhm4R6NsipqFVbXn2XpDA=_13fb9d6f274:2ac9c5:f5718180
     */

    private String direction;
    private long updated;
    private String title;
    private String id;
    private String continuation;
    private List<AlternateBean> alternate;
    private List<SelfBean> self;
    //private List<FeedlyEntries.> items;

    public static class AlternateBean {
        /**
         * type : text/html
         * href : http://www.theverge.com/
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

    public static class SelfBean {
        /**
         * href : https => //cloud.feedly.com/reader/3/stream/contents/feed%2Fhttp%3A%2F%2Fwww.theverge.com%2Frss%2Ffull.xml?n=20&unreadOnly=true
         */

        private String href;
    }
}
