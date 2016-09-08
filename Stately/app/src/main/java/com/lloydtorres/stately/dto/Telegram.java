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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-03-08.
 * This contains data about a telegram.
 */
public class Telegram implements Parcelable, Comparable<Telegram> {
    public static final String GET_TELEGRAM = "https://www.nationstates.net/page=telegrams/template-overall=none/folder=%s?start=%d";
    public static final String SEND_TELEGRAM = "https://www.nationstates.net/page=telegrams/template-overall=none";
    public static final String MARK_READ = "https://www.nationstates.net/page=ajax3/a=markread/tgid=%d/chk=%s";
    public static final String MOVE_TELEGRAM = "https://www.nationstates.net/page=ajax3/a=tgmove/tgid=%d/dest=%s/chk=%s";
    public static final String DELETE_TELEGRAM = "https://www.nationstates.net/page=ajax3/a=tgdelete/tgid=%d/chk=%s";
    public static final String PERMDELETE_TELEGRAM = "https://www.nationstates.net/page=ajax3/a=tgpermadelete/tgid=%d/chk=%s";
    public static final String TELEGRAM_CONVERSATION = "https://www.nationstates.net/page=tg/template-overall=none/tgid=%d/conversation=1";

    public static final int TELEGRAM_GENERIC = 0;
    public static final int TELEGRAM_RECRUITMENT = 1;
    public static final int TELEGRAM_REGION = 2;
    public static final int TELEGRAM_WELCOME = 3;
    public static final int TELEGRAM_MODERATOR = 4;

    public int id;
    public int type;
    public long timestamp;
    public boolean isUnread;
    public String sender;
    public boolean isNation;
    public List<String> recipients;
    public String preview;
    public String content;
    public String regionTarget;
    public boolean isExpanded;

    public Telegram() { super(); }

    protected Telegram(Parcel in) {
        id = in.readInt();
        type = in.readInt();
        timestamp = in.readLong();
        isUnread = in.readByte() != 0x00;
        sender = in.readString();
        isNation = in.readByte() != 0x00;
        if (in.readByte() == 0x01) {
            recipients = new ArrayList<String>();
            in.readList(recipients, String.class.getClassLoader());
        } else {
            recipients = null;
        }
        preview = in.readString();
        content = in.readString();
        regionTarget = in.readString();
        isExpanded = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeLong(timestamp);
        dest.writeByte((byte) (isUnread ? 0x01 : 0x00));
        dest.writeString(sender);
        dest.writeByte((byte) (isNation ? 0x01 : 0x00));
        if (recipients == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(recipients);
        }
        dest.writeString(preview);
        dest.writeString(content);
        dest.writeString(regionTarget);
        dest.writeByte((byte) (isExpanded ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Telegram> CREATOR = new Parcelable.Creator<Telegram>() {
        @Override
        public Telegram createFromParcel(Parcel in) {
            return new Telegram(in);
        }

        @Override
        public Telegram[] newArray(int size) {
            return new Telegram[size];
        }
    };

    @Override
    public int compareTo(Telegram another) {
        if (this.timestamp > another.timestamp)
        {
            return -1;
        }
        else if (this.timestamp == another.timestamp)
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }
}
