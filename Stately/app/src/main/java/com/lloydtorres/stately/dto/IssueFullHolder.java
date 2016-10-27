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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-10-17.
 * This model contains data from the API issues query.
 */
@Root(name="NATION", strict=false)
public class IssueFullHolder implements Parcelable {
    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?nation=%s&q=issues+nextissuetime+zombie"
                                        + "&v=" + SparkleHelper.API_VERSION;
    public static final String CONFIRM_QUERY = SparkleHelper.BASE_URI_NOSLASH + "/page=show_dilemma/dilemma=%d/template-overall=none";

    @ElementList(name="ISSUES", required=false)
    public List<Issue> issues;
    @Element(name="NEXTISSUETIME", required=false)
    public long nextIssueTime;
    @Element(name="ZOMBIE", required=false)
    public Zombie zombieData;

    public IssueFullHolder() { super(); }

    protected IssueFullHolder(Parcel in) {
        if (in.readByte() == 0x01) {
            issues = new ArrayList<Issue>();
            in.readList(issues, Issue.class.getClassLoader());
        } else {
            issues = null;
        }
        nextIssueTime = in.readLong();
        zombieData = (Zombie) in.readValue(Zombie.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (issues == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(issues);
        }
        dest.writeLong(nextIssueTime);
        dest.writeValue(zombieData);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IssueFullHolder> CREATOR = new Parcelable.Creator<IssueFullHolder>() {
        @Override
        public IssueFullHolder createFromParcel(Parcel in) {
            return new IssueFullHolder(in);
        }

        @Override
        public IssueFullHolder[] newArray(int size) {
            return new IssueFullHolder[size];
        }
    };
}
