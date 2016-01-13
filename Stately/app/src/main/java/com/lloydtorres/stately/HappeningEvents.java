package com.lloydtorres.stately;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-12.
 */
@Root(name="EVENT", strict=false)
public class HappeningEvents {

    @Element(name="TIMESTAMP")
    public long timestamp;
    @Element(name="TEXT")
    public String content;

    public HappeningEvents() { super(); }
}
