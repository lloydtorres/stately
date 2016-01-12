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
                                        + "banner+flag+name+type+wa"
                                        + "+category+region+population+motto+freedom+freedomscores"
                                        + "+customleader+customcapital+govtpriority+tax"
                                        + "+currency+gdp+income+majorindustry"
                                        + "+demonym+demonym2+demonym2plural+customreligion+animal"
                                        + "+endorsements";

    @Element(name="BANNER")
    public String bannerKey;
    @Element(name="FLAG")
    public String flagURL;
    @Element(name="NAME")
    public String name;
    @Element(name="TYPE")
    public String prename;
    @Element(name="UNSTATUS")
    public String waState;

    @Element(name="CATEGORY")
    public String govType;
    @Element(name="REGION")
    public String region;
    @Element(name="POPULATION")
    public int popBase;
    @Element(name="MOTTO")
    public String motto;

    @Element(name="FREEDOM")
    public Freedom freedomDesc;
    @Element(name="FREEDOMSCORES")
    public FreedomScores freedomPts;

    @Element(name="LEADER", required=false)
    public String leader;
    @Element(name="CAPITAL", required=false)
    public String capital;
    @Element(name="GOVTPRIORITY")
    public String govtPriority;
    @Element(name="TAX")
    public double tax;

    @Element(name="CURRENCY")
    public String currency;
    @Element(name="GDP")
    public long gdp;
    @Element(name="INCOME")
    public long income;
    @Element(name="MAJORINDUSTRY")
    public String industry;

    @Element(name="DEMONYM")
    public String demAdjective;
    @Element(name="DEMONYM2")
    public String demNoun;
    @Element(name="DEMONYM2PLURAL")
    public String demPlural;
    @Element(name="RELIGION", required=false)
    public String religion;
    @Element(name="ANIMAL")
    public String animal;

    @Element(name="ENDORSEMENTS", required=false)
    public String endorsements;

    public Nation()
    {
        super();
    }
}
