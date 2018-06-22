package com.biniisu.leanrss.models.inoreader;

import android.support.annotation.Keep;

import java.util.List;

/**
 * Created by biniam on 5/1/17.
 */
@Keep
public class InoFoldersTagsList {

    private List<TagsBean> tags;

    public List<TagsBean> getTags() {
        return tags;
    }

    public void setTags(List<TagsBean> tags) {
        this.tags = tags;
    }
    @Keep
    public static class TagsBean {
        /**
         * id : user/1005921515/state/com.google/starred
         * sortid : FFFFFFFF
         */

        private String id;
        private String sortid;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSortid() {
            return sortid;
        }

        public void setSortid(String sortid) {
            this.sortid = sortid;
        }
    }
}
