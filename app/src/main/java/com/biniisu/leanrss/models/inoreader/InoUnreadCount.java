package com.biniisu.leanrss.models.inoreader;

import android.support.annotation.Keep;

import java.util.List;

/**
 * Created by biniam on 5/1/17.
 */
@Keep
public class InoUnreadCount {

    /**
     * max : 1000
     * unreadcounts : [{"id":"user/1005921515/state/com.google/reading-list","count":4,"newestItemTimestampUsec":"1415620910006331"},{"id":"user/1005921515/state/com.google/starred","count":5,"newestItemTimestampUsec":"1415620910006331"},{"id":"user/1005921515/label/Animation","count":0,"newestItemTimestampUsec":"1415620910006331"},{"id":"user/1005921515/label/CAN READ","count":0,"newestItemTimestampUsec":"1415620910006331"}]
     */

    private String max;
    private List<UnreadcountsBean> unreadcounts;

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public List<UnreadcountsBean> getUnreadcounts() {
        return unreadcounts;
    }

    public void setUnreadcounts(List<UnreadcountsBean> unreadcounts) {
        this.unreadcounts = unreadcounts;
    }
    @Keep
    public static class UnreadcountsBean {
        /**
         * id : user/1005921515/state/com.google/reading-list
         * count : 4
         * newestItemTimestampUsec : 1415620910006331
         */

        private String id;
        private int count;
        private String newestItemTimestampUsec;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getNewestItemTimestampUsec() {
            return newestItemTimestampUsec;
        }

        public void setNewestItemTimestampUsec(String newestItemTimestampUsec) {
            this.newestItemTimestampUsec = newestItemTimestampUsec;
        }
    }
}
