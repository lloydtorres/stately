package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-12.
 */
@Root(name="EVENT", strict=false)
public class HappeningEvent implements Parcelable {

    @Element(name="TIMESTAMP")
    public long timestamp;
    @Element(name="TEXT")
    public String content;

    public HappeningEvent() { super(); }

    protected HappeningEvent(Parcel in) {
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
    public static final Parcelable.Creator<HappeningEvent> CREATOR = new Parcelable.Creator<HappeningEvent>() {
        @Override
        public HappeningEvent createFromParcel(Parcel in) {
            return new HappeningEvent(in);
        }

        @Override
        public HappeningEvent[] newArray(int size) {
            return new HappeningEvent[size];
        }
    };
}
