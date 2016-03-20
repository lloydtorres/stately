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
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by Lloyd on 2016-02-21.
 * Stores information on a given census score from a nation.
 */
@Root(name="CENSUSSCORE", strict=false)
public class CensusScore implements Parcelable {
    @Attribute
    public int id;
    @Text
    public float value;

    public CensusScore() { super(); }

    protected CensusScore(Parcel in) {
        id = in.readInt();
        value = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeFloat(value);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusScore> CREATOR = new Parcelable.Creator<CensusScore>() {
        @Override
        public CensusScore createFromParcel(Parcel in) {
            return new CensusScore(in);
        }

        @Override
        public CensusScore[] newArray(int size) {
            return new CensusScore[size];
        }
    };
}
