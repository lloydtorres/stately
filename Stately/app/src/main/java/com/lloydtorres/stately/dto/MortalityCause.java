package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by Lloyd on 2016-01-12.
 * A DTO that stores information on one cause of mortality in a nation in percent, as returned
 * by the NationStates API.
 */
@Root(name="CAUSE", strict=false)
public class MortalityCause implements Parcelable {

    @Attribute
    public String type;
    @Text
    public double value;

    public MortalityCause() { super(); }

    protected MortalityCause(Parcel in) {
        type = in.readString();
        value = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeDouble(value);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MortalityCause> CREATOR = new Parcelable.Creator<MortalityCause>() {
        @Override
        public MortalityCause createFromParcel(Parcel in) {
            return new MortalityCause(in);
        }

        @Override
        public MortalityCause[] newArray(int size) {
            return new MortalityCause[size];
        }
    };
}
