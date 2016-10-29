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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-10.
 * The main DTO used to store information about a nation, as returned by the NationStates API.
 */
@Root(name="NATION", strict=false)
public class Nation implements Parcelable {

    public static final String BASE_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?nation=%s&q="
                                                + "banner+flag+name+type+wa"
                                                + "+category+region+influence+population+founded+lastactivity+motto"
                                                + "+freedom"
                                                + "+customleader+customcapital+govtpriority+tax"
                                                + "+currency+gdp+income+majorindustry"
                                                + "+demonym+demonym2+demonym2plural+customreligion+animal+animaltrait"
                                                + "+census+wcensus"
                                                + "+gavote+scvote+endorsements"
                                                + "+notable+sensibilities+crime+deaths"
                                                + "+govtdesc+govt"
                                                + "+industrydesc+poorest+richest+sectors"
                                                + "+happenings+zombie";

    public static final String CENSUS_MODIFIER = ";scale=all;mode=score+rank+rrank+prank+prrank";

    public static final String QUERY = BASE_QUERY + CENSUS_MODIFIER + "&v=" + SparkleHelper.API_VERSION;

    public static final String QUERY_HTML = SparkleHelper.BASE_URI_NOSLASH + "/nation=%s/template-overall=none";

    // String template used to get nation banners from NationStates
    // @param: banner_id
    public static final String BANNER_TEMPLATE = SparkleHelper.BASE_URI_NOSLASH + "/images/banners/%s.jpg";

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

    @Element(name="LEADER", required=false)
    public String leader;
    @Element(name="CAPITAL", required=false)
    public String capital;
    @Element(name="GOVTPRIORITY", required=false)
    public String govtPriority;
    @Element(name="TAX")
    public float tax;

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
    @Element(name="ANIMALTRAIT", required=false)
    public String animalTrait;

    @ElementList(name="CENSUS")
    public List<CensusDetailedRank> census;
    @Element(name="WCENSUS", required=false)
    public CensusBasicRank wCensus;

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

    @Element(name="ZOMBIE")
    public Zombie zombieData;

    public Nation() { super(); }

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
        leader = in.readString();
        capital = in.readString();
        govtPriority = in.readString();
        tax = in.readFloat();
        currency = in.readString();
        gdp = in.readLong();
        income = in.readLong();
        industry = in.readString();
        demAdjective = in.readString();
        demNoun = in.readString();
        demPlural = in.readString();
        religion = in.readString();
        animal = in.readString();
        animalTrait = in.readString();
        if (in.readByte() == 0x01) {
            census = new ArrayList<CensusDetailedRank>();
            in.readList(census, CensusDetailedRank.class.getClassLoader());
        } else {
            census = null;
        }
        wCensus = (CensusBasicRank) in.readValue(CensusBasicRank.class.getClassLoader());
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
        zombieData = (Zombie) in.readValue(Zombie.class.getClassLoader());
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
        dest.writeString(leader);
        dest.writeString(capital);
        dest.writeString(govtPriority);
        dest.writeFloat(tax);
        dest.writeString(currency);
        dest.writeLong(gdp);
        dest.writeLong(income);
        dest.writeString(industry);
        dest.writeString(demAdjective);
        dest.writeString(demNoun);
        dest.writeString(demPlural);
        dest.writeString(religion);
        dest.writeString(animal);
        dest.writeString(animalTrait);
        if (census == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(census);
        }
        dest.writeValue(wCensus);
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
        dest.writeValue(zombieData);
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


    /**
     * Return the URL of a nation banner.
     * @param id The banner ID.
     * @return The URL to the banner.
     */
    public static String getBannerURL(String id)
    {
        return String.format(Locale.US, BANNER_TEMPLATE, id);
    }

    /**
     * Factory for deserializing a Nation XML.
     * @param c App context
     * @param serializer SimpleXML deserializer
     * @param response XML response
     * @return Nation object
     * @throws Exception
     */
    public static Nation parseNationFromXML(Context c, Persister serializer, String response) throws Exception {
        Nation nationResponse = serializer.read(Nation.class, response);
        return fieldReplacer(c, nationResponse);
    }

    protected static Nation fieldReplacer(Context c, Nation nationResponse) {
        // Switch flag URL to https
        nationResponse.flagURL = nationResponse.flagURL.replace("http://","https://");

        // Map out government priorities
        if (nationResponse.govtPriority != null) {
            switch (nationResponse.govtPriority)
            {
                case "Defence":
                    nationResponse.govtPriority = c.getString(R.string.defense);
                    break;
                case "Commerce":
                    nationResponse.govtPriority = c.getString(R.string.industry);
                    break;
                case "Social Equality":
                    nationResponse.govtPriority = c.getString(R.string.social_policy);
                    break;
            }
        }

        return nationResponse;
    }
}
