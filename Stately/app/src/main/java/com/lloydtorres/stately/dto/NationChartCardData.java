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
 * A holder for data in chart-based nation cards.
 */
public class NationChartCardData implements Parcelable {
    public static final int MODE_PEOPLE = 0;
    public static final int MODE_GOV = 1;
    public static final int MODE_ECON = 2;
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NationChartCardData> CREATOR =
            new Parcelable.Creator<NationChartCardData>() {
                @Override
                public NationChartCardData createFromParcel(Parcel in) {
                    return new NationChartCardData(in);
                }

                @Override
                public NationChartCardData[] newArray(int size) {
                    return new NationChartCardData[size];
                }
            };
    public int mode;
    public List<DataPair> details = new ArrayList<DataPair>();
    public List<MortalityCause> mortalityList = new ArrayList<MortalityCause>();
    public String animal;
    public GovBudget govBudget;
    public Sectors sectors;

    public NationChartCardData() {
        super();
    }

    protected NationChartCardData(Parcel in) {
        mode = in.readInt();
        if (in.readByte() == 0x01) {
            details = new ArrayList<DataPair>();
            in.readList(details, DataPair.class.getClassLoader());
        } else {
            details = null;
        }
        if (in.readByte() == 0x01) {
            mortalityList = new ArrayList<MortalityCause>();
            in.readList(mortalityList, MortalityCause.class.getClassLoader());
        } else {
            mortalityList = null;
        }
        animal = in.readString();
        govBudget = (GovBudget) in.readValue(GovBudget.class.getClassLoader());
        sectors = (Sectors) in.readValue(Sectors.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mode);
        if (details == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(details);
        }
        if (mortalityList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mortalityList);
        }
        dest.writeString(animal);
        dest.writeValue(govBudget);
        dest.writeValue(sectors);
    }
}
