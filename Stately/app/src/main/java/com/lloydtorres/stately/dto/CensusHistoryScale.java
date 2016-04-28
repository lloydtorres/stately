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
 * Created by Lloyd on 2016-04-10.
 * Holder for a given census scale's history.
 */
@Root(name="CENSUS", strict=false)
public class CensusHistoryScale implements Parcelable {

    @ElementList(name="SCALE", required=false)
    public List<CensusHistoryPoint> points;

    public CensusHistoryScale() { super(); }

    protected CensusHistoryScale(Parcel in) {
        if (in.readByte() == 0x01) {
            points = new ArrayList<CensusHistoryPoint>();
            in.readList(points, CensusHistoryPoint.class.getClassLoader());
        } else {
            points = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (points == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(points);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusHistoryScale> CREATOR = new Parcelable.Creator<CensusHistoryScale>() {
        @Override
        public CensusHistoryScale createFromParcel(Parcel in) {
            return new CensusHistoryScale(in);
        }

        @Override
        public CensusHistoryScale[] newArray(int size) {
            return new CensusHistoryScale[size];
        }
    };
}
