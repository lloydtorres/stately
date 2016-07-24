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
 * Created by Lloyd on 2016-01-21.
 * An officer in a region.
 */
@Root(name="OFFICER", strict=false)
public class Officer implements Parcelable, Comparable<Officer> {

    // Officers are ordered from smallest to greatest; this numbering puts
    // the delegate and the founder first and second, respectively.
    public static final int DELEGATE_ORDER = -2;
    public static final int FOUNDER_ORDER = -1;

    @Element(name="NATION", required=false)
    public String name;
    @Element(name="OFFICE", required=false)
    public String office;
    @Element(name="ORDER", required=false)
    public int order;

    public Officer() { super(); }

    public Officer(String n, String off, int ord) {
        super();
        name = n;
        office = off;
        order = ord;
    }

    protected Officer(Parcel in) {
        name = in.readString();
        office = in.readString();
        order = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(office);
        dest.writeInt(order);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Officer> CREATOR = new Parcelable.Creator<Officer>() {
        @Override
        public Officer createFromParcel(Parcel in) {
            return new Officer(in);
        }

        @Override
        public Officer[] newArray(int size) {
            return new Officer[size];
        }
    };

    @Override
    public int compareTo(Officer another) {
        return this.order - another.order;
    }
}
