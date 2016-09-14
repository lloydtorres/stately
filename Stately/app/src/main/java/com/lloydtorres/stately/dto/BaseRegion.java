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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-09-11.
 * Model used to store basic region data. This class is specifically used for world featured
 * region queries.
 */
public class BaseRegion implements Parcelable {

    public static final String BASE_QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?region=%s&q="
                                                + "name+flag+numnations"
                                                + "+delegate+delegatevotes+founder+founded"
                                                + "+factbook+tags"
                                                + "&v=" + SparkleHelper.API_VERSION;

    @Element(name="NAME")
    public String name;
    @Element(name="FLAG", required=false)
    public String flagURL;
    @Element(name="NUMNATIONS")
    public int numNations;

    @Element(name="DELEGATE")
    public String delegate;
    @Element(name="DELEGATEVOTES")
    public int delegateVotes;
    @Element(name="FOUNDER")
    public String founder;
    @Element(name="FOUNDED")
    public String founded;

    @Element(name="FACTBOOK", required=false)
    public String factbook;

    @ElementList(name="TAGS")
    public List<String> tags;

    public BaseRegion() { super(); }

    protected BaseRegion(Parcel in) {
        name = in.readString();
        flagURL = in.readString();
        numNations = in.readInt();
        delegate = in.readString();
        delegateVotes = in.readInt();
        founder = in.readString();
        founded = in.readString();
        factbook = in.readString();
        if (in.readByte() == 0x01) {
            tags = new ArrayList<String>();
            in.readList(tags, String.class.getClassLoader());
        } else {
            tags = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(flagURL);
        dest.writeInt(numNations);
        dest.writeString(delegate);
        dest.writeInt(delegateVotes);
        dest.writeString(founder);
        dest.writeString(founded);
        dest.writeString(factbook);
        if (tags == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(tags);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<BaseRegion> CREATOR = new Parcelable.Creator<BaseRegion>() {
        @Override
        public BaseRegion createFromParcel(Parcel in) {
            return new BaseRegion(in);
        }

        @Override
        public BaseRegion[] newArray(int size) {
            return new BaseRegion[size];
        }
    };

    public static BaseRegion parseRegionXML(Persister serializer, String response) throws Exception {
        BaseRegion regionResponse = serializer.read(BaseRegion.class, response);
        return fieldReplacer(regionResponse);
    }

    protected static BaseRegion fieldReplacer(BaseRegion response) {
        // Switch flag URL to https
        if (response.flagURL != null) {
            response.flagURL = response.flagURL.replace("http://", "https://");
        }
        return response;
    }
}
