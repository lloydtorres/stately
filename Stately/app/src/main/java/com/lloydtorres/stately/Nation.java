package com.lloydtorres.stately;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-10.
 */
@Root(name="NATION", strict=false)
public class Nation implements Parcelable {

    public static final String QUERY = "http://www.nationstates.net/cgi-bin/api.cgi?nation=%s&q="
                                        + "banner+flag+name+type+wa"
                                        + "+category+region+population+founded+lastactivity+motto"
                                        + "+freedom+freedomscores"
                                        + "+customleader+customcapital+govtpriority+tax"
                                        + "+currency+gdp+income+majorindustry"
                                        + "+demonym+demonym2+demonym2plural+customreligion+animal"
                                        + "+endorsements"
                                        + "+notable+sensibilities+crime+deaths"
                                        + "+govtdesc+govt"
                                        + "+industrydesc+poorest+richest+sectors"
                                        + "+happenings";

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
    @Element(name="FOUNDED")
    public String foundedAgo;
    @Element(name="LASTACTIVITY")
    public String lastActivityAgo;
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

    @Element(name="NOTABLE")
    public String notable;
    @Element(name="SENSIBILITIES")
    public String sensible;
    @Element(name="CRIME")
    public String crime;
    @Element(name="DEATHS")
    public Mortality mortalityRoot;

    @Element(name="GOVTDESC")
    public String govtDesc;
    @Element(name="GOVT")
    public GovBudget govBudget;

    @Element(name="INDUSTRYDESC")
    public String industryDesc;
    @Element(name="POOREST")
    public long poorest;
    @Element(name="RICHEST")
    public long richest;
    @Element(name="SECTORS")
    public Sectors sectors;

    @Element(name="HAPPENINGS")
    public Happenings happeningsRoot;

    public Nation()
    {
        super();
    }

    protected Nation(Parcel in) {
        bannerKey = in.readString();
        flagURL = in.readString();
        name = in.readString();
        prename = in.readString();
        waState = in.readString();
        govType = in.readString();
        region = in.readString();
        popBase = in.readInt();
        foundedAgo = in.readString();
        lastActivityAgo = in.readString();
        motto = in.readString();
        freedomDesc = (Freedom) in.readValue(Freedom.class.getClassLoader());
        freedomPts = (FreedomScores) in.readValue(FreedomScores.class.getClassLoader());
        leader = in.readString();
        capital = in.readString();
        govtPriority = in.readString();
        tax = in.readDouble();
        currency = in.readString();
        gdp = in.readLong();
        income = in.readLong();
        industry = in.readString();
        demAdjective = in.readString();
        demNoun = in.readString();
        demPlural = in.readString();
        religion = in.readString();
        animal = in.readString();
        endorsements = in.readString();
        notable = in.readString();
        sensible = in.readString();
        crime = in.readString();
        mortalityRoot = (Mortality) in.readValue(Mortality.class.getClassLoader());
        govtDesc = in.readString();
        govBudget = (GovBudget) in.readValue(GovBudget.class.getClassLoader());
        industryDesc = in.readString();
        poorest = in.readLong();
        richest = in.readLong();
        sectors = (Sectors) in.readValue(Sectors.class.getClassLoader());
        happeningsRoot = (Happenings) in.readValue(Happenings.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bannerKey);
        dest.writeString(flagURL);
        dest.writeString(name);
        dest.writeString(prename);
        dest.writeString(waState);
        dest.writeString(govType);
        dest.writeString(region);
        dest.writeInt(popBase);
        dest.writeString(foundedAgo);
        dest.writeString(lastActivityAgo);
        dest.writeString(motto);
        dest.writeValue(freedomDesc);
        dest.writeValue(freedomPts);
        dest.writeString(leader);
        dest.writeString(capital);
        dest.writeString(govtPriority);
        dest.writeDouble(tax);
        dest.writeString(currency);
        dest.writeLong(gdp);
        dest.writeLong(income);
        dest.writeString(industry);
        dest.writeString(demAdjective);
        dest.writeString(demNoun);
        dest.writeString(demPlural);
        dest.writeString(religion);
        dest.writeString(animal);
        dest.writeString(endorsements);
        dest.writeString(notable);
        dest.writeString(sensible);
        dest.writeString(crime);
        dest.writeValue(mortalityRoot);
        dest.writeString(govtDesc);
        dest.writeValue(govBudget);
        dest.writeString(industryDesc);
        dest.writeLong(poorest);
        dest.writeLong(richest);
        dest.writeValue(sectors);
        dest.writeValue(happeningsRoot);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Nation> CREATOR = new Parcelable.Creator<Nation>() {
        @Override
        public Nation createFromParcel(Parcel in) {
            return new Nation(in);
        }

        @Override
        public Nation[] newArray(int size) {
            return new Nation[size];
        }
    };
}
