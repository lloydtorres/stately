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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-09-18.
 * Model for a single notice from the Notices private API.
 */
@Root(name="NOTICE", strict=false)
public class Notice implements Parcelable {
    @Element(name="TIMESTAMP", required=false)
    public long timestamp;
    @Element(name="TYPE", required=false)
    public String type;
    @Element(name="TITLE", required=false)
    public String title;
    @Element(name="TEXT", required=false)
    public String content;
    @Element(name="WHO", required=false)
    public String subject;
    @Element(name="URL", required=false)
    public String link;

    public Notice() { super(); }

    protected Notice(Parcel in) {
        timestamp = in.readLong();
        type = in.readString();
        title = in.readString();
        content = in.readString();
        subject = in.readString();
        link = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeString(type);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(subject);
        dest.writeString(link);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Notice> CREATOR = new Parcelable.Creator<Notice>() {
        @Override
        public Notice createFromParcel(Parcel in) {
            return new Notice(in);
        }

        @Override
        public Notice[] newArray(int size) {
            return new Notice[size];
        }
    };
}
