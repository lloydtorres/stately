package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-21.
 * An option in the regional poll, along with current vote count.
 */
@Root(name="OPTION", strict=false)
public class PollOption implements Parcelable {

    @Attribute(required=false)
    public int id;

    @Element(name="OPTIONTEXT", required=false)
    public String text;
    @Element(name="VOTES", required=false)
    public int votes;

    public PollOption() { super(); }

    protected PollOption(Parcel in) {
        id = in.readInt();
        text = in.readString();
        votes = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(text);
        dest.writeInt(votes);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PollOption> CREATOR = new Parcelable.Creator<PollOption>() {
        @Override
        public PollOption createFromParcel(Parcel in) {
            return new PollOption(in);
        }

        @Override
        public PollOption[] newArray(int size) {
            return new PollOption[size];
        }
    };
}
