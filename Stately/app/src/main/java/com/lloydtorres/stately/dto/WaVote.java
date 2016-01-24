package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-21.
 * An object holding information on a region's WA vote statistics.
 */
@Root(strict=false)
public class WaVote implements Parcelable {

    @Element(required=false)
    public int chamber;
    @Element(name="FOR", required=false)
    public int voteFor;
    @Element(name="AGAINST",required=false)
    public int voteAgainst;

    public WaVote() { super(); }

    protected WaVote(Parcel in) {
        chamber = in.readInt();
        voteFor = in.readInt();
        voteAgainst = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(chamber);
        dest.writeInt(voteFor);
        dest.writeInt(voteAgainst);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WaVote> CREATOR = new Parcelable.Creator<WaVote>() {
        @Override
        public WaVote createFromParcel(Parcel in) {
            return new WaVote(in);
        }

        @Override
        public WaVote[] newArray(int size) {
            return new WaVote[size];
        }
    };
}
