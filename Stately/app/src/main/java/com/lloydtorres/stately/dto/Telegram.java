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
public class Telegram implements Parcelable {
    public static final int TELEGRAM_GENERIC = 0;
    public static final int TELEGRAM_RECRUITMENT = 1;
    public static final int TELEGRAM_REGION = 2;
    public static final int TELEGRAM_MODERATOR = 3;

    public int id;
    public int type;
    public String sender;
    public List<String> nationRecepients;
    public List<String> otherRecepients;
    public String content;

    public Telegram() { super(); }

    protected Telegram(Parcel in) {
        id = in.readInt();
        type = in.readInt();
        sender = in.readString();
        if (in.readByte() == 0x01) {
            nationRecepients = new ArrayList<String>();
            in.readList(nationRecepients, String.class.getClassLoader());
        } else {
            nationRecepients = null;
        }
        if (in.readByte() == 0x01) {
            otherRecepients = new ArrayList<String>();
            in.readList(otherRecepients, String.class.getClassLoader());
        } else {
            otherRecepients = null;
        }
        content = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeString(sender);
        if (nationRecepients == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(nationRecepients);
        }
        if (otherRecepients == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(otherRecepients);
        }
        dest.writeString(content);
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
}
