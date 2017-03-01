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

/**
 * Created by Lloyd on 2016-07-24.
 * A holder for data in the main nation overview card.
 */
public class NationOverviewCardData implements Parcelable {
    public String category;
    public String region;
    public String inflDesc;
    public float inflScore;
    public int population;
    public String motto;
    public long established;
    public long lastSeen;

    public String waState;
    public String endorsements;
    public String gaVote;
    public String scVote;

    public NationOverviewCardData() { super(); }

    protected NationOverviewCardData(Parcel in) {
        category = in.readString();
        region = in.readString();
        inflDesc = in.readString();
        inflScore = in.readFloat();
        population = in.readInt();
        motto = in.readString();
        established = in.readLong();
        lastSeen = in.readLong();
        waState = in.readString();
        endorsements = in.readString();
        gaVote = in.readString();
        scVote = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(region);
        dest.writeString(inflDesc);
        dest.writeFloat(inflScore);
        dest.writeInt(population);
        dest.writeString(motto);
        dest.writeLong(established);
        dest.writeLong(lastSeen);
        dest.writeString(waState);
        dest.writeString(endorsements);
        dest.writeString(gaVote);
        dest.writeString(scVote);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NationOverviewCardData> CREATOR = new Parcelable.Creator<NationOverviewCardData>() {
        @Override
        public NationOverviewCardData createFromParcel(Parcel in) {
            return new NationOverviewCardData(in);
        }

        @Override
        public NationOverviewCardData[] newArray(int size) {
            return new NationOverviewCardData[size];
        }
    };
}
