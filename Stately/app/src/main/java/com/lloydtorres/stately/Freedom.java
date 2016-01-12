package com.lloydtorres.stately;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-11.
 */
@Root(name="FREEDOM", strict=false)
public class Freedom {

    @Element(name="CIVILRIGHTS")
    public String civilRightsDesc;
    @Element(name="ECONOMY")
    public String economyDesc;
    @Element(name="POLITICALFREEDOM")
    public String politicalDesc;

    public Freedom() {
        super();
    }
}
