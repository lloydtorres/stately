package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Lloyd on 2016-01-26.
 * Defines the information needed to login a user.
 */
public class UserLogin extends SugarRecord implements Parcelable, Comparable<UserLogin> {
    @Unique
    public String nationId;
    public String name;
    public String autologin;

    public UserLogin() { super(); }

    public UserLogin(String i, String n, String a)
    {
        nationId = i;
        name = n;
        autologin = a;
    }

    protected UserLogin(Parcel in) {
        nationId = in.readString();
        name = in.readString();
        autologin = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nationId);
        dest.writeString(name);
        dest.writeString(autologin);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<UserLogin> CREATOR = new Parcelable.Creator<UserLogin>() {
        @Override
        public UserLogin createFromParcel(Parcel in) {
            return new UserLogin(in);
        }

        @Override
        public UserLogin[] newArray(int size) {
            return new UserLogin[size];
        }
    };

    @Override
    public int compareTo(UserLogin another) {
        return this.name.compareTo(another.name);
    }
}
