package com.biniam.rss.models.local.atom;

import android.support.annotation.Keep;

import com.biniam.rss.connectivity.local.RSS;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by biniam_Haddish on 30/11/17.
 * <p>
 * This class the POJO equivalent of an atom entry tag
 */


@Keep
@Root(name = RSS.ENTRY_TAG, strict = false)
public class AtomFeed {
    @Element(required = false)
    public String id;
    @ElementList(inline = true)
    public List<AtomLink> link;
    @Element(required = false)
    public String title;
    @Element(required = false)
    public AtomAuthor author;
    @Element(required = false)
    public String published;
    @Element(required = false)
    public String content;
    @Element(name = "encoded", data = true, required = false)
    @Namespace(prefix = "content", reference = "readablyapp.com/xml/content")
    public String contentEncoded;


}
