package com.biniisu.leanrss.models.local.rss;

import android.support.annotation.Keep;

import com.biniisu.leanrss.connectivity.local.RSS;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by biniam_Haddish on 30/11/17.
 * <p>
 * This class the POJO equivalent of an channel xml that has parent element "rss"
 */

@Keep
@Root(name = RSS.RSS_TAG, strict = false)
public class RssVar {

    @Element
    public Channel channel;

}

