package com.biniisu.leanrss.models;

import android.support.annotation.Keep;

/**
 * Created by  on 5/3/17.
 * <p>
 * This class represents a data returned by mercury parser
 */

@Keep
public class MercuryResult {

    private String title;
    private String content;
    private String author;
    private String date_published;
    private String lead_image_url;
    private String dek;
    private String url;
    private String domain;
    private String excerpt;
    private int word_count;
    private String direction;
    private int total_pages;
    private int rendered_pages;
    private String next_page_url;

    public MercuryResult(String title, String content, String date_published, String lead_image_url, String dek, String url, String domain, String excerpt, int word_count, String direction, int total_pages, int rendered_pages, String next_page_url) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.date_published = date_published;
        this.lead_image_url = lead_image_url;
        this.dek = dek;
        this.url = url;
        this.domain = domain;
        this.excerpt = excerpt;
        this.word_count = word_count;
        this.direction = direction;
        this.total_pages = total_pages;
        this.rendered_pages = rendered_pages;
        this.next_page_url = next_page_url;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate_published() {
        return date_published;
    }

    public String getLead_image_url() {
        return lead_image_url;
    }

    public String getDek() {
        return dek;
    }

    public String getUrl() {
        return url;
    }

    public String getDomain() {
        return domain;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public int getWord_count() {
        return word_count;
    }

    public String getDirection() {
        return direction;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public int getRendered_pages() {
        return rendered_pages;
    }

    public String getNext_page_url() {
        return next_page_url;
    }
}
