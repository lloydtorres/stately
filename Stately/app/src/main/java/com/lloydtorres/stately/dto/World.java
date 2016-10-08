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
 * Created by Lloyd on 2016-09-11.
 * Model used for storing world data.
 */
@Root(name="WORLD", strict=false)
public class World implements Parcelable {
    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?q="
                                        + "numnations+numregions+featuredregion+happenings"
                                        + "+census+censusid;scale=all;limit=5"
                                        + "&v=" + SparkleHelper.API_VERSION;

    @Element(name="NUMNATIONS")
    public int numNations;
    @Element(name="NUMREGIONS")
    public int numRegions;
    @Element(name="FEATUREDREGION")
    public String featuredRegion;

    @ElementList(name="HAPPENINGS")
    public List<Event> happenings;

    @Element(name="CENSUSID")
    public int featuredCensus;
    @ElementList(name="CENSUS")
    public List<CensusDetailedRank> census;

    public World() { super(); }

    protected World(Parcel in) {
        numNations = in.readInt();
        numRegions = in.readInt();
        featuredRegion = in.readString();
        if (in.readByte() == 0x01) {
            happenings = new ArrayList<Event>();
            in.readList(happenings, Event.class.getClassLoader());
        } else {
            happenings = null;
        }
        featuredCensus = in.readInt();
        if (in.readByte() == 0x01) {
            census = new ArrayList<CensusDetailedRank>();
            in.readList(census, CensusDetailedRank.class.getClassLoader());
        } else {
            census = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(numNations);
        dest.writeInt(numRegions);
        dest.writeString(featuredRegion);
        if (happenings == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(happenings);
        }
        dest.writeInt(featuredCensus);
        if (census == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(census);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<World> CREATOR = new Parcelable.Creator<World>() {
        @Override
        public World createFromParcel(Parcel in) {
            return new World(in);
        }

        @Override
        public World[] newArray(int size) {
            return new World[size];
        }
    };
}
