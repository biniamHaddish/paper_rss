package com.biniam.rss.models.feedbin;

import android.support.annotation.Keep;

import java.io.Serializable;

/**
 * Created by biniam on 2/28/17.
 */


@Keep
public class FeedBinEntriesItem implements Serializable {

    /**
     * id : 2077
     * feed_id : 135
     * title : Objective-C Runtime Releases
     * url : http://mjtsai.com/blog/2013/02/02/objective-c-runtime-releases/
     * author : Michael Tsai
     * content : <p><a href="https://twitter.com/bavarious/status/297851496945577984">Bavarious</a> created a <a href="https://github.com/bavarious/objc4/commits/master">GitHub repository</a> that shows the differences between versions of <a href="http://www.opensource.apple.com/source/objc4/">Apple’s Objective-C runtime</a> that shipped with different versions of Mac OS X.</p>
     * summary : Bavarious created a GitHub repository that shows the differences between versions of Apple’s Objective-C runtime that shipped with different versions of Mac OS X.
     * published : 2013-02-03T01:00:19.000000Z
     * created_at : 2013-02-04T01:00:19.127893Z
     */


    private int id;
    private int feed_id;
    private String title;
    private String url;
    private String author;
    private String content;
    private String summary;
    private String published;
    private String created_at;


    private boolean read;    // The read status of the feed item
    private boolean favorite; // Whether this feed item is favorite
    private String fullArticle;
    private long publishedTimeStamp;
    private long createdTimeStamp;

    public FeedBinEntriesItem(
            int id,
            int feed_id,
            String title,
            String url,
            String author,
            String content,
            String summary,
            String published,
            String created_at) {

        this.id=id;
        this.feed_id=feed_id;
        this.title= title;
        this.url=url;
        this.author= author;
        this.content= content;
        this.summary= summary;
        this.published= published;
        this.created_at= created_at;
    }

    public FeedBinEntriesItem(
            int id,
            int feed_id,
            String title,
            String url,
            String author,
            String content,
            String summary,
            String published,
            String created_at,
            boolean read, boolean favorite) {
        this.id = id;
        this.feed_id = feed_id;
        this.title = title;
        this.url = url;
        this.author = author;
        this.content = content;
        this.summary = summary;
        this.published = published;
        this.created_at = created_at;
        this.read = read;
        this.favorite = favorite;

    }


    public String getId() {
        return String.valueOf(id);
    }


    public String getSubscriptionId() {
        return String.valueOf(feed_id);
    }


    public long getPublishedTime() {
        return publishedTimeStamp;
    }


    public long getCreatedTime() {
        return createdTimeStamp;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getUrl() {
        return url;
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public String getFullArticle() {
        return null;
    }


    public void setFullArticle(String fullArticle) {
        this.fullArticle = fullArticle;
    }


    public boolean hasFullArticle() {
        return fullArticle != null && !fullArticle.isEmpty();
    }


    public boolean isRead() {
        return false;
    }


    public void setRead(boolean read) {
        this.read = read;
    }


    public boolean isFavorite() {
        return false;
    }


    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }


    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }


    public String getSummary() {
        return summary;
    }


    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPublished() {
        return published;
    }

    public String getCreated_at() {
        return created_at;
    }
}
