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
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-21.
 * An option in the regional poll, along with current vote count.
 */
@Root(name = "OPTION", strict = false)
public class PollOption implements Parcelable, Comparable<PollOption> {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PollOption> CREATOR =
            new Parcelable.Creator<PollOption>() {
                @Override
                public PollOption createFromParcel(Parcel in) {
                    return new PollOption(in);
                }

                @Override
                public PollOption[] newArray(int size) {
                    return new PollOption[size];
                }
            };
    @Attribute(required = false)
    public int id;
    @Element(name = "OPTIONTEXT", required = false)
    public String text;
    @Element(name = "VOTES", required = false)
    public int votes;
    @Element(name = "VOTERS", required = false)
    public String voters;

    public PollOption() {
        super();
    }

    protected PollOption(Parcel in) {
        id = in.readInt();
        text = in.readString();
        votes = in.readInt();
        voters = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(text);
        dest.writeInt(votes);
        dest.writeString(voters);
    }

    @Override
    public int compareTo(PollOption another) {
        return this.id - another.id;
    }
}
