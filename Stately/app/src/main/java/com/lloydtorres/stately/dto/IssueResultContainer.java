/**
 * Copyright 2017 Lloyd Torres
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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by lloyd on 2017-06-01.
 * Contains data about the results of an issue decision.
 */
@Root(name = "NATION", strict = false)
public class IssueResultContainer implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IssueResultContainer> CREATOR =
            new Parcelable.Creator<IssueResultContainer>() {
                @Override
                public IssueResultContainer createFromParcel(Parcel in) {
                    return new IssueResultContainer(in);
                }

                @Override
                public IssueResultContainer[] newArray(int size) {
                    return new IssueResultContainer[size];
                }
            };
    @Element(name = "ISSUE")
    public IssueResult results;

    public IssueResultContainer() {
        super();
    }

    protected IssueResultContainer(Parcel in) {
        results = (IssueResult) in.readValue(IssueResult.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(results);
    }
}
