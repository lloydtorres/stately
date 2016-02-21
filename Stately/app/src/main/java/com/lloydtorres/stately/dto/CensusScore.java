package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by Lloyd on 2016-02-21.
 * Stores information on a given census score from a nation.
 */
@Root(name="CENSUSSCORE", strict=false)
public class CensusScore implements Parcelable {
    @Attribute
    public int id;
    @Text
    public double value;

    public CensusScore() { super(); }

    protected CensusScore(Parcel in) {
        id = in.readInt();
        value = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeDouble(value);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CensusScore> CREATOR = new Parcelable.Creator<CensusScore>() {
        @Override
        public CensusScore createFromParcel(Parcel in) {
            return new CensusScore(in);
        }

        @Override
        public CensusScore[] newArray(int size) {
            return new CensusScore[size];
        }
    };
}
