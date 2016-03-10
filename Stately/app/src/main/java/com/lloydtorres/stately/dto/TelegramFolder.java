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

/**
 * Created by Lloyd on 2016-03-09.
 * Contains data about a telegram folder.
 */
public class TelegramFolder implements Parcelable {
    public String name;
    public String value;

    public TelegramFolder() { super(); }

    protected TelegramFolder(Parcel in) {
        name = in.readString();
        value = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(value);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TelegramFolder> CREATOR = new Parcelable.Creator<TelegramFolder>() {
        @Override
        public TelegramFolder createFromParcel(Parcel in) {
            return new TelegramFolder(in);
        }

        @Override
        public TelegramFolder[] newArray(int size) {
            return new TelegramFolder[size];
        }
    };
}
