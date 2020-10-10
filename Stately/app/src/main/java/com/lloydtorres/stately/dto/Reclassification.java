/**
 * Copyright 2017 Lloyd Torres
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
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by lloyd on 2017-06-01.
 * Contains data on reclassifications after an issue decision.
 */
@Root(name = "RECLASSIFY", strict = false)
public class Reclassification implements Parcelable {
    public static final String TYPE_GOVERNMENT = "govt";
    public static final String TYPE_CIVILRIGHTS = "0";
    public static final String TYPE_ECONOMY = "1";
    public static final String TYPE_POLITICALFREEDOM = "2";
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Reclassification> CREATOR =
            new Parcelable.Creator<Reclassification>() {
                @Override
                public Reclassification createFromParcel(Parcel in) {
                    return new Reclassification(in);
                }

                @Override
                public Reclassification[] newArray(int size) {
                    return new Reclassification[size];
                }
            };
    @Attribute(name = "type", required = false)
    public String type;
    @Element(name = "FROM", required = false)
    public String from;
    @Element(name = "TO", required = false)
    public String to;

    public Reclassification() {
        super();
    }

    protected Reclassification(Parcel in) {
        type = in.readString();
        from = in.readString();
        to = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(from);
        dest.writeString(to);
    }
}
