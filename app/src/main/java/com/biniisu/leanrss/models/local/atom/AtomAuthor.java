package com.biniisu.leanrss.models.local.atom;

import android.support.annotation.Keep;

import com.biniisu.leanrss.connectivity.local.RSS;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by biniam_Haddish on 30/11/17.
 * <p>
 * This class the POJO equivalent of an atom author tag
 */

@Keep
@Root(name = RSS.AUTHOR_TAG, strict = false)
public class AtomAuthor {

    @Element(required = false)
    public String name;
    @Element(required = false)
    public String uri;
    @Element(required = false)
    public String email;


}
