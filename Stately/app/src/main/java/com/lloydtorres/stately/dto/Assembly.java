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
 * Created by Lloyd on 2016-01-16.
 * This is a DTO to hold the results of a query to the World Assembly API.
 */
@Root(name="WA", strict=false)
public class Assembly implements Parcelable {

    public static final int GENERAL_ASSEMBLY = 1;
    public static final int SECURITY_COUNCIL = 2;
    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?wa=%d&q="
                                        + "resolution+votetrack"
                                        + "+lastresolution"
                                        + "+numnations+numdelegates+happenings"
                                        + "&v=" + SparkleHelper.API_VERSION;
    public static final String TARGET_GA = "https://www.nationstates.net/page=ga/template-overall=none";
    public static final String TARGET_SC = "https://www.nationstates.net/page=sc/template-overall=none";

    @Element(name="RESOLUTION", required=false)
    public Resolution resolution;

    @Element(name="LASTRESOLUTION")
    public String lastResolution;

    @Element(name="NUMNATIONS")
    public int numNations;
    @Element(name="NUMDELEGATES")
    public int numDelegates;
    @Element(name="HAPPENINGS")
    public Happenings happeningsRoot;

    public Assembly() {
        super();
    }

    protected Assembly(Parcel in) {
        resolution = (Resolution) in.readValue(Resolution.class.getClassLoader());
        lastResolution = in.readString();
        numNations = in.readInt();
        numDelegates = in.readInt();
        happeningsRoot = (Happenings) in.readValue(Happenings.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(resolution);
        dest.writeString(lastResolution);
        dest.writeInt(numNations);
        dest.writeInt(numDelegates);
        dest.writeValue(happeningsRoot);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Assembly> CREATOR = new Parcelable.Creator<Assembly>() {
        @Override
        public Assembly createFromParcel(Parcel in) {
            return new Assembly(in);
        }

        @Override
        public Assembly[] newArray(int size) {
            return new Assembly[size];
        }
    };
}
