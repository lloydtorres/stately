package com.lloydtorres.stately;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by Lloyd on 2016-01-12.
 */
@Root(name="CAUSE", strict=false)
public class MortalityCause {

    @Attribute
    public String type;
    @Text
    public double value;
}
