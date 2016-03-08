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
 * Created by Lloyd on 2016-01-12.
 * A list of MortalityCause DTOs as returned by the NationStates API. Formatted this way due to
 * the structure of the XML returned by the API, and its easier to deserialize like this.
 */
@Root(name="DEATHS", strict=false)
public class Mortality implements Parcelable {

    @ElementList(inline=true)
    public List<MortalityCause> causes;

    public Mortality() { super(); }

    protected Mortality(Parcel in) {
        if (in.readByte() == 0x01) {
            causes = new ArrayList<MortalityCause>();
            in.readList(causes, MortalityCause.class.getClassLoader());
        } else {
            causes = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (causes == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(causes);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Mortality> CREATOR = new Parcelable.Creator<Mortality>() {
        @Override
        public Mortality createFromParcel(Parcel in) {
            return new Mortality(in);
        }

        @Override
        public Mortality[] newArray(int size) {
            return new Mortality[size];
        }
    };
}
