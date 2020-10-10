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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

/**
 * Created by Lloyd on 2016-08-07.
 * Same as the Nation model, but contains data from private shards as well.
 * Used to get data from a user.
 */
@Root(name = "NATION", strict = false)
public class UserNation extends Nation {

    public static final String QUERY = BASE_QUERY + "+unread"
            + CENSUS_MODIFIER + "&v=" + SparkleHelper.API_VERSION;
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<UserNation> CREATOR =
            new Parcelable.Creator<UserNation>() {
        @Override
        public UserNation createFromParcel(Parcel in) {
            return new UserNation(in);
        }

        @Override
        public UserNation[] newArray(int size) {
            return new UserNation[size];
        }
    };
    @Element(name = "UNREAD", required = false)
    public UnreadData unread;

    public UserNation() {
        super();
    }

    protected UserNation(Parcel in) {
        super(in);
        unread = (UnreadData) in.readValue(UnreadData.class.getClassLoader());
    }

    /**
     * Factory for deserializing a UserNation XML.
     * @param c App context
     * @param serializer SimpleXML deserializer
     * @param response XML response
     * @return UserNation object
     * @throws Exception
     */
    public static UserNation parseNationFromXML(Context c, Persister serializer, String response) throws Exception {
        UserNation nationResponse = serializer.read(UserNation.class, response);
        return ((UserNation) fieldReplacer(c, nationResponse));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeValue(unread);
    }
}
