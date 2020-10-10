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
 * Created by Lloyd on 2016-10-26.
 * Stores data about superweapon availability during Z-Day.
 */
public class ZSuperweaponStatus implements Parcelable {
    public static final String ZSUPER_TZES = "zsw_tzes";
    public static final String ZSUPER_CURE = "zsw_cure";
    public static final String ZSUPER_HORDE = "zsw_horde";
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ZSuperweaponStatus> CREATOR =
            new Parcelable.Creator<ZSuperweaponStatus>() {
        @Override
        public ZSuperweaponStatus createFromParcel(Parcel in) {
            return new ZSuperweaponStatus(in);
        }

        @Override
        public ZSuperweaponStatus[] newArray(int size) {
            return new ZSuperweaponStatus[size];
        }
    };
    public boolean isTZES;
    public boolean isCure;
    public boolean isHorde;

    public ZSuperweaponStatus() {
        super();
    }

    protected ZSuperweaponStatus(Parcel in) {
        isTZES = in.readByte() != 0x00;
        isCure = in.readByte() != 0x00;
        isHorde = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isTZES ? 0x01 : 0x00));
        dest.writeByte((byte) (isCure ? 0x01 : 0x00));
        dest.writeByte((byte) (isHorde ? 0x01 : 0x00));
    }
}
