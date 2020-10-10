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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

/**
 * Created by Lloyd on 2016-02-21.
 * Stores information on a given census score from a nation.
 */
public class CensusBasicRank implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusBasicRank> CREATOR =
            new Parcelable.Creator<CensusBasicRank>() {
        @Override
        public CensusBasicRank createFromParcel(Parcel in) {
            return new CensusBasicRank(in);
        }

        @Override
        public CensusBasicRank[] newArray(int size) {
            return new CensusBasicRank[size];
        }
    };
    @Attribute
    public int id;
    @Text
    public int value;

    public CensusBasicRank() {
        super();
    }

    protected CensusBasicRank(Parcel in) {
        id = in.readInt();
        value = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(value);
    }
}
