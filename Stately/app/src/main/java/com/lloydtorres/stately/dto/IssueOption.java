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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by Lloyd on 2016-01-28.
 * An object containing text for one of the options in an issue.
 */
@Root(name = "OPTION", strict = false)
public class IssueOption implements Parcelable {
    public static final String POST_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi";
    public static final int DISMISS_ISSUE_ID = -1;
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IssueOption> CREATOR =
            new Parcelable.Creator<IssueOption>() {
                @Override
                public IssueOption createFromParcel(Parcel in) {
                    return new IssueOption(in);
                }

                @Override
                public IssueOption[] newArray(int size) {
                    return new IssueOption[size];
                }
            };
    @Attribute(required = false)
    public int id;
    public int index;
    @Text(required = false)
    public String content;

    public IssueOption() {
        super();
    }

    protected IssueOption(Parcel in) {
        id = in.readInt();
        index = in.readInt();
        content = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(index);
        dest.writeString(content);
    }
}