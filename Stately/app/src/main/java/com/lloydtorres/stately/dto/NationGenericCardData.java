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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-07-24.
 * A holder for a generic data card in the nation fragment.
 */
public class NationGenericCardData implements Parcelable {
    public String title;
    public String mainContent;
    public List<DataPair> items = new ArrayList<DataPair>();
    public String nationCensusTarget;
    public int idCensusTarget;

    public NationGenericCardData() { super(); }

    protected NationGenericCardData(Parcel in) {
        title = in.readString();
        mainContent = in.readString();
        if (in.readByte() == 0x01) {
            items = new ArrayList<DataPair>();
            in.readList(items, DataPair.class.getClassLoader());
        } else {
            items = null;
        }
        nationCensusTarget = in.readString();
        idCensusTarget = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(mainContent);
        if (items == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(items);
        }
        dest.writeString(nationCensusTarget);
        dest.writeInt(idCensusTarget);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NationGenericCardData> CREATOR = new Parcelable.Creator<NationGenericCardData>() {
        @Override
        public NationGenericCardData createFromParcel(Parcel in) {
            return new NationGenericCardData(in);
        }

        @Override
        public NationGenericCardData[] newArray(int size) {
            return new NationGenericCardData[size];
        }
    };
}
