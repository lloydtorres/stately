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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-28.
 * An object containing all information about one issue encountered in NationStates.
 */
@Root(name = "ISSUE", strict = false)
public class Issue implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Issue> CREATOR = new Parcelable.Creator<Issue>() {
        @Override
        public Issue createFromParcel(Parcel in) {
            return new Issue(in);
        }

        @Override
        public Issue[] newArray(int size) {
            return new Issue[size];
        }
    };
    @Attribute(required = false)
    public int id;
    @Element(name = "TITLE", required = false)
    public String title;
    @Element(name = "CHAIN", required = false)
    public String chain;
    @Element(name = "RECAP", required = false)
    public String recap;
    @Element(name = "TEXT", required = false)
    public String content;
    @Element(name = "PIC1", required = false)
    public String image;
    @ElementList(name = "OPTION", required = false, inline = true)
    public List<IssueOption> options;

    public Issue() {
        super();
    }

    protected Issue(Parcel in) {
        id = in.readInt();
        title = in.readString();
        chain = in.readString();
        recap = in.readString();
        content = in.readString();
        image = in.readString();
        if (in.readByte() == 0x01) {
            options = new ArrayList<IssueOption>();
            in.readList(options, IssueOption.class.getClassLoader());
        } else {
            options = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(chain);
        dest.writeString(recap);
        dest.writeString(content);
        dest.writeString(image);
        if (options == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(options);
        }
    }
}