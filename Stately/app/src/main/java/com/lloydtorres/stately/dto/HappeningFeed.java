package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-02-09.
 * This DTO is a generic for holding happenings from any root.
 */
@Root(strict=false)
public class HappeningFeed implements Parcelable {
    public static final String QUERY_NATION = "https://www.nationstates.net/cgi-bin/api.cgi?nation=%s&q=happenings"
                                                + "&v=" + SparkleHelper.API_VERSION;
    public static final String QUERY_REGION = "https://www.nationstates.net/cgi-bin/api.cgi?region=%s&q=happenings"
                                                + "&v=" + SparkleHelper.API_VERSION;
    public static final String QUERY_WA = "https://www.nationstates.net/cgi-bin/api.cgi?wa=1&q=happenings"
                                            + "&v=" + SparkleHelper.API_VERSION;

    @ElementList(name="HAPPENINGS")
    public List<Event> happenings;

    public HappeningFeed() { super(); }

    protected HappeningFeed(Parcel in) {
        if (in.readByte() == 0x01) {
            happenings = new ArrayList<Event>();
            in.readList(happenings, Event.class.getClassLoader());
        } else {
            happenings = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (happenings == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(happenings);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<HappeningFeed> CREATOR = new Parcelable.Creator<HappeningFeed>() {
        @Override
        public HappeningFeed createFromParcel(Parcel in) {
            return new HappeningFeed(in);
        }

        @Override
        public HappeningFeed[] newArray(int size) {
            return new HappeningFeed[size];
        }
    };
}
