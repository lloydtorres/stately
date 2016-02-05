package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-02-04.
 */
@Root(name="NATION", strict=false)
public class RegionMemStatus implements Parcelable {
    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?nation=%s&q="
                                        + "region"
                                        + "&v=" + SparkleHelper.API_VERSION;

    @Element(name="REGION")
    public String region;

    public RegionMemStatus() { super(); }

    protected RegionMemStatus(Parcel in) {
        region = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(region);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RegionMemStatus> CREATOR = new Parcelable.Creator<RegionMemStatus>() {
        @Override
        public RegionMemStatus createFromParcel(Parcel in) {
            return new RegionMemStatus(in);
        }

        @Override
        public RegionMemStatus[] newArray(int size) {
            return new RegionMemStatus[size];
        }
    };
}
