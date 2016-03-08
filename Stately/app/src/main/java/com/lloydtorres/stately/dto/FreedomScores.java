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
 * A DTO used to store the freedom scores of a nation returned by the NationStates API.
 */
@Root(name="FREEDOMSCORES", strict=false)
public class FreedomScores implements Parcelable {

    @Element(name="CIVILRIGHTS")
    public int civilRightsPts;
    @Element(name="ECONOMY")
    public int economyPts;
    @Element(name="POLITICALFREEDOM")
    public int politicalPts;

    public FreedomScores() {
        super();
    }

    protected FreedomScores(Parcel in) {
        civilRightsPts = in.readInt();
        economyPts = in.readInt();
        politicalPts = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(civilRightsPts);
        dest.writeInt(economyPts);
        dest.writeInt(politicalPts);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FreedomScores> CREATOR = new Parcelable.Creator<FreedomScores>() {
        @Override
        public FreedomScores createFromParcel(Parcel in) {
            return new FreedomScores(in);
        }

        @Override
        public FreedomScores[] newArray(int size) {
            return new FreedomScores[size];
        }
    };
}
