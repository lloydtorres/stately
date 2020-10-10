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

/**
 * Created by Lloyd on 2016-10-28.
 * Keeps track of a nation's zombie superweapon research progress.
 * Used for Z-Day.
 */
public class ZSuperweaponProgress implements Parcelable {
    public static final String ZOMBIE_CONTROL_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page" +
            "=zombie_control/template-overall=none";

    public static final String ONE_HUNDRED_PERCENT = "100.0%";
    public static final String TYPE_TZES = "Tactical Zombie Elimination Squads";
    public static final String TYPE_CURE = "Cure Missiles";
    public static final String TYPE_HORDE = "Hordes";
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ZSuperweaponProgress> CREATOR =
            new Parcelable.Creator<ZSuperweaponProgress>() {
                @Override
                public ZSuperweaponProgress createFromParcel(Parcel in) {
                    return new ZSuperweaponProgress(in);
                }

                @Override
                public ZSuperweaponProgress[] newArray(int size) {
                    return new ZSuperweaponProgress[size];
                }
            };
    public String tzesCurrentLevel;
    public String tzesNextLevel;
    public String tzesNextProgress;
    public String cureCurrentLevel;
    public String cureNextLevel;
    public String cureNextProgress;
    public String hordeCurrentLevel;
    public String hordeNextLevel;
    public String hordeNextProgress;

    public ZSuperweaponProgress() {
        super();
    }

    protected ZSuperweaponProgress(Parcel in) {
        tzesCurrentLevel = in.readString();
        tzesNextLevel = in.readString();
        tzesNextProgress = in.readString();
        cureCurrentLevel = in.readString();
        cureNextLevel = in.readString();
        cureNextProgress = in.readString();
        hordeCurrentLevel = in.readString();
        hordeNextLevel = in.readString();
        hordeNextProgress = in.readString();
    }

    public boolean isTzesReady() {
        return tzesCurrentLevel != null;
    }

    public boolean isTzesNextVisible() {
        return tzesNextLevel != null && tzesNextProgress != null;
    }

    public boolean isTzesVisible() {
        return isTzesNextVisible() || isTzesReady();
    }

    public boolean isCureReady() {
        return cureCurrentLevel != null;
    }

    public boolean isCureNextVisible() {
        return cureNextLevel != null && cureNextProgress != null;
    }

    public boolean isCureVisible() {
        return isCureNextVisible() || isCureReady();
    }

    public boolean isHordeReady() {
        return hordeCurrentLevel != null;
    }

    public boolean isHordeNextVisible() {
        return hordeNextProgress != null;
    }

    public boolean isHordeVisible() {
        return isHordeNextVisible() || isHordeReady();
    }

    public boolean isAnySuperweaponReady() {
        return isTzesReady() || isCureReady() || isHordeReady();
    }

    public boolean isAnySuperweaponVisible() {
        return isTzesVisible() || isCureVisible() || isHordeVisible();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tzesCurrentLevel);
        dest.writeString(tzesNextLevel);
        dest.writeString(tzesNextProgress);
        dest.writeString(cureCurrentLevel);
        dest.writeString(cureNextLevel);
        dest.writeString(cureNextProgress);
        dest.writeString(hordeCurrentLevel);
        dest.writeString(hordeNextLevel);
        dest.writeString(hordeNextProgress);
    }
}
