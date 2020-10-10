/**
 * Copyright 2018 Lloyd Torres
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
 * Created by lloyd on 2018-02-09.
 * Contains data about the different census scales available in NationStates.
 */
public class CensusScale implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusScale> CREATOR =
            new Parcelable.Creator<CensusScale>() {
                @Override
                public CensusScale createFromParcel(Parcel in) {
                    return new CensusScale(in);
                }

                @Override
                public CensusScale[] newArray(int size) {
                    return new CensusScale[size];
                }
            };
    public int id;
    public String name;
    public String unit;
    public String banner;

    public CensusScale() {
        super();
    }

    protected CensusScale(Parcel in) {
        id = in.readInt();
        name = in.readString();
        unit = in.readString();
        banner = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(unit);
        dest.writeString(banner);
    }
}
