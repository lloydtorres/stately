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
 * Created by Lloyd on 2016-08-06.
 * This model contains a user's unread counts.
 */
@Root(name="UNREAD", strict=false)
public class UnreadData implements Parcelable {

    @Element(name="ISSUES")
    public int issues;
    @Element(name="TELEGRAMS")
    public int telegrams;
    @Element(name="RMB")
    public int rmb;
    @Element(name="WA")
    public int wa;

    public UnreadData() { super(); }

    protected UnreadData(Parcel in) {
        issues = in.readInt();
        telegrams = in.readInt();
        rmb = in.readInt();
        wa = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(issues);
        dest.writeInt(telegrams);
        dest.writeInt(rmb);
        dest.writeInt(wa);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<UnreadData> CREATOR = new Parcelable.Creator<UnreadData>() {
        @Override
        public UnreadData createFromParcel(Parcel in) {
            return new UnreadData(in);
        }

        @Override
        public UnreadData[] newArray(int size) {
            return new UnreadData[size];
        }
    };
}
