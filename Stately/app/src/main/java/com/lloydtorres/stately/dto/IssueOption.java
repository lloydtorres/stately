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
 * Created by Lloyd on 2016-01-28.
 * An object containing text for one of the options in an issue.
 */
public class IssueOption implements Parcelable {
    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page=show_dilemma/dilemma=%d/template-overall=none";
    public static final String POST_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page=enact_dilemma/dilemma=%d/template-overall=none";
    public static final String SELECTED_HEADER = "selected";
    public static final String DISMISS_HEADER = "choice--1";

    public int index;
    public String content;
    public String header;

    public IssueOption() { super(); }

    protected IssueOption(Parcel in) {
        index = in.readInt();
        content = in.readString();
        header = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(index);
        dest.writeString(content);
        dest.writeString(header);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IssueOption> CREATOR = new Parcelable.Creator<IssueOption>() {
        @Override
        public IssueOption createFromParcel(Parcel in) {
            return new IssueOption(in);
        }

        @Override
        public IssueOption[] newArray(int size) {
            return new IssueOption[size];
        }
    };
}