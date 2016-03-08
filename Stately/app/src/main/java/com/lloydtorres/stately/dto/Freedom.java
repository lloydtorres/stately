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
 * Created by Lloyd on 2016-01-11.
 * A DTO used to store the freedom descriptors of a nation returned by the NationStates API.
 */
@Root(name="FREEDOM", strict=false)
public class Freedom implements Parcelable {

    @Element(name="CIVILRIGHTS")
    public String civilRightsDesc;
    @Element(name="ECONOMY")
    public String economyDesc;
    @Element(name="POLITICALFREEDOM")
    public String politicalDesc;

    public Freedom() {
        super();
    }

    protected Freedom(Parcel in) {
        civilRightsDesc = in.readString();
        economyDesc = in.readString();
        politicalDesc = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(civilRightsDesc);
        dest.writeString(economyDesc);
        dest.writeString(politicalDesc);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Freedom> CREATOR = new Parcelable.Creator<Freedom>() {
        @Override
        public Freedom createFromParcel(Parcel in) {
            return new Freedom(in);
        }

        @Override
        public Freedom[] newArray(int size) {
            return new Freedom[size];
        }
    };
}
