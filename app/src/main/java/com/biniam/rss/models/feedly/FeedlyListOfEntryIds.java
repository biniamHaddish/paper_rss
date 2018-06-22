package com.biniam.rss.models.feedly;

import java.util.List;

/**
 * Created by biniam on 5/19/17.
 */

public class FeedlyListOfEntryIds {

    /**
     * ids : ["gRtwnDeqCDpZ42bXE9Sp7dNhm4R6NsipqFVbXn2XpDA=_13fb9d6f274:2ac9c5:f5718180","9bVktswTBLT3zSr0Oy09Gz8mJYLymYp71eEVeQryp2U=_13fb9d1263d:2a8ef5:db3da1a7"]
     * continuation : 13fb9d1263d:2a8ef5:db3da1a7
     */

    private String continuation;
    private List<String> ids;

    public String getContinuation() {
        return continuation;
    }

    public void setContinuation(String continuation) {
        this.continuation = continuation;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
