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
 * Created by Lloyd on 2016-01-12.
 * A DTO used to hold a single happening event as returned by the NationStates API.
 */
@Root(name = "EVENT", strict = false)
public class Event implements Parcelable, Comparable<Event> {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    @Attribute(required = false)
    public long id;
    @Element(name = "TIMESTAMP")
    public long timestamp;
    @Element(name = "TEXT")
    public String content;

    public Event() {
        super();
    }

    protected Event(Parcel in) {
        id = in.readLong();
        timestamp = in.readLong();
        content = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(timestamp);
        dest.writeString(content);
    }

    @Override
    public int compareTo(Event another) {
        if (this.timestamp > another.timestamp) {
            return -1;
        } else if (this.timestamp < another.timestamp) {
            return 1;
        } else {
            return 0;
        }
    }
}
