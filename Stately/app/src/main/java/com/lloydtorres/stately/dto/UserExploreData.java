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
 * Created by Lloyd on 2016-10-15.
 * This model holds useful information about a specific user, to be looked up while exploring
 * other nations and regions.
 */
@Root(name = "NATION", strict = false)
public class UserExploreData extends Dossier {
    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api" +
            ".cgi?nation=%s&q="
            + "dossier+rdossier+zombie"
            + "&v=" + SparkleHelper.API_VERSION;
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<UserExploreData> CREATOR =
            new Parcelable.Creator<UserExploreData>() {
                @Override
                public UserExploreData createFromParcel(Parcel in) {
                    return new UserExploreData(in);
                }

                @Override
                public UserExploreData[] newArray(int size) {
                    return new UserExploreData[size];
                }
            };
    @Element(name = "ZOMBIE")
    public Zombie zombieData;

    public UserExploreData() {
        super();
    }

    protected UserExploreData(Parcel in) {
        super(in);
        zombieData = (Zombie) in.readValue(Zombie.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(zombieData);
    }
}
