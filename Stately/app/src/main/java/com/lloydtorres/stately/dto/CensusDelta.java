package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lloyd on 2016-03-02.
 * This is a model containing data on the change for one census stat after an issue result.
 */
public class CensusDelta implements Parcelable {
    public static final String REGEX_ID = "\\/nation=.*?\\/detail=trend\\?censusid=";

    public int censusId;
    public String delta;
    public boolean isPositive;

    public CensusDelta() { super(); }

    protected CensusDelta(Parcel in) {
        censusId = in.readInt();
        delta = in.readString();
        isPositive = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(censusId);
        dest.writeString(delta);
        dest.writeByte((byte) (isPositive ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusDelta> CREATOR = new Parcelable.Creator<CensusDelta>() {
        @Override
        public CensusDelta createFromParcel(Parcel in) {
            return new CensusDelta(in);
        }

        @Override
        public CensusDelta[] newArray(int size) {
            return new CensusDelta[size];
        }
    };
}
