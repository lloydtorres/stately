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
 * Created by lloyd on 2017-06-04.
 * Helper class for containing issue result headlines.
 */
public class IssueResultHeadlinesContainer implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IssueResultHeadlinesContainer> CREATOR =
            new Parcelable.Creator<IssueResultHeadlinesContainer>() {
        @Override
        public IssueResultHeadlinesContainer createFromParcel(Parcel in) {
            return new IssueResultHeadlinesContainer(in);
        }

        @Override
        public IssueResultHeadlinesContainer[] newArray(int size) {
            return new IssueResultHeadlinesContainer[size];
        }
    };
    public List<String> headlines;

    public IssueResultHeadlinesContainer() {
        super();
    }

    protected IssueResultHeadlinesContainer(Parcel in) {
        if (in.readByte() == 0x01) {
            headlines = new ArrayList<String>();
            in.readList(headlines, String.class.getClassLoader());
        } else {
            headlines = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (headlines == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(headlines);
        }
    }
}
