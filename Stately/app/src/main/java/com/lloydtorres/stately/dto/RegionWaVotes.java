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
import org.simpleframework.xml.Root;

/**
 * Created by lloyd on 2017-09-08.
 * Model holding data on regional WA resolution votes.
 */
@Root(name = "REGION", strict = false)
public class RegionWaVotes implements Parcelable {
    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api" +
            ".cgi?region=%s&q="
            + "gavote+scvote"
            + "&v=" + SparkleHelper.API_VERSION;
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RegionWaVotes> CREATOR =
            new Parcelable.Creator<RegionWaVotes>() {
                @Override
                public RegionWaVotes createFromParcel(Parcel in) {
                    return new RegionWaVotes(in);
                }

                @Override
                public RegionWaVotes[] newArray(int size) {
                    return new RegionWaVotes[size];
                }
            };
    public int councilId;
    public String regionName;
    @Element(name = "GAVOTE", required = false)
    public WaVote gaVote;
    @Element(name = "SCVOTE", required = false)
    public WaVote scVote;

    public RegionWaVotes() {
        super();
    }

    protected RegionWaVotes(Parcel in) {
        councilId = in.readInt();
        regionName = in.readString();
        gaVote = (WaVote) in.readValue(WaVote.class.getClassLoader());
        scVote = (WaVote) in.readValue(WaVote.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(councilId);
        dest.writeString(regionName);
        dest.writeValue(gaVote);
        dest.writeValue(scVote);
    }
}
