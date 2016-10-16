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
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-10-16.
 * Holder for a region's zombie data.
 */
@Root(name="REGION", strict=false)
public class ZombieRegion implements Parcelable {
    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?region=%s&q="
                                        + "name+zombie"
                                        + "&v=" + SparkleHelper.API_VERSION;

    @Element(name="NAME")
    public String name;
    @Element(name="ZOMBIE")
    public Zombie zombieData;

    public ZombieRegion() { super(); }

    protected ZombieRegion(Parcel in) {
        name = in.readString();
        zombieData = (Zombie) in.readValue(Zombie.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeValue(zombieData);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ZombieRegion> CREATOR = new Parcelable.Creator<ZombieRegion>() {
        @Override
        public ZombieRegion createFromParcel(Parcel in) {
            return new ZombieRegion(in);
        }

        @Override
        public ZombieRegion[] newArray(int size) {
            return new ZombieRegion[size];
        }
    };
}
