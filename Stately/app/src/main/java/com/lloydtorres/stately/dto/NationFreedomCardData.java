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
 * Created by Lloyd on 2016-07-24.
 * A holder for freedom score data in the nation overview tab.
 */
public class NationFreedomCardData implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NationFreedomCardData> CREATOR =
            new Parcelable.Creator<NationFreedomCardData>() {
        @Override
        public NationFreedomCardData createFromParcel(Parcel in) {
            return new NationFreedomCardData(in);
        }

        @Override
        public NationFreedomCardData[] newArray(int size) {
            return new NationFreedomCardData[size];
        }
    };
    public String nationTarget;
    public String civDesc;
    public int civScore;
    public String econDesc;
    public int econScore;
    public String poliDesc;
    public int poliScore;

    public NationFreedomCardData() {
        super();
    }

    protected NationFreedomCardData(Parcel in) {
        nationTarget = in.readString();
        civDesc = in.readString();
        civScore = in.readInt();
        econDesc = in.readString();
        econScore = in.readInt();
        poliDesc = in.readString();
        poliScore = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nationTarget);
        dest.writeString(civDesc);
        dest.writeInt(civScore);
        dest.writeString(econDesc);
        dest.writeInt(econScore);
        dest.writeString(poliDesc);
        dest.writeInt(poliScore);
    }

}
