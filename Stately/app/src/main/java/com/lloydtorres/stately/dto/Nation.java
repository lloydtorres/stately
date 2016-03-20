/**
 * Copyright 2016 Lloyd Torres
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-10.
 * The main DTO used to store information about a nation, as returned by the NationStates API.
 */
@Root(name="NATION", strict=false)
public class Nation implements Parcelable {

    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?nation=%s&q="
                                        + "banner+flag+name+type+wa"
                                        + "+category+region+influence+population+founded+lastactivity+motto"
                                        + "+freedom+freedomscores"
                                        + "+customleader+customcapital+govtpriority+tax"
                                        + "+currency+gdp+income+majorindustry"
                                        + "+demonym+demonym2+demonym2plural+customreligion+animal"
                                        + "+censusscore+rcensus+wcensus"
                                        + "+gavote+scvote+endorsements"
                                        + "+notable+sensibilities+crime+deaths"
                                        + "+govtdesc+govt"
                                        + "+industrydesc+poorest+richest+sectors"
                                        + "+happenings"
                                        + "&v=" + SparkleHelper.API_VERSION;

    public static final String QUERY_HTML = "https://www.nationstates.net/nation=%s/template-overall=none";

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
    @Element(name="INFLUENCE")
    public String influence;
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

    @Element(name="CENSUSSCORE")
    public CensusScore censusScore;
    @Element(name="RCENSUS")
    public int rCensus;
    @Element(name="WCENSUS")
    public int wCensus;

    @Element(name="GAVOTE", required=false)
    public String gaVote;
    @Element(name="SCVOTE", required=false)
    public String scVote;
    @Element(name="ENDORSEMENTS", required=false)
    public String endorsements;

    @Element(name="NOTABLE")
    public String notable;
    @Element(name="SENSIBILITIES")
    public String sensible;
    @Element(name="CRIME")
    public String crime;
    @ElementList(name="DEATHS")
    public List<MortalityCause> causes;

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

    @ElementList(name="HAPPENINGS")
    public List<Event> events;

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
        influence = in.readString();
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
        censusScore = (CensusScore) in.readValue(CensusScore.class.getClassLoader());
        rCensus = in.readInt();
        wCensus = in.readInt();
        gaVote = in.readString();
        scVote = in.readString();
        endorsements = in.readString();
        notable = in.readString();
        sensible = in.readString();
        crime = in.readString();
        if (in.readByte() == 0x01) {
            causes = new ArrayList<MortalityCause>();
            in.readList(causes, MortalityCause.class.getClassLoader());
        } else {
            causes = null;
        }
        govtDesc = in.readString();
        govBudget = (GovBudget) in.readValue(GovBudget.class.getClassLoader());
        industryDesc = in.readString();
        poorest = in.readLong();
        richest = in.readLong();
        sectors = (Sectors) in.readValue(Sectors.class.getClassLoader());
        if (in.readByte() == 0x01) {
            events = new ArrayList<Event>();
            in.readList(events, Event.class.getClassLoader());
        } else {
            events = null;
        }
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
        dest.writeString(influence);
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
        dest.writeValue(censusScore);
        dest.writeInt(rCensus);
        dest.writeInt(wCensus);
        dest.writeString(gaVote);
        dest.writeString(scVote);
        dest.writeString(endorsements);
        dest.writeString(notable);
        dest.writeString(sensible);
        dest.writeString(crime);
        if (causes == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(causes);
        }
        dest.writeString(govtDesc);
        dest.writeValue(govBudget);
        dest.writeString(industryDesc);
        dest.writeLong(poorest);
        dest.writeLong(richest);
        dest.writeValue(sectors);
        if (events == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(events);
        }
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
