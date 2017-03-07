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
 * Created by lloyd on 2017-03-06.
 * Contains data about a nation/region's WA commendation/condemnation/liberation.
 */
@Root(name="WABADGE", strict=false)
public class WaBadge implements Parcelable {
    public static final String TYPE_COMMEND = "commend";
    public static final String TYPE_CONDEMN = "condemn";
    public static final String TYPE_LIBERATE = "liberate";

    @Attribute(required=false)
    public String type;
    @Text(required=false)
    public int scResolution;

    public WaBadge() { super(); }

    protected WaBadge(Parcel in) {
        type = in.readString();
        scResolution = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeInt(scResolution);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WaBadge> CREATOR = new Parcelable.Creator<WaBadge>() {
        @Override
        public WaBadge createFromParcel(Parcel in) {
            return new WaBadge(in);
        }

        @Override
        public WaBadge[] newArray(int size) {
            return new WaBadge[size];
        }
    };
}
