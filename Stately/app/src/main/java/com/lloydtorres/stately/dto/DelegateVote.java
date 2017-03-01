package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by lloyd on 2017-02-28.
 * Stores a WA delegate and their vote count.
 */
@Root(name="DELEGATE", strict=false)
public class DelegateVote implements Parcelable {
    @Element(name="NATION", required=false)
    public String delegate;
    @Element(name="VOTES", required=false)
    public int votes;

    public DelegateVote() { super(); }

    protected DelegateVote(Parcel in) {
        delegate = in.readString();
        votes = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(delegate);
        dest.writeInt(votes);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<DelegateVote> CREATOR = new Parcelable.Creator<DelegateVote>() {
        @Override
        public DelegateVote createFromParcel(Parcel in) {
            return new DelegateVote(in);
        }

        @Override
        public DelegateVote[] newArray(int size) {
            return new DelegateVote[size];
        }
    };
}
