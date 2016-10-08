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

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-09-10.
 * A model holding a list of nation census rankings.
 */
@Root(name="CENSUSRANKS", strict=false)
public class CensusNationRankList implements Parcelable {
    @ElementList(name="NATIONS")
    public List<CensusNationRank> ranks;

    public CensusNationRankList() {
        super();
    }

    protected CensusNationRankList(Parcel in) {
        if (in.readByte() == 0x01) {
            ranks = new ArrayList<CensusNationRank>();
            in.readList(ranks, CensusNationRank.class.getClassLoader());
        } else {
            ranks = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (ranks == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(ranks);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusNationRankList> CREATOR = new Parcelable.Creator<CensusNationRankList>() {
        @Override
        public CensusNationRankList createFromParcel(Parcel in) {
            return new CensusNationRankList(in);
        }

        @Override
        public CensusNationRankList[] newArray(int size) {
            return new CensusNationRankList[size];
        }
    };
}
