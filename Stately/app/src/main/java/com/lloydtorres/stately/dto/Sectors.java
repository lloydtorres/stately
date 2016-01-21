package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-12.
 * A DTO that stores information on a nation's economic sectors, as returned by the
 * NationStates API.
 */
@Root(name="SECTORS", strict=false)
public class Sectors implements Parcelable {

    @Element(name="BLACKMARKET")
    public double blackMarket;
    @Element(name="GOVERNMENT")
    public double government;
    @Element(name="INDUSTRY")
    public double privateSector;
    @Element(name="PUBLIC")
    public double stateOwned;

    public Sectors() { super(); }

    protected Sectors(Parcel in) {
        blackMarket = in.readDouble();
        government = in.readDouble();
        privateSector = in.readDouble();
        stateOwned = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(blackMarket);
        dest.writeDouble(government);
        dest.writeDouble(privateSector);
        dest.writeDouble(stateOwned);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Sectors> CREATOR = new Parcelable.Creator<Sectors>() {
        @Override
        public Sectors createFromParcel(Parcel in) {
            return new Sectors(in);
        }

        @Override
        public Sectors[] newArray(int size) {
            return new Sectors[size];
        }
    };
}
