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

/**
 * Created by Lloyd on 2016-09-14.
 * Convenience class used within the CommunityRecyclerAdapter for holding card data about region embassies.
 */
public class EmbassyHolder implements Parcelable {
    public ArrayList<String> embassies;

    public EmbassyHolder(ArrayList<String> emb) {
        embassies = emb;
    }

    protected EmbassyHolder(Parcel in) {
        if (in.readByte() == 0x01) {
            embassies = new ArrayList<String>();
            in.readList(embassies, String.class.getClassLoader());
        } else {
            embassies = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (embassies == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(embassies);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<EmbassyHolder> CREATOR = new Parcelable.Creator<EmbassyHolder>() {
        @Override
        public EmbassyHolder createFromParcel(Parcel in) {
            return new EmbassyHolder(in);
        }

        @Override
        public EmbassyHolder[] newArray(int size) {
            return new EmbassyHolder[size];
        }
    };

}
