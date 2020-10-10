/**
 * Copyright 2016 Lloyd Torres
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-16.
 * A DTO that stores information on an active WA resolution, as returned by the NationStates API.
 */
@Root(name = "WA", strict = false)
public class Resolution implements Parcelable {

    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?wa=%d&q="
            + "resolution+votetrack+delvotes"
            + "&v=" + SparkleHelper.API_VERSION;
    public static final String QUERY_INACTIVE = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api" +
            ".cgi?wa=%d"
            + "&id=%d&q=resolution"
            + "&v=" + SparkleHelper.API_VERSION;
    public static final String PATH_PROPOSAL = SparkleHelper.BASE_URI_NOSLASH + "/page" +
            "=UN_view_proposal/id=%s";
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Resolution> CREATOR =
            new Parcelable.Creator<Resolution>() {
        @Override
        public Resolution createFromParcel(Parcel in) {
            return new Resolution(in);
        }

        @Override
        public Resolution[] newArray(int size) {
            return new Resolution[size];
        }
    };
    @Element(name = "NAME", required = false)
    public String name;
    @Element(name = "CREATED", required = false)
    public long created;
    @Element(name = "CATEGORY", required = false)
    public String category;
    @Element(name = "OPTION", required = false)
    public String target;
    @Element(name = "PROPOSED_BY", required = false)
    public String proposedBy;
    @Element(name = "DESC", required = false)
    public String content;
    @Element(name = "TOTAL_VOTES_AGAINST", required = false)
    public int votesAgainst;
    @Element(name = "TOTAL_VOTES_FOR", required = false)
    public int votesFor;
    @ElementList(name = "DELVOTES_FOR", required = false)
    public List<DelegateVote> delegateVotesFor;
    @ElementList(name = "DELVOTES_AGAINST", required = false)
    public List<DelegateVote> delegateVotesAgainst;
    @ElementList(name = "VOTE_TRACK_AGAINST", required = false)
    public List<Integer> voteHistoryAgainst;
    @ElementList(name = "VOTE_TRACK_FOR", required = false)
    public List<Integer> voteHistoryFor;
    @Element(name = "COUNCILID", required = false)
    public int id;
    @Element(name = "IMPLEMENTED", required = false)
    public long implemented;
    @Element(name = "REPEALED_BY", required = false)
    public int repealed;
    @Element(name = "REPEALS_COUNCILID", required = false)
    public int repealTarget;

    public Resolution() {
        super();
    }

    protected Resolution(Parcel in) {
        name = in.readString();
        created = in.readLong();
        category = in.readString();
        target = in.readString();
        proposedBy = in.readString();
        content = in.readString();
        votesAgainst = in.readInt();
        votesFor = in.readInt();
        if (in.readByte() == 0x01) {
            delegateVotesFor = new ArrayList<DelegateVote>();
            in.readList(delegateVotesFor, DelegateVote.class.getClassLoader());
        } else {
            delegateVotesFor = null;
        }
        if (in.readByte() == 0x01) {
            delegateVotesAgainst = new ArrayList<DelegateVote>();
            in.readList(delegateVotesAgainst, DelegateVote.class.getClassLoader());
        } else {
            delegateVotesAgainst = null;
        }
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
        id = in.readInt();
        implemented = in.readLong();
        repealed = in.readInt();
        repealTarget = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(created);
        dest.writeString(category);
        dest.writeString(target);
        dest.writeString(proposedBy);
        dest.writeString(content);
        dest.writeInt(votesAgainst);
        dest.writeInt(votesFor);
        if (delegateVotesFor == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(delegateVotesFor);
        }
        if (delegateVotesAgainst == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(delegateVotesAgainst);
        }
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
        dest.writeInt(id);
        dest.writeLong(implemented);
        dest.writeInt(repealed);
        dest.writeInt(repealTarget);
    }
}
