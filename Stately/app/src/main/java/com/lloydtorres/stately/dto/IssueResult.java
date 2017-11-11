/**
 * Copyright 2017 Lloyd Torres
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
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lloyd on 2017-02-11.
 * Contains data from the results of an issue decision.
 */
@Root(name="ISSUE", strict=false)
public class IssueResult implements Parcelable {

    @Element(name="ERROR", required=false)
    public String errorMessage;

    public String image;
    public String issueContent;
    public String issuePosition;

    @Element(name="DESC", required=false)
    public String mainResult;

    @ElementList(name="RECLASSIFICATIONS", required=false)
    public List<Reclassification> reclassifications;
    @ElementList(name="NEW_POLICIES", required=false)
    public List<Policy> enactedPolicies;
    @ElementList(name="REMOVED_POLICIES", required=false)
    public List<Policy> abolishedPolicies;
    @ElementList(name="RANKINGS", required=false)
    public List<CensusDelta> rankings;
    @ElementList(name="HEADLINES", required=false)
    public List<String> headlines;
    @ElementList(name="UNLOCKS", required=false)
    public List<String> postcards;

    public IssueResultHeadlinesContainer niceHeadlines;
    public List<IssuePostcard> nicePostcards;

    public IssueResult() { super(); }

    protected IssueResult(Parcel in) {
        errorMessage = in.readString();
        image = in.readString();
        issueContent = in.readString();
        issuePosition = in.readString();
        mainResult = in.readString();
        if (in.readByte() == 0x01) {
            reclassifications = new ArrayList<Reclassification>();
            in.readList(reclassifications, Reclassification.class.getClassLoader());
        } else {
            reclassifications = null;
        }
        if (in.readByte() == 0x01) {
            enactedPolicies = new ArrayList<Policy>();
            in.readList(enactedPolicies, Policy.class.getClassLoader());
        } else {
            enactedPolicies = null;
        }
        if (in.readByte() == 0x01) {
            abolishedPolicies = new ArrayList<Policy>();
            in.readList(abolishedPolicies, Policy.class.getClassLoader());
        } else {
            abolishedPolicies = null;
        }
        if (in.readByte() == 0x01) {
            rankings = new ArrayList<CensusDelta>();
            in.readList(rankings, CensusDelta.class.getClassLoader());
        } else {
            rankings = null;
        }
        if (in.readByte() == 0x01) {
            headlines = new ArrayList<String>();
            in.readList(headlines, String.class.getClassLoader());
        } else {
            headlines = null;
        }
        if (in.readByte() == 0x01) {
            postcards = new ArrayList<String>();
            in.readList(postcards, String.class.getClassLoader());
        } else {
            postcards = null;
        }
        niceHeadlines = (IssueResultHeadlinesContainer) in.readValue(IssueResultHeadlinesContainer.class.getClassLoader());
        if (in.readByte() == 0x01) {
            nicePostcards = new ArrayList<IssuePostcard>();
            in.readList(nicePostcards, IssuePostcard.class.getClassLoader());
        } else {
            nicePostcards = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(errorMessage);
        dest.writeString(image);
        dest.writeString(issueContent);
        dest.writeString(issuePosition);
        dest.writeString(mainResult);
        if (reclassifications == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(reclassifications);
        }
        if (enactedPolicies == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(enactedPolicies);
        }
        if (abolishedPolicies == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(abolishedPolicies);
        }
        if (rankings == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(rankings);
        }
        if (headlines == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(headlines);
        }
        if (postcards == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(postcards);
        }
        dest.writeValue(niceHeadlines);
        if (nicePostcards == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(nicePostcards);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IssueResult> CREATOR = new Parcelable.Creator<IssueResult>() {
        @Override
        public IssueResult createFromParcel(Parcel in) {
            return new IssueResult(in);
        }

        @Override
        public IssueResult[] newArray(int size) {
            return new IssueResult[size];
        }
    };
}
