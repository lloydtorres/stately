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
 * Created by Lloyd on 2016-01-21.
 * Embassy information for a region.
 */
@Root(name="OFFICER", strict=false)
public class Embassy implements Parcelable {

    @Attribute(required=false)
    public String type;
    @Text(required=false)
    public String name;

    public Embassy() { super(); }

    protected Embassy(Parcel in) {
        type = in.readString();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(name);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Embassy> CREATOR = new Parcelable.Creator<Embassy>() {
        @Override
        public Embassy createFromParcel(Parcel in) {
            return new Embassy(in);
        }

        @Override
        public Embassy[] newArray(int size) {
            return new Embassy[size];
        }
    };
}
