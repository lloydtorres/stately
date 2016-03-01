package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lloyd on 2016-01-28.
 * An object containing text for one of the options in an issue.
 */
public class IssueOption implements Parcelable {
    public static final String QUERY = "https://www.nationstates.net/page=show_dilemma/dilemma=%d/template-overall=none";
    public static final String POST_QUERY = "https://www.nationstates.net/page=enact_dilemma/dilemma=%d/template-overall=none";
    public static final String SELECTED_HEADER = "selected";
    public static final String DISMISS_HEADER = "choice--1";

    public int index;
    public String content;
    public String header;

    public IssueOption() { super(); }

    protected IssueOption(Parcel in) {
        index = in.readInt();
        content = in.readString();
        header = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(index);
        dest.writeString(content);
        dest.writeString(header);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IssueOption> CREATOR = new Parcelable.Creator<IssueOption>() {
        @Override
        public IssueOption createFromParcel(Parcel in) {
            return new IssueOption(in);
        }

        @Override
        public IssueOption[] newArray(int size) {
            return new IssueOption[size];
        }
    };
}