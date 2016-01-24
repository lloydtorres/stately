package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-17.
 * This is similar to the Assembly DTO, but only contains information on the resolution
 * as well as voting history to minimize the amount of data to be downloaded.
 */
@Root(name="WA", strict=false)
public class AssemblyActive implements Parcelable {
    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?wa=%d&q=resolution+votetrack"
                                            + "&v=" + SparkleHelper.API_VERSION;

    @Element(name="RESOLUTION")
    public Resolution resolution;

    public AssemblyActive() { super(); }

    protected AssemblyActive(Parcel in) {
        resolution = (Resolution) in.readValue(Resolution.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(resolution);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AssemblyActive> CREATOR = new Parcelable.Creator<AssemblyActive>() {
        @Override
        public AssemblyActive createFromParcel(Parcel in) {
            return new AssemblyActive(in);
        }

        @Override
        public AssemblyActive[] newArray(int size) {
            return new AssemblyActive[size];
        }
    };
}
