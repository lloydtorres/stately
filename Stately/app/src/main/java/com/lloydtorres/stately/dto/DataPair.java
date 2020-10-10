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
 * Created by Lloyd on 2016-09-12.
 * For when you'd rather use a LinkedHashMap but the model needs to be parcelable.
 */
public class DataPair implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<DataPair> CREATOR = new Parcelable.Creator<DataPair>() {
        @Override
        public DataPair createFromParcel(Parcel in) {
            return new DataPair(in);
        }

        @Override
        public DataPair[] newArray(int size) {
            return new DataPair[size];
        }
    };
    public String key;
    public String value;

    public DataPair() {
        super();
    }

    public DataPair(String k, String v) {
        key = k;
        value = v;
    }

    protected DataPair(Parcel in) {
        key = in.readString();
        value = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(value);
    }

}
