package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-21.
 * An officer in a region.
 */
@Root(name="OFFICER", strict=false)
public class Officer implements Parcelable, Comparable<Officer> {

    @Element(name="NATION", required=false)
    public String name;
    @Element(name="OFFICE", required=false)
    public String office;
    @Element(name="ORDER", required=false)
    public int order;

    public Officer() { super(); }

    protected Officer(Parcel in) {
        name = in.readString();
        office = in.readString();
        order = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(office);
        dest.writeInt(order);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Officer> CREATOR = new Parcelable.Creator<Officer>() {
        @Override
        public Officer createFromParcel(Parcel in) {
            return new Officer(in);
        }

        @Override
        public Officer[] newArray(int size) {
            return new Officer[size];
        }
    };

    @Override
    public int compareTo(Officer another) {
        return this.order - another.order;
    }
}
