package com.lloydtorres.stately;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-12.
 */
@Root(name="DEATHS", strict=false)
public class Mortality {

    @ElementList(inline=true)
    public List<MortalityCause> causes;

    public Mortality() { super(); }
}
