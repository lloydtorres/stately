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
 * Created by Lloyd on 2016-09-10.
 * Model for querying nation census rank data by itself.
 */
@Root(strict = false)
public class CensusNationRankData implements Parcelable {
    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?%sq="
            + "censusranks"
            + ";scale=%d"
            + "&start=%d"
            + "&v=" + SparkleHelper.API_VERSION;
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusNationRankData> CREATOR =
            new Parcelable.Creator<CensusNationRankData>() {
                @Override
                public CensusNationRankData createFromParcel(Parcel in) {
                    return new CensusNationRankData(in);
                }

                @Override
                public CensusNationRankData[] newArray(int size) {
                    return new CensusNationRankData[size];
                }
            };
    @Element(name = "CENSUSRANKS", required = false)
    public CensusNationRankList ranks;

    public CensusNationRankData() {
        super();
    }

    protected CensusNationRankData(Parcel in) {
        ranks = (CensusNationRankList) in.readValue(CensusNationRankList.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(ranks);
    }
}
