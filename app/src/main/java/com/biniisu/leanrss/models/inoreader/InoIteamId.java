package com.biniisu.leanrss.models.inoreader;

import android.support.annotation.Keep;

import java.util.List;

/**
 * Created by biniam on 5/2/17.
 */
@Keep
public class InoIteamId {

    /**
     * items : []
     * itemRefs : [{"id":"3614359203","directStreamIds":["user/1005921515/label/MTB"],"timestampUsec":"1416313130505268"},{"id":"3614347074","directStreamIds":["user/1005921515/label/MUST READ"],"timestampUsec":"1416313031906359"},{"id":"3614347075","directStreamIds":["user/1005921515/label/MUST READ"],"timestampUsec":"1416313031906358"}]
     * continuation : aDRTXr1ek6qT
     */

    private String continuation;
    private List<?> items;
    private List<ItemRefsBean> itemRefs;

    public String getContinuation() {
        return continuation;
    }

    public void setContinuation(String continuation) {
        this.continuation = continuation;
    }

    public List<?> getItems() {
        return items;
    }

    public void setItems(List<?> items) {
        this.items = items;
    }

    public List<ItemRefsBean> getItemRefs() {
        return itemRefs;
    }

    public void setItemRefs(List<ItemRefsBean> itemRefs) {
        this.itemRefs = itemRefs;
    }

    public static class ItemRefsBean {
        /**
         * id : 3614359203
         * directStreamIds : ["user/1005921515/label/MTB"]
         * timestampUsec : 1416313130505268
         */

        private String id;
        private String timestampUsec;
        private List<String> directStreamIds;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTimestampUsec() {
            return timestampUsec;
        }

        public void setTimestampUsec(String timestampUsec) {
            this.timestampUsec = timestampUsec;
        }

        public List<String> getDirectStreamIds() {
            return directStreamIds;
        }

        public void setDirectStreamIds(List<String> directStreamIds) {
            this.directStreamIds = directStreamIds;
        }
    }
}
