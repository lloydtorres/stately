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
 * Created by Lloyd on 2016-04-10.
 * This is a single point in a census scale's history.
 */
@Root(name="POINT", strict=false)
public class CensusHistoryPoint implements Parcelable {

    @Element(name="TIMESTAMP", required=false)
    public long timestamp;
    @Element(name="SCORE", required=false)
    public float score;

    public CensusHistoryPoint() { super(); }

    protected CensusHistoryPoint(Parcel in) {
        timestamp = in.readLong();
        score = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeFloat(score);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusHistoryPoint> CREATOR = new Parcelable.Creator<CensusHistoryPoint>() {
        @Override
        public CensusHistoryPoint createFromParcel(Parcel in) {
            return new CensusHistoryPoint(in);
        }

        @Override
        public CensusHistoryPoint[] newArray(int size) {
            return new CensusHistoryPoint[size];
        }
    };
}
