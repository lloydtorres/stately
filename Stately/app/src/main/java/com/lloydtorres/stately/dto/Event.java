package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-12.
 * A DTO used to hold a single happening event as returned by the NationStates API.
 */
@Root(name="EVENT", strict=false)
public class Event implements Parcelable, Comparable<Event> {

    @Element(name="TIMESTAMP")
    public long timestamp;
    @Element(name="TEXT")
    public String content;

    public Event() { super(); }

    protected Event(Parcel in) {
        timestamp = in.readLong();
        content = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeString(content);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @Override
    public int compareTo(Event another) {
        if (this.timestamp > another.timestamp)
        {
            return -1;
        }
        else if (this.timestamp < another.timestamp)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
}
