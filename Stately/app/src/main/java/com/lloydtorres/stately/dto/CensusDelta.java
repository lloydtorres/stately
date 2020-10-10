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

import androidx.annotation.NonNull;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-03-02.
 * This is a model containing data on the change for one census stat after an issue result.
 */
@Root(name = "RANK", strict = false)
public class CensusDelta implements Parcelable, Comparable<CensusDelta> {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusDelta> CREATOR =
            new Parcelable.Creator<CensusDelta>() {
        @Override
        public CensusDelta createFromParcel(Parcel in) {
            return new CensusDelta(in);
        }

        @Override
        public CensusDelta[] newArray(int size) {
            return new CensusDelta[size];
        }
    };
    @Attribute(name = "id", required = false)
    public int censusId;
    @Element(name = "PCHANGE", required = false)
    public double percentDelta;

    public CensusDelta() {
        super();
    }

    protected CensusDelta(Parcel in) {
        censusId = in.readInt();
        percentDelta = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(censusId);
        dest.writeDouble(percentDelta);
    }

    @Override
    public int compareTo(@NonNull CensusDelta o) {
        if (this.percentDelta > o.percentDelta) {
            return -1;
        } else if (this.percentDelta < o.percentDelta) {
            return 1;
        } else {
            return 0;
        }
    }
}
