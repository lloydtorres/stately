package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-28.
 * An object containing all information about one issue encountered in NationStates.
 */
public class Issue implements Parcelable {
    public static final String QUERY = "https://www.nationstates.net/page=dilemmas/template-overall=none";

    public static final int STATUS_UNADDRESSED = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_DISMISSED = 2;

    public int id;
    public String title;
    public int status;
    public String content;
    public List<IssueOption> options;

    public Issue() { super(); }

    protected Issue(Parcel in) {
        id = in.readInt();
        title = in.readString();
        status = in.readInt();
        content = in.readString();
        if (in.readByte() == 0x01) {
            options = new ArrayList<IssueOption>();
            in.readList(options, IssueOption.class.getClassLoader());
        } else {
            options = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeInt(status);
        dest.writeString(content);
        if (options == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(options);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Issue> CREATOR = new Parcelable.Creator<Issue>() {
        @Override
        public Issue createFromParcel(Parcel in) {
            return new Issue(in);
        }

        @Override
        public Issue[] newArray(int size) {
            return new Issue[size];
        }
    };
}