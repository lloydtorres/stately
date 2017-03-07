package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lloyd on 2017-02-11.
 * Contains data from the results of an issue decision.
 */
public class IssueResult implements Parcelable {
    public String image;
    public String mainResult;
    public String reclassResults;
    public String issueContent;
    public String issuePosition;

    public IssueResult() { super(); }

    protected IssueResult(Parcel in) {
        image = in.readString();
        mainResult = in.readString();
        reclassResults = in.readString();
        issueContent = in.readString();
        issuePosition = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(image);
        dest.writeString(mainResult);
        dest.writeString(reclassResults);
        dest.writeString(issueContent);
        dest.writeString(issuePosition);
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
