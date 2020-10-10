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
 * Convenient model to hold parcelable region factbook string for Region > Overview.
 */
public class RegionFactbookCardData implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RegionFactbookCardData> CREATOR =
            new Parcelable.Creator<RegionFactbookCardData>() {
        @Override
        public RegionFactbookCardData createFromParcel(Parcel in) {
            return new RegionFactbookCardData(in);
        }

        @Override
        public RegionFactbookCardData[] newArray(int size) {
            return new RegionFactbookCardData[size];
        }
    };
    public String factbook;

    public RegionFactbookCardData() {
        super();
    }

    protected RegionFactbookCardData(Parcel in) {
        factbook = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(factbook);
    }
}
