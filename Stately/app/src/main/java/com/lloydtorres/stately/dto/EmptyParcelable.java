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

import androidx.annotation.NonNull;

/**
 * This is a dummy class used to store objects with no content into Parcelable collections.
 */
public class EmptyParcelable implements Parcelable {
    public EmptyParcelable() { super(); }
    protected EmptyParcelable(Parcel in) {
    }

    public static final Creator<EmptyParcelable> CREATOR = new Creator<EmptyParcelable>() {
        @Override
        public EmptyParcelable createFromParcel(Parcel in) {
            return new EmptyParcelable(in);
        }

        @Override
        public EmptyParcelable[] newArray(int size) {
            return new EmptyParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // no-op
    }
}
