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
 * Holder for a given census scale's history.
 */
@Root(name="CENSUS", strict=false)
public class CensusHistoryHolder implements Parcelable {

    @Element(name="SCALE")
    public CensusHistoryScale scale;

    public CensusHistoryHolder() { super(); }

    protected CensusHistoryHolder(Parcel in) {
        scale = (CensusHistoryScale) in.readValue(CensusHistoryScale.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(scale);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusHistoryHolder> CREATOR = new Parcelable.Creator<CensusHistoryHolder>() {
        @Override
        public CensusHistoryHolder createFromParcel(Parcel in) {
            return new CensusHistoryHolder(in);
        }

        @Override
        public CensusHistoryHolder[] newArray(int size) {
            return new CensusHistoryHolder[size];
        }
    };

}
