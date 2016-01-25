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

    @Attribute(required=false)
    public int id;

    @Element(name="TIMESTAMP", required=false)
    public long timestamp;
    @Element(name="NATION", required=false)
    public String name;
    @Element(name="MESSAGE", required=false)
    public String message;

    public Post() { super(); }

    protected Post(Parcel in) {
        id = in.readInt();
        timestamp = in.readLong();
        name = in.readString();
        message = in.readString();
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
            return -1;
        }
        else if (this.timestamp == another.timestamp)
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }
}
