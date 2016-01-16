package com.lloydtorres.stately.dto;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-16.
 */
@Root(name="WA", strict=false)
public class Assembly {

    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?wa=%d&q="
                                        + "resolution+votetrack"
                                        + "+lastresolution"
                                        + "+numnations+numdelegates+happenings";

    @Element(name="RESOLUTION", required=false)
    public Resolution resolution;

    @Element(name="LASTRESOLUTION")
    public String lastResolution;

    @Element(name="NUMNATIONS")
    public int numNations;
    @Element(name="NUMDELEGATES")
    public int numDelegates;
    @Element(name="HAPPENINGS")
    public Happenings happeningsRoot;
}
