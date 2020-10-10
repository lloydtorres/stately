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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by lloyd on 2017-11-10.
 * Model containing data on a nation's policy.
 */
@Root(name = "POLICY", strict = false)
public class Policy implements Parcelable {
    public static final int VIEW_NONE = 0;
    public static final int VIEW_ENACTED = 1;
    public static final int VIEW_ABOLISHED = 2;
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Policy> CREATOR = new Parcelable.Creator<Policy>() {
        @Override
        public Policy createFromParcel(Parcel in) {
            return new Policy(in);
        }

        @Override
        public Policy[] newArray(int size) {
            return new Policy[size];
        }
    };
    @Element(name = "NAME", required = false)
    public String name;
    @Element(name = "DESC", required = false)
    public String description;
    @Element(name = "PIC", required = false)
    public String imageId;
    public int renderType = VIEW_NONE;

    public Policy() {
        super();
    }

    protected Policy(Parcel in) {
        name = in.readString();
        description = in.readString();
        imageId = in.readString();
        renderType = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(imageId);
        dest.writeInt(renderType);
    }
}
