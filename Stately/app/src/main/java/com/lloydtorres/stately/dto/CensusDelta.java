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
 * Created by Lloyd on 2016-03-02.
 * This is a model containing data on the change for one census stat after an issue result.
 */
public class CensusDelta implements Parcelable {
    public static final String REGEX_ID = "\\/nation=.*?\\/detail=trend\\?censusid=";

    public int censusId;
    public String delta;
    public boolean isPositive;

    public CensusDelta() { super(); }

    protected CensusDelta(Parcel in) {
        censusId = in.readInt();
        delta = in.readString();
        isPositive = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(censusId);
        dest.writeString(delta);
        dest.writeByte((byte) (isPositive ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusDelta> CREATOR = new Parcelable.Creator<CensusDelta>() {
        @Override
        public CensusDelta createFromParcel(Parcel in) {
            return new CensusDelta(in);
        }

        @Override
        public CensusDelta[] newArray(int size) {
            return new CensusDelta[size];
        }
    };
}
