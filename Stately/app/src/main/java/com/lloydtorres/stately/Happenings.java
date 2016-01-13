package com.lloydtorres.stately;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-12.
 */
@Root(name="HAPPENINGS", strict=false)
public class Happenings {

    @ElementList(inline=true)
    public List<HappeningEvents> events;

    public Happenings() { super(); }
}
