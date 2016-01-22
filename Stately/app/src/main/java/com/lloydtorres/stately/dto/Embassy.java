package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by Lloyd on 2016-01-21.
 * Embassy information for a region.
 */
@Root(name="OFFICER", strict=false)
public class Embassy implements Parcelable {

    @Attribute(required=false)
    public String type;
    @Text(required=false)
    public String name;

    public Embassy() { super(); }

    protected Embassy(Parcel in) {
        type = in.readString();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(name);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Embassy> CREATOR = new Parcelable.Creator<Embassy>() {
        @Override
        public Embassy createFromParcel(Parcel in) {
            return new Embassy(in);
        }

        @Override
        public Embassy[] newArray(int size) {
            return new Embassy[size];
        }
    };
}
