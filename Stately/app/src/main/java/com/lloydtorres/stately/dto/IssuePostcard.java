package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lloyd on 2016-02-29.
 * A postcard from the issues result activity.
 */
public class IssuePostcard implements Parcelable {
    public String imgUrl;
    public String title;

    public IssuePostcard() { super(); }

    protected IssuePostcard(Parcel in) {
        imgUrl = in.readString();
        title = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imgUrl);
        dest.writeString(title);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IssuePostcard> CREATOR = new Parcelable.Creator<IssuePostcard>() {
        @Override
        public IssuePostcard createFromParcel(Parcel in) {
            return new IssuePostcard(in);
        }

        @Override
        public IssuePostcard[] newArray(int size) {
            return new IssuePostcard[size];
        }
    };
}
