package com.lloydtorres.stately;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-12.
 */
@Root(name="DEATHS", strict=false)
public class Mortality implements Parcelable {

    @ElementList(inline=true)
    public List<MortalityCause> causes;

    public Mortality() { super(); }

    protected Mortality(Parcel in) {
        if (in.readByte() == 0x01) {
            causes = new ArrayList<MortalityCause>();
            in.readList(causes, MortalityCause.class.getClassLoader());
        } else {
            causes = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (causes == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(causes);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Mortality> CREATOR = new Parcelable.Creator<Mortality>() {
        @Override
        public Mortality createFromParcel(Parcel in) {
            return new Mortality(in);
        }

        @Override
        public Mortality[] newArray(int size) {
            return new Mortality[size];
        }
    };
}
