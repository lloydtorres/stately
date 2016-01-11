package com.lloydtorres.stately;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-10.
 */
@Root(name="NATION", strict=false)
public class Nation {

    @Element(name="NAME")
    public String name;
    @Element(name="TYPE")
    public String prename;
    @Element(name="FLAG")
    public String flagURL;
    @Element(name="BANNER")
    public String bannerKey;

    public Nation()
    {
        super();
    }
}
