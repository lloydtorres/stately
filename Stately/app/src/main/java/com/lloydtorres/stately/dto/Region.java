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

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Lloyd on 2016-01-21.
 * The DTO used to store information about a region, as returned by the NationStates API.
 * Excludes messages, which is stored in a separate DTO.
 */
@Root(name = "REGION", strict = false)
public class Region extends BaseRegion implements Parcelable {

    public static final String QUERY = BaseRegion.BASE_QUERY
            + "+power+wabadges+bannerurl"
            + "+poll+gavote+scvote"
            + "+officers+embassies+banlist"
            + "+happenings+history"
            + "+zombie+census"
            + ";scale=all;mode=score+rank+prank"
            + "&v=" + SparkleHelper.API_VERSION;
    public static final String QUERY_HTML = SparkleHelper.BASE_URI_NOSLASH + "/region=%s/template" +
            "-overall=none";
    public static final String CHANGE_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page" +
            "=change_region" + SparkleHelper.TEMPLATE_NONE;
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Region> CREATOR = new Parcelable.Creator<Region>() {
        @Override
        public Region createFromParcel(Parcel in) {
            return new Region(in);
        }

        @Override
        public Region[] newArray(int size) {
            return new Region[size];
        }
    };
    @Element(name = "POWER")
    public String power;
    @ElementList(name = "WABADGES", required = false)
    public List<WaBadge> waBadges;
    @Element(name = "BANNERURL")
    public String bannerUrl;
    @Element(name = "BANNED", required = false)
    public String rawBanList;
    @Element(name = "POLL", required = false)
    public Poll poll;
    @Element(name = "GAVOTE", required = false)
    public WaVote gaVote;
    @Element(name = "SCVOTE", required = false)
    public WaVote scVote;
    @ElementList(name = "OFFICERS", required = false)
    public List<Officer> officers;
    @ElementList(name = "EMBASSIES", required = false)
    public List<Embassy> embassies;
    @ElementList(name = "CENSUS")
    public List<CensusDetailedRank> census;
    @ElementList(name = "HAPPENINGS")
    public List<Event> happenings;
    @ElementList(name = "HISTORY")
    public List<Event> history;
    @Element(name = "ZOMBIE")
    public Zombie zombieData;

    private Set<String> banListSet;

    public Region() {
        super();
    }

    protected Region(Parcel in) {
        super(in);
        power = in.readString();
        if (in.readByte() == 0x01) {
            waBadges = new ArrayList<WaBadge>();
            in.readList(waBadges, WaBadge.class.getClassLoader());
        } else {
            waBadges = null;
        }
        bannerUrl = in.readString();
        rawBanList = in.readString();
        poll = (Poll) in.readValue(Poll.class.getClassLoader());
        gaVote = (WaVote) in.readValue(WaVote.class.getClassLoader());
        scVote = (WaVote) in.readValue(WaVote.class.getClassLoader());
        if (in.readByte() == 0x01) {
            officers = new ArrayList<Officer>();
            in.readList(officers, Officer.class.getClassLoader());
        } else {
            officers = null;
        }
        if (in.readByte() == 0x01) {
            embassies = new ArrayList<Embassy>();
            in.readList(embassies, Embassy.class.getClassLoader());
        } else {
            embassies = null;
        }
        if (in.readByte() == 0x01) {
            census = new ArrayList<CensusDetailedRank>();
            in.readList(census, CensusDetailedRank.class.getClassLoader());
        } else {
            census = null;
        }
        if (in.readByte() == 0x01) {
            happenings = new ArrayList<Event>();
            in.readList(happenings, Event.class.getClassLoader());
        } else {
            happenings = null;
        }
        if (in.readByte() == 0x01) {
            history = new ArrayList<Event>();
            in.readList(history, Event.class.getClassLoader());
        } else {
            history = null;
        }
        zombieData = (Zombie) in.readValue(Zombie.class.getClassLoader());
    }

    public static Region parseRegionXML(Context c, Persister serializer, String response) throws Exception {
        Region regionResponse = serializer.read(Region.class, response);
        regionResponse = ((Region) processRawFields(c, regionResponse));
        // Convert poll text BBcode to HTML if it exists
        if (regionResponse.poll != null) {
            regionResponse.poll.text = SparkleHelper.transformBBCodeToHtml(c,
                    regionResponse.poll.text);
        }
        return regionResponse;
    }

    public boolean isNationBannedFromRegion(final String nationId) {
        if (banListSet == null) {
            banListSet = new HashSet<>();
        }

        if (rawBanList != null) {
            banListSet.addAll(Arrays.asList(rawBanList.split(":")));
        }

        return banListSet.contains(nationId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(power);
        if (waBadges == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(waBadges);
        }
        dest.writeString(bannerUrl);
        dest.writeString(rawBanList);
        dest.writeValue(poll);
        dest.writeValue(gaVote);
        dest.writeValue(scVote);
        if (officers == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(officers);
        }
        if (embassies == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(embassies);
        }
        if (census == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(census);
        }
        if (happenings == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(happenings);
        }
        if (history == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(history);
        }
        dest.writeValue(zombieData);
    }
}
