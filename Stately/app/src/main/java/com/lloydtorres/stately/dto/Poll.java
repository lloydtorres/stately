package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-21.
 * A regional poll retrieved from the NationStates API.
 */
@Root(name="POLL", strict=false)
public class Poll implements Parcelable {

    @Element(name="TITLE")
    public String title;
    @Element(name="TEXT", required=false)
    public String text;
    @Element(name="START")
    public long startTime;
    @Element(name="STOP")
    public long stopTime;
    @Element(name="AUTHOR")
    public String author;

    @ElementList(name="OPTIONS")
    public List<PollOption> options;

    public Poll() { super(); }

    protected Poll(Parcel in) {
        title = in.readString();
        text = in.readString();
        startTime = in.readLong();
        stopTime = in.readLong();
        author = in.readString();
        if (in.readByte() == 0x01) {
            options = new ArrayList<PollOption>();
            in.readList(options, PollOption.class.getClassLoader());
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
        dest.writeString(title);
        dest.writeString(text);
        dest.writeLong(startTime);
        dest.writeLong(stopTime);
        dest.writeString(author);
        if (options == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(options);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Poll> CREATOR = new Parcelable.Creator<Poll>() {
        @Override
        public Poll createFromParcel(Parcel in) {
            return new Poll(in);
        }

        @Override
        public Poll[] newArray(int size) {
            return new Poll[size];
        }
    };

}
