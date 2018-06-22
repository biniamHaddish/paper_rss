package com.biniam.rss.models.inoreader;

import android.support.annotation.Keep;

/**
 * Created by biniam on 4/6/17.
 */
@Keep
public class InoReaderSubscriptionItems {

    /**
     * query : feed/http://feeds.arstechnica.com/arstechnica/science
     * numResults : 1
     * streamId : feed/http://arstechnica.com/
     * streamName : Ars Technica » Scientific Method
     */

    private String query;
    private int numResults;
    private String streamId;
    private String streamName;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getNumResults() {
        return numResults;
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }
}
