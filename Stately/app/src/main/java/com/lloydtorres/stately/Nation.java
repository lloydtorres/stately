package com.lloydtorres.stately;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Created by Lloyd on 2016-01-10.
 */
@Root(name="NATION", strict=false)
public class Nation {

    public static final String QUERY = "http://www.nationstates.net/cgi-bin/api.cgi?nation=%s&q="
                                        + "name+type+motto+category+region+flag+banner";

    @Element(name="NAME")
    public String name;
    @Element(name="TYPE")
    public String prename;
    @Element(name="MOTTO")
    public String motto;
    @Element(name="CATEGORY")
    public String govType;
    @Element(name="REGION")
    public String region;
    @Element(name="FLAG")
    public String flagURL;
    @Element(name="BANNER")
    public String bannerKey;

    public Nation()
    {
        super();
    }
}
