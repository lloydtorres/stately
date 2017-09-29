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
 * Created by Lloyd on 2016-09-14.
 * Model holding data for the 'Quick Facts' card in Region > Overview.
 */
public class RegionQuickFactsCardData implements Parcelable {
    public String waDelegate;
    public int delegateVotes;
    public long lastUpdate;
    public String founder;
    public long founded;
    public String power;

    public RegionQuickFactsCardData() { super(); }

    protected RegionQuickFactsCardData(Parcel in) {
        waDelegate = in.readString();
        delegateVotes = in.readInt();
        lastUpdate = in.readLong();
        founder = in.readString();
        founded = in.readLong();
        power = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(waDelegate);
        dest.writeInt(delegateVotes);
        dest.writeLong(lastUpdate);
        dest.writeString(founder);
        dest.writeLong(founded);
        dest.writeString(power);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RegionQuickFactsCardData> CREATOR = new Parcelable.Creator<RegionQuickFactsCardData>() {
        @Override
        public RegionQuickFactsCardData createFromParcel(Parcel in) {
            return new RegionQuickFactsCardData(in);
        }

        @Override
        public RegionQuickFactsCardData[] newArray(int size) {
            return new RegionQuickFactsCardData[size];
        }
    };
}
