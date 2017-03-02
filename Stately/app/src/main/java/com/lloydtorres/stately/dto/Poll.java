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

import org.simpleframework.xml.Attribute;
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

    public static final String RESPONSE_VOTE = "Your vote has been lodged";
    public static final String RESPONSE_WITHDRAW = "Your vote has been withdrawn";

    public static final int NO_VOTE = -1;

    @Attribute
    public int id;

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
    public boolean isVotingEnabled;
    public int votedOption;
    public boolean isVoteLoading;

    public Poll() { super(); }

    protected Poll(Parcel in) {
        id = in.readInt();
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
        isVotingEnabled = in.readByte() != 0x00;
        votedOption = in.readInt();
        isVoteLoading = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
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
        dest.writeByte((byte) (isVotingEnabled ? 0x01 : 0x00));
        dest.writeInt(votedOption);
        dest.writeByte((byte) (isVoteLoading ? 0x01 : 0x00));
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
