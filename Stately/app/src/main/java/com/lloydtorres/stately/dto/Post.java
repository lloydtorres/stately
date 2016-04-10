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
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-24.
 * This objects holds a single post in the regional message board.
 */
@Root(name="POST", strict=false)
public class Post implements Parcelable, Comparable<Post> {

    public static final int POST_REGULAR = 0;
    public static final int POST_SUPPRESSED = 1;
    public static final int POST_DELETED = 2;
    public static final int POST_BANHAMMERED = 9;

    @Attribute(required=false)
    public int id;

    @Element(name="TIMESTAMP", required=false)
    public long timestamp;
    @Element(name="NATION", required=false)
    public String name;
    @Element(name="MESSAGE", required=false)
    public String message;
    @Element(name="STATUS", required=false)
    public int status;
    @Element(name="SUPPRESSOR", required=false)
    public String suppressor;
    @Element(name="LIKES", required=false)
    public int likes;
    @Element(name="LIKERS", required=false)
    public String likedBy;

    public Post() { super(); }

    protected Post(Parcel in) {
        id = in.readInt();
        timestamp = in.readLong();
        name = in.readString();
        message = in.readString();
        status = in.readInt();
        suppressor = in.readString();
        likes = in.readInt();
        likedBy = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeLong(timestamp);
        dest.writeString(name);
        dest.writeString(message);
        dest.writeInt(status);
        dest.writeString(suppressor);
        dest.writeInt(likes);
        dest.writeString(likedBy);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int compareTo(Post another) {
        if (this.timestamp > another.timestamp)
        {
            return 1;
        }
        else if (this.timestamp == another.timestamp)
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }
}
