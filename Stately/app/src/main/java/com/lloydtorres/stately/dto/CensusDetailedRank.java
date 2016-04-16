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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-04-09.
 * This is the model used for storing data on one census scale, such as its world/region rank
 * (both in percent and absolute rank) as well as the actual score for that scale.
 */
@Root(name="SCALE", strict=false)
public class CensusDetailedRank implements Parcelable {

    @Attribute
    public int id;
    @Element(name="SCORE")
    public float score;
    @Element(name="RANK", required=false)
    public int worldRank;
    @Element(name="PRANK", required=false)
    public float worldRankPercent;
    @Element(name="RRANK", required=false)
    public int regionRank;
    @Element(name="PRRANK", required=false)
    public float regionRankPercent;

    public CensusDetailedRank() { super(); }

    protected CensusDetailedRank(Parcel in) {
        id = in.readInt();
        score = in.readFloat();
        worldRank = in.readInt();
        worldRankPercent = in.readFloat();
        regionRank = in.readInt();
        regionRankPercent = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeFloat(score);
        dest.writeInt(worldRank);
        dest.writeFloat(worldRankPercent);
        dest.writeInt(regionRank);
        dest.writeFloat(regionRankPercent);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusDetailedRank> CREATOR = new Parcelable.Creator<CensusDetailedRank>() {
        @Override
        public CensusDetailedRank createFromParcel(Parcel in) {
            return new CensusDetailedRank(in);
        }

        @Override
        public CensusDetailedRank[] newArray(int size) {
            return new CensusDetailedRank[size];
        }
    };
}
