package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lloyd on 2016-02-29.
 * An object containing information about a headline from resolving an issue.
 */
public class IssueResultHeadline implements Parcelable {
    public String headline;
    public String imgUrl;

    public IssueResultHeadline() { super(); }

    protected IssueResultHeadline(Parcel in) {
        headline = in.readString();
        imgUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(headline);
        dest.writeString(imgUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IssueResultHeadline> CREATOR = new Parcelable.Creator<IssueResultHeadline>() {
        @Override
        public IssueResultHeadline createFromParcel(Parcel in) {
            return new IssueResultHeadline(in);
        }

        @Override
        public IssueResultHeadline[] newArray(int size) {
            return new IssueResultHeadline[size];
        }
    };
}
