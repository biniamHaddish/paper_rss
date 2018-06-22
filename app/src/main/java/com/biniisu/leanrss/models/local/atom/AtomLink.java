package com.biniisu.leanrss.models.local.atom;

import android.support.annotation.Keep;

import com.biniisu.leanrss.connectivity.local.RSS;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by biniam_Haddish on 30/11/17.
 * <p>
 * This class the POJO equivalent of an atom link tag
 */


@Keep
@Root(name = RSS.LINK_TAG, strict = false)
public class AtomLink {
    @Attribute
    public String href;
}
