/**
 * Copyright 2016 Lloyd Torres
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    public static final String QUERY_NATION = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?q=happenings;view=nations.%s"
                                                + "&v=" + SparkleHelper.API_VERSION;
    public static final String QUERY_REGION = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?q=happenings;view=regions.%s"
                                                + "&v=" + SparkleHelper.API_VERSION;
    public static final String QUERY_WA = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?wa=1&q=happenings"
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
