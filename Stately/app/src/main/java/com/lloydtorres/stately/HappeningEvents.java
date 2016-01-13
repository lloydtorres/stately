package com.lloydtorres.stately;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-12.
 */
@Root(name="EVENT", strict=false)
public class HappeningEvents implements Parcelable {

    @Element(name="TIMESTAMP")
    public long timestamp;
    @Element(name="TEXT")
    public String content;

    public HappeningEvents() { super(); }

    protected HappeningEvents(Parcel in) {
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
    public static final Parcelable.Creator<HappeningEvents> CREATOR = new Parcelable.Creator<HappeningEvents>() {
        @Override
        public HappeningEvents createFromParcel(Parcel in) {
            return new HappeningEvents(in);
        }

        @Override
        public HappeningEvents[] newArray(int size) {
            return new HappeningEvents[size];
        }
    };
}
