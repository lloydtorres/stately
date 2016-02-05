package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-24.
 * This object holds a list of posts from the regional message board.
 */
@Root(name="REGION", strict=false)
public class RegionMessages implements Parcelable {

    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?region=%s&q=messages;offset=%d"
                                            + "&v=" + SparkleHelper.API_VERSION;
    public static final String POST_QUERY = "https://www.nationstates.net/page=lodgermbpost/region=%s";

    @ElementList(name="MESSAGES", required=false)
    public List<Post> posts;

    public RegionMessages() { super(); }

    protected RegionMessages(Parcel in) {
        if (in.readByte() == 0x01) {
            posts = new ArrayList<Post>();
            in.readList(posts, Post.class.getClassLoader());
        } else {
            posts = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (posts == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(posts);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RegionMessages> CREATOR = new Parcelable.Creator<RegionMessages>() {
        @Override
        public RegionMessages createFromParcel(Parcel in) {
            return new RegionMessages(in);
        }

        @Override
        public RegionMessages[] newArray(int size) {
            return new RegionMessages[size];
        }
    };
}
