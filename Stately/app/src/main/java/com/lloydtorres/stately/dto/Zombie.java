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

import com.lloydtorres.stately.R;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Locale;

/**
 * Created by Lloyd on 2016-10-15.
 * Contains data about a nation/region's zombie infestation. Used during Z-Day.
 */
@Root(name = "ZOMBIE", strict = false)
public class Zombie implements Parcelable {

    public static final String ZACTION_PARAM_BASE = "zact_%s";
    public static final String ZACTION_MILITARY = "exterminate";
    public static final String ZACTION_CURE = "research";
    public static final String ZACTION_ZOMBIE = "export";
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Zombie> CREATOR = new Parcelable.Creator<Zombie>() {
        @Override
        public Zombie createFromParcel(Parcel in) {
            return new Zombie(in);
        }

        @Override
        public Zombie[] newArray(int size) {
            return new Zombie[size];
        }
    };
    @Element(name = "ZACTION", required = false)
    public String action;
    @Element(name = "SURVIVORS", required = false)
    public int survivors;
    @Element(name = "ZOMBIES", required = false)
    public int zombies;
    @Element(name = "DEAD", required = false)
    public int dead;

    public Zombie() {
        super();
    }

    protected Zombie(Parcel in) {
        action = in.readString();
        survivors = in.readInt();
        zombies = in.readInt();
        dead = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(action);
        dest.writeInt(survivors);
        dest.writeInt(zombies);
        dest.writeInt(dead);
    }

    /**
     * Returns some descriptive text about the current action
     * There's three main cases: military action, cure and join horde
     * For each case, there's different text on whether or not there's survivors left
     * And for the first two, if there are no zombies left
     * @param context App context
     * @param target Name of target nation
     */
    public String getActionDescription(Context context, String target) {
        // Normal actions
        int actionTextId = R.string.zombie_noaction;
        if (action == null) {
            actionTextId = survivors > 0 ? R.string.zombie_noaction : R.string.zombie_noaction_fail;
        } else if (action.equals(ZACTION_MILITARY)) {
            actionTextId = survivors > 0 ? R.string.zombie_military : R.string.zombie_military_fail;
        } else if (action.equals(ZACTION_CURE)) {
            actionTextId = survivors > 0 ? R.string.zombie_cure : R.string.zombie_cure_fail;
        } else if (action.equals(ZACTION_ZOMBIE)) {
            actionTextId = survivors > 0 ? R.string.zombie_join : R.string.zombie_join_done;
        }

        // If everyone's dead
        if (survivors <= 0 && zombies <= 0) {
            actionTextId = R.string.zombie_quiet;
        }

        return String.format(Locale.US, context.getString(actionTextId), target);
    }
}
