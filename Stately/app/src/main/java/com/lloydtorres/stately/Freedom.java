package com.lloydtorres.stately;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-11.
 */
@Root(name="FREEDOM", strict=false)
public class Freedom implements Parcelable {

    @Element(name="CIVILRIGHTS")
    public String civilRightsDesc;
    @Element(name="ECONOMY")
    public String economyDesc;
    @Element(name="POLITICALFREEDOM")
    public String politicalDesc;

    public Freedom() {
        super();
    }

    protected Freedom(Parcel in) {
        civilRightsDesc = in.readString();
        economyDesc = in.readString();
        politicalDesc = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(civilRightsDesc);
        dest.writeString(economyDesc);
        dest.writeString(politicalDesc);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Freedom> CREATOR = new Parcelable.Creator<Freedom>() {
        @Override
        public Freedom createFromParcel(Parcel in) {
            return new Freedom(in);
        }

        @Override
        public Freedom[] newArray(int size) {
            return new Freedom[size];
        }
    };
}
