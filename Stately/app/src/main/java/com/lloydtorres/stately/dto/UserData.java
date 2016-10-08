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
 * Created by Lloyd on 2016-08-06.
 * This model contains private user data from the NationStates API.
 */
@Root(name="NATION", strict=false)
public class UserData implements Parcelable {

    public static final String UNREAD_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?nation=%s&q=unread"
                                            + "&v=" + SparkleHelper.API_VERSION;

    @Element(name="UNREAD", required=false)
    public UnreadData unread;

    public UserData() { super(); }

    protected UserData(Parcel in) {
        unread = (UnreadData) in.readValue(UnreadData.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(unread);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<UserData> CREATOR = new Parcelable.Creator<UserData>() {
        @Override
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }

        @Override
        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };
}
