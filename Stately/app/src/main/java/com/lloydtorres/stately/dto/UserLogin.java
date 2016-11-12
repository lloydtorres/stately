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

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-26.
 * Defines the information needed to login a user.
 */
public class UserLogin extends SugarRecord implements Parcelable, Comparable<UserLogin> {
    @Unique
    public String nationId;
    public String name;
    public String autologin;
    public String pin;

    public UserLogin() { super(); }

    public UserLogin(String i, String n, String a, String p) {
        nationId = i;
        name = n;
        autologin = a;
        pin = p;
    }

    protected UserLogin(Parcel in) {
        nationId = in.readString();
        name = in.readString();
        autologin = in.readString();
        pin = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nationId);
        dest.writeString(name);
        dest.writeString(autologin);
        dest.writeString(pin);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<UserLogin> CREATOR = new Parcelable.Creator<UserLogin>() {
        @Override
        public UserLogin createFromParcel(Parcel in) {
            return new UserLogin(in);
        }

        @Override
        public UserLogin[] newArray(int size) {
            return new UserLogin[size];
        }
    };

    @Override
    public int compareTo(UserLogin another) {
        return this.name.toLowerCase(Locale.US).compareTo(another.name.toLowerCase(Locale.US));
    }
}
