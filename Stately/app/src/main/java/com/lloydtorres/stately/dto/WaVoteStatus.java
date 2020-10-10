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
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-02-02.
 * A DTO used to store data on a nation's WA status (member status and current votes).
 */
@Root(name = "NATION", strict = false)
public class WaVoteStatus implements Parcelable {
    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api" +
            ".cgi?nation=%s&q="
            + "wa+gavote+scvote"
            + "&v=" + SparkleHelper.API_VERSION;

    // Vote statuses
    public static final String VOTE_FOR = "FOR";
    public static final String VOTE_AGAINST = "AGAINST";
    public static final String VOTE_UNDECIDED = "UNDECIDED";
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WaVoteStatus> CREATOR =
            new Parcelable.Creator<WaVoteStatus>() {
                @Override
                public WaVoteStatus createFromParcel(Parcel in) {
                    return new WaVoteStatus(in);
                }

                @Override
                public WaVoteStatus[] newArray(int size) {
                    return new WaVoteStatus[size];
                }
            };
    @Element(name = "UNSTATUS")
    public String waState;
    @Element(name = "GAVOTE", required = false)
    public String gaVote;
    @Element(name = "SCVOTE", required = false)
    public String scVote;

    public WaVoteStatus() {
        super();
    }

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
}
