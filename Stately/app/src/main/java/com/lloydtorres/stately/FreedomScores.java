package com.lloydtorres.stately;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-11.
 */
@Root(name="FREEDOMSCORES", strict=false)
public class FreedomScores {

    @Element(name="CIVILRIGHTS")
    public int civilRightsPts;
    @Element(name="ECONOMY")
    public int economyPts;
    @Element(name="POLITICALFREEDOM")
    public int politicalPts;

    public FreedomScores() {
        super();
    }
}
