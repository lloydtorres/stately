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
 * Convenient model to hold parcelable region tags for Region > Overview.
 */
public class RegionTagsCardData implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RegionTagsCardData> CREATOR =
            new Parcelable.Creator<RegionTagsCardData>() {
                @Override
                public RegionTagsCardData createFromParcel(Parcel in) {
                    return new RegionTagsCardData(in);
                }

                @Override
                public RegionTagsCardData[] newArray(int size) {
                    return new RegionTagsCardData[size];
                }
            };
    public List<String> tags;

    public RegionTagsCardData() {
        super();
    }

    protected RegionTagsCardData(Parcel in) {
        if (in.readByte() == 0x01) {
            tags = new ArrayList<String>();
            in.readList(tags, String.class.getClassLoader());
        } else {
            tags = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (tags == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(tags);
        }
    }
}
