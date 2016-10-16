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
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-10-15.
 * Model contains user data used for zombie control.
 */
@Root(name="NATION", strict=false)
public class ZombieControlData implements Parcelable {
    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?nation=%s&q="
                                        + "name+flag+zombie+happenings"
                                        + "&v=" + SparkleHelper.API_VERSION;
    public static final String ZOMBIE_CONTROL = SparkleHelper.BASE_URI_NOSLASH + "/page=zombie_control";

    @Element(name="NAME")
    public String name;
    @Element(name="FLAG")
    public String flagURL;
    @Element(name="ZOMBIE")
    public Zombie zombieData;
    @ElementList(name="HAPPENINGS")
    public List<Event> events;

    public ZombieControlData() { super(); }

    protected ZombieControlData(Parcel in) {
        name = in.readString();
        flagURL = in.readString();
        zombieData = (Zombie) in.readValue(Zombie.class.getClassLoader());
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
        dest.writeString(name);
        dest.writeString(flagURL);
        dest.writeValue(zombieData);
        if (events == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(events);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ZombieControlData> CREATOR = new Parcelable.Creator<ZombieControlData>() {
        @Override
        public ZombieControlData createFromParcel(Parcel in) {
            return new ZombieControlData(in);
        }

        @Override
        public ZombieControlData[] newArray(int size) {
            return new ZombieControlData[size];
        }
    };
}
