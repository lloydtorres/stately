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

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-12.
 * A list of HappeningEvent DTOs as returned by the NationStates API. This is needed due to
 * the way the XML response is structured, and its easier to deserialize this way.
 */
@Root(name="HAPPENINGS", strict=false)
public class Happenings implements Parcelable {

    @ElementList(inline=true)
    public List<Event> events;

    public Happenings() { super(); }

    protected Happenings(Parcel in) {
        if (in.readByte() == 0x01) {
            events = new ArrayList<Event>();
            in.readList(events, Event.class.getClassLoader());
        } else {
            events = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (events == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(events);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Happenings> CREATOR = new Parcelable.Creator<Happenings>() {
        @Override
        public Happenings createFromParcel(Parcel in) {
            return new Happenings(in);
        }

        @Override
        public Happenings[] newArray(int size) {
            return new Happenings[size];
        }
    };
}
