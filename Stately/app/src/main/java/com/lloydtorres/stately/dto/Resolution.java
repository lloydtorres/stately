package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-16.
 */
@Root(name="WA", strict=false)
public class Resolution implements Parcelable {

    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?wa=%d&q="
                                        + "resolution+votetrack";

    @Element(name="CATEGORY", required=false)
    public String category;
    @Element(name="CREATED", required=false)
    public long created;
    @Element(name="DESC", required=false)
    public String content;
    @Element(name="NAME", required=false)
    public String name;
    @Element(name="OPTION", required=false)
    public String target;
    @Element(name="PROPOSED_BY", required=false)
    public String proposedBy;
    @Element(name="TOTAL_VOTES_AGAINST", required=false)
    public int votesAgainst;
    @Element(name="TOTAL_VOTES_FOR", required=false)
    public int votesFor;

    @ElementList(name="VOTE_TRACK_AGAINST", required=false)
    public List<Integer> voteHistoryAgainst;
    @ElementList(name="VOTE_TRACK_FOR", required=false)
    public List<Integer> voteHistoryFor;

    public Resolution() {
        super();
    }

    protected Resolution(Parcel in) {
        category = in.readString();
        created = in.readLong();
        content = in.readString();
        name = in.readString();
        target = in.readString();
        proposedBy = in.readString();
        votesAgainst = in.readInt();
        votesFor = in.readInt();
        if (in.readByte() == 0x01) {
            voteHistoryAgainst = new ArrayList<Integer>();
            in.readList(voteHistoryAgainst, Integer.class.getClassLoader());
        } else {
            voteHistoryAgainst = null;
        }
        if (in.readByte() == 0x01) {
            voteHistoryFor = new ArrayList<Integer>();
            in.readList(voteHistoryFor, Integer.class.getClassLoader());
        } else {
            voteHistoryFor = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeLong(created);
        dest.writeString(content);
        dest.writeString(name);
        dest.writeString(target);
        dest.writeString(proposedBy);
        dest.writeInt(votesAgainst);
        dest.writeInt(votesFor);
        if (voteHistoryAgainst == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(voteHistoryAgainst);
        }
        if (voteHistoryFor == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(voteHistoryFor);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Resolution> CREATOR = new Parcelable.Creator<Resolution>() {
        @Override
        public Resolution createFromParcel(Parcel in) {
            return new Resolution(in);
        }

        @Override
        public Resolution[] newArray(int size) {
            return new Resolution[size];
        }
    };
}
