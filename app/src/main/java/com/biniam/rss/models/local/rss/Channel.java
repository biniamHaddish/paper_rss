package com.biniam.rss.models.local.rss;

import android.support.annotation.Keep;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;


@Keep
public class Channel {

    @Element
    public String title;

    @ElementList
    public ArrayList<RssFeed> items;
}