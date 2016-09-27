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
 * Created by Lloyd on 2016-09-18.
 * A list of notices from a nation from the Notices private API.
 */
@Root(name="NATION", strict=false)
public class NoticeHolder implements Parcelable {
    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?nation=%s&q=notices"
                                        + "&v=" + SparkleHelper.API_VERSION;

    @ElementList(name="NOTICES")
    public List<Notice> notices;

    public NoticeHolder() { super(); }

    protected NoticeHolder(Parcel in) {
        if (in.readByte() == 0x01) {
            notices = new ArrayList<Notice>();
            in.readList(notices, Notice.class.getClassLoader());
        } else {
            notices = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (notices == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(notices);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NoticeHolder> CREATOR = new Parcelable.Creator<NoticeHolder>() {
        @Override
        public NoticeHolder createFromParcel(Parcel in) {
            return new NoticeHolder(in);
        }

        @Override
        public NoticeHolder[] newArray(int size) {
            return new NoticeHolder[size];
        }
    };
}
