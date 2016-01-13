package com.lloydtorres.stately;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-12.
 */
@Root(name="SECTORS", strict=false)
public class Sectors {

    @Element(name="BLACKMARKET")
    public double blackMarket;
    @Element(name="GOVERNMENT")
    public double government;
    @Element(name="INDUSTRY")
    public double privateSector;
    @Element(name="PUBLIC")
    public double stateOwned;

    public Sectors() { super(); }
}
