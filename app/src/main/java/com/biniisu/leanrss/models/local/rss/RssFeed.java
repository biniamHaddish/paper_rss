package com.biniisu.leanrss.models.local.rss;

import android.support.annotation.Keep;

import com.biniisu.leanrss.connectivity.local.RSS;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

/**
 * Created by biniam_Haddish on 30/11/17.
 * <p>
 * This class the POJO equivalent of an atom entry tag
 */

@Keep
@Root(name = RSS.ITEM_TAG, strict = false)
public class RssFeed {
    @Element(required = false)
    public String title;
    @Element(required = false)
    public String link;
    @Element(required = false)
    public String pubDate;
    @Element(required = false)
    public String guid;
    @Element(required = false)
    public String description;
    @Element(name = "creator", required = false)
    @Namespace(prefix = "dc", reference = "readablyapp.com/xml/dc")
    public String author;
    @Element(name = "encoded", data = true, required = false)
    @Namespace(prefix = "content", reference = "readablyapp.com/xml/content")
    public String contentEncoded;
}
