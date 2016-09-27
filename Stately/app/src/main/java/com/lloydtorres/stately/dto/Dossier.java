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
 * Created by Lloyd on 2016-09-26.
 * This model stores data about a nation's dossier, both for nations and regions.
 */
@Root(name="NATION", strict=false)
public class Dossier implements Parcelable {
    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?nation=%s&q=dossier+rdossier"
                                        + "&v=" + SparkleHelper.API_VERSION;

    @ElementList(name="DOSSIER")
    public List<String> nations;
    @ElementList(name="RDOSSIER")
    public List<String> regions;

    public Dossier() { super(); }

    protected Dossier(Parcel in) {
        if (in.readByte() == 0x01) {
            nations = new ArrayList<String>();
            in.readList(nations, String.class.getClassLoader());
        } else {
            nations = null;
        }
        if (in.readByte() == 0x01) {
            regions = new ArrayList<String>();
            in.readList(regions, String.class.getClassLoader());
        } else {
            regions = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (nations == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(nations);
        }
        if (regions == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(regions);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Dossier> CREATOR = new Parcelable.Creator<Dossier>() {
        @Override
        public Dossier createFromParcel(Parcel in) {
            return new Dossier(in);
        }

        @Override
        public Dossier[] newArray(int size) {
            return new Dossier[size];
        }
    };

}
