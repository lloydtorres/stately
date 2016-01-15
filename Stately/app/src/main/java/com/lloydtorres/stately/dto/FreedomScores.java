package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-11.
 */
@Root(name="FREEDOMSCORES", strict=false)
public class FreedomScores implements Parcelable {

    @Element(name="CIVILRIGHTS")
    public int civilRightsPts;
    @Element(name="ECONOMY")
    public int economyPts;
    @Element(name="POLITICALFREEDOM")
    public int politicalPts;

    public FreedomScores() {
        super();
    }

    protected FreedomScores(Parcel in) {
        civilRightsPts = in.readInt();
        economyPts = in.readInt();
        politicalPts = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(civilRightsPts);
        dest.writeInt(economyPts);
        dest.writeInt(politicalPts);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FreedomScores> CREATOR = new Parcelable.Creator<FreedomScores>() {
        @Override
        public FreedomScores createFromParcel(Parcel in) {
            return new FreedomScores(in);
        }

        @Override
        public FreedomScores[] newArray(int size) {
            return new FreedomScores[size];
        }
    };
}
