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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by lloyd on 2017-02-28.
 * Stores a WA delegate and their vote count.
 */
@Root(name="DELEGATE", strict=false)
public class DelegateVote implements Parcelable, Comparable<DelegateVote> {
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

    @Override
    public int compareTo(DelegateVote o) {
        int descendingDiff = o.votes - this.votes;
        if (descendingDiff != 0) {
            return descendingDiff;
        } else {
            return this.delegate.compareTo(o.delegate);
        }
    }
}
