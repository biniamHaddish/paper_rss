package com.biniam.rss.models.local.atom;

import android.support.annotation.Keep;

import com.biniam.rss.connectivity.local.RSS;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

/**
 * Created by biniam_Haddish on 30/11/17.
 * <p>
 * This class the POJO equivalent of an atom xml
 */


@Keep
@Root(name = RSS.FEED_TAG, strict = false)
public class Atom {

    @ElementList(inline = true, required = false)
    public ArrayList<AtomFeed> feeds;
}
