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

import com.lloydtorres.stately.helpers.RaraHelper;

/**
 * Created by Lloyd on 2016-02-29.
 * A postcard from the issues result activity.
 */
public class IssuePostcard implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IssuePostcard> CREATOR =
            new Parcelable.Creator<IssuePostcard>() {
        @Override
        public IssuePostcard createFromParcel(Parcel in) {
            return new IssuePostcard(in);
        }

        @Override
        public IssuePostcard[] newArray(int size) {
            return new IssuePostcard[size];
        }
    };
    public String imgUrl;
    public String rawId;

    public IssuePostcard() {
        super();
    }

    public IssuePostcard(String id) {
        rawId = id;
        imgUrl = RaraHelper.getBannerURL(rawId);
    }

    protected IssuePostcard(Parcel in) {
        imgUrl = in.readString();
        rawId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imgUrl);
        dest.writeString(rawId);
    }
}
