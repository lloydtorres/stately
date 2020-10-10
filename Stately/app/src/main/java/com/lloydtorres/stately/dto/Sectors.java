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
 * Created by Lloyd on 2016-01-12.
 * A DTO that stores information on a nation's economic sectors, as returned by the
 * NationStates API.
 */
@Root(name = "SECTORS", strict = false)
public class Sectors implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Sectors> CREATOR = new Parcelable.Creator<Sectors>() {
        @Override
        public Sectors createFromParcel(Parcel in) {
            return new Sectors(in);
        }

        @Override
        public Sectors[] newArray(int size) {
            return new Sectors[size];
        }
    };
    @Element(name = "BLACKMARKET")
    public float blackMarket;
    @Element(name = "GOVERNMENT")
    public float government;
    @Element(name = "INDUSTRY")
    public float privateSector;
    @Element(name = "PUBLIC")
    public float stateOwned;

    public Sectors() {
        super();
    }

    protected Sectors(Parcel in) {
        blackMarket = in.readFloat();
        government = in.readFloat();
        privateSector = in.readFloat();
        stateOwned = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(blackMarket);
        dest.writeFloat(government);
        dest.writeFloat(privateSector);
        dest.writeFloat(stateOwned);
    }
}
