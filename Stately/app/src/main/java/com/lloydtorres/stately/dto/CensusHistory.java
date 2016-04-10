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

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-04-10.
 * Model for a given census scale's history;
 */
@Root(name="NATION", strict=false)
public class CensusHistory implements Parcelable {

    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?%s=%s&q="
                                        + "name+census"
                                        + ";scale=%d;mode=history"
                                        + ";from=%d&to=%d"
                                        + "&v=" + SparkleHelper.API_VERSION;
    public static final long SIXTY_DAYS_IN_SECONDS = 5184000;
    public static final String NATION_HISTORY = "nation";
    public static final String REGION_HISTORY = "region";

    @Element(name="NAME")
    public String name;
    @Element(name="CENSUS")
    public CensusHistoryScale scale;

    public CensusHistory() { super(); }

    protected CensusHistory(Parcel in) {
        name = in.readString();
        scale = (CensusHistoryScale) in.readValue(CensusHistoryScale.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeValue(scale);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusHistory> CREATOR = new Parcelable.Creator<CensusHistory>() {
        @Override
        public CensusHistory createFromParcel(Parcel in) {
            return new CensusHistory(in);
        }

        @Override
        public CensusHistory[] newArray(int size) {
            return new CensusHistory[size];
        }
    };
}
