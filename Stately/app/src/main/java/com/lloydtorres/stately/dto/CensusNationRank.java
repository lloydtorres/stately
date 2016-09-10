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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-09-10.
 * Model for the census rankings for a particular nation.
 */
@Root(name="NATION", strict=false)
public class CensusNationRank implements Parcelable, Comparable<CensusNationRank> {
    @Element(name="NAME")
    public String name;
    @Element(name="RANK")
    public int rank;
    @Element(name="SCORE")
    public float score;

    public CensusNationRank() {
        super();
    }

    protected CensusNationRank(Parcel in) {
        name = in.readString();
        rank = in.readInt();
        score = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(rank);
        dest.writeFloat(score);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusNationRank> CREATOR = new Parcelable.Creator<CensusNationRank>() {
        @Override
        public CensusNationRank createFromParcel(Parcel in) {
            return new CensusNationRank(in);
        }

        @Override
        public CensusNationRank[] newArray(int size) {
            return new CensusNationRank[size];
        }
    };

    @Override
    public int compareTo(CensusNationRank another) {
        if (this.rank > another.rank)
        {
            return 1;
        }
        else if (this.rank == another.rank)
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }
}
