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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-16.
 * This is a DTO to hold the results of a query to the World Assembly API.
 */
@Root(name="WA", strict=false)
public class Assembly extends BaseAssembly implements Parcelable {

    public static final int GENERAL_ASSEMBLY = 1;
    public static final int SECURITY_COUNCIL = 2;
    public static final String QUERY = SparkleHelper.BASE_URI_NOSLASH + "/cgi-bin/api.cgi?wa=%d&q="
                                        + "resolution+votetrack+delvotes"
                                        + "+lastresolution"
                                        + "+numnations+numdelegates+happenings"
                                        + "&v=" + SparkleHelper.API_VERSION;
    public static final String TARGET_GA = SparkleHelper.BASE_URI_NOSLASH + "/page=ga/template-overall=none";
    public static final String TARGET_SC = SparkleHelper.BASE_URI_NOSLASH + "/page=sc/template-overall=none";

    @Element(name="LASTRESOLUTION")
    public String lastResolution;

    @Element(name="NUMNATIONS")
    public int numNations;
    @Element(name="NUMDELEGATES")
    public int numDelegates;
    @ElementList(name="HAPPENINGS")
    public List<Event> events;

    public Assembly() {
        super();
    }

    protected Assembly(Parcel in) {
        super(in);
        lastResolution = in.readString();
        numNations = in.readInt();
        numDelegates = in.readInt();
        if (in.readByte() == 0x01) {
            events = new ArrayList<Event>();
            in.readList(events, Event.class.getClassLoader());
        } else {
            events = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(lastResolution);
        dest.writeInt(numNations);
        dest.writeInt(numDelegates);
        if (events == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(events);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Assembly> CREATOR = new Parcelable.Creator<Assembly>() {
        @Override
        public Assembly createFromParcel(Parcel in) {
            return new Assembly(in);
        }

        @Override
        public Assembly[] newArray(int size) {
            return new Assembly[size];
        }
    };

    public static Assembly parseAssemblyXML(Context c, Persister serializer, String response) throws Exception {
        Assembly assembly = serializer.read(Assembly.class, response);
        return ((Assembly) processRawFields(c, assembly));
    }
}
