package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-02-02.
 * A DTO used to store data on a nation's WA status (member status and current votes).
 */
@Root(name="NATION", strict=false)
public class WaVoteStatus implements Parcelable {
    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?nation=%s&q="
                                        + "wa+gavote+scvote"
                                        + "&v=" + SparkleHelper.API_VERSION;

    @Element(name="UNSTATUS")
    public String waState;
    @Element(name="GAVOTE", required=false)
    public String gaVote;
    @Element(name="SCVOTE", required=false)
    public String scVote;

    public WaVoteStatus() { super(); }

    protected WaVoteStatus(Parcel in) {
        waState = in.readString();
        gaVote = in.readString();
        scVote = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(waState);
        dest.writeString(gaVote);
        dest.writeString(scVote);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WaVoteStatus> CREATOR = new Parcelable.Creator<WaVoteStatus>() {
        @Override
        public WaVoteStatus createFromParcel(Parcel in) {
            return new WaVoteStatus(in);
        }

        @Override
        public WaVoteStatus[] newArray(int size) {
            return new WaVoteStatus[size];
        }
    };
}
