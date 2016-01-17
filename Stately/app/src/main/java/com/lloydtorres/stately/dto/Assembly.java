package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-16.
 */
@Root(name="WA", strict=false)
public class Assembly implements Parcelable {

    public static int GENERAL_ASSEMBLY = 1;
    public static final int SECURITY_COUNCIL = 2;
    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?wa=%d&q="
                                        + "resolution+votetrack"
                                        + "+lastresolution"
                                        + "+numnations+numdelegates+happenings";

    @Element(name="RESOLUTION", required=false)
    public Resolution resolution;

    @Element(name="LASTRESOLUTION")
    public String lastResolution;

    @Element(name="NUMNATIONS")
    public int numNations;
    @Element(name="NUMDELEGATES")
    public int numDelegates;
    @Element(name="HAPPENINGS")
    public Happenings happeningsRoot;

    public Assembly() {
        super();
    }

    protected Assembly(Parcel in) {
        resolution = (Resolution) in.readValue(Resolution.class.getClassLoader());
        lastResolution = in.readString();
        numNations = in.readInt();
        numDelegates = in.readInt();
        happeningsRoot = (Happenings) in.readValue(Happenings.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(resolution);
        dest.writeString(lastResolution);
        dest.writeInt(numNations);
        dest.writeInt(numDelegates);
        dest.writeValue(happeningsRoot);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Assembly> CREATOR = new Parcelable.Creator<Assembly>() {
        @Override
        public Assembly createFromParcel(Parcel in) {
            return new Assembly(in);
        }

        @Override
        public Assembly[] newArray(int size) {
            return new Assembly[size];
        }
    };
}
