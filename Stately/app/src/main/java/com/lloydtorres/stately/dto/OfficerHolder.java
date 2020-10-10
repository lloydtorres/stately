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
 * Created by Lloyd on 2016-09-14.
 * Convenience class used within the CommunityRecyclerAdapter for holding card data about region
 * officers.
 */
public class OfficerHolder implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<OfficerHolder> CREATOR =
            new Parcelable.Creator<OfficerHolder>() {
                @Override
                public OfficerHolder createFromParcel(Parcel in) {
                    return new OfficerHolder(in);
                }

                @Override
                public OfficerHolder[] newArray(int size) {
                    return new OfficerHolder[size];
                }
            };
    public ArrayList<Officer> officers;

    public OfficerHolder(List<Officer> off) {
        officers = new ArrayList<Officer>(off);
    }

    protected OfficerHolder(Parcel in) {
        if (in.readByte() == 0x01) {
            officers = new ArrayList<Officer>();
            in.readList(officers, Officer.class.getClassLoader());
        } else {
            officers = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (officers == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(officers);
        }
    }

}
