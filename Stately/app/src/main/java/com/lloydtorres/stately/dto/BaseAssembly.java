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
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

/**
 * Created by Lloyd on 2016-01-17.
 * This is similar to the Assembly DTO, but only contains information on the resolution
 * as well as voting history to minimize the amount of data to be downloaded.
 */
@Root(name = "WA", strict = false)
public class BaseAssembly implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<BaseAssembly> CREATOR =
            new Parcelable.Creator<BaseAssembly>() {
        @Override
        public BaseAssembly createFromParcel(Parcel in) {
            return new BaseAssembly(in);
        }

        @Override
        public BaseAssembly[] newArray(int size) {
            return new BaseAssembly[size];
        }
    };
    @Element(name = "RESOLUTION")
    public Resolution resolution;

    public BaseAssembly() {
        super();
    }

    protected BaseAssembly(Parcel in) {
        resolution = (Resolution) in.readValue(Resolution.class.getClassLoader());
    }

    public static BaseAssembly parseAssemblyXML(Context c, Persister serializer, String response) throws Exception {
        BaseAssembly baseAssembly = serializer.read(BaseAssembly.class, response);
        baseAssembly = processRawFields(c, baseAssembly);
        return baseAssembly;
    }

    protected static BaseAssembly processRawFields(Context c, BaseAssembly response) {
        if (response.resolution != null) {
            response.resolution.content = SparkleHelper.transformBBCodeToHtml(c,
                    response.resolution.content);
        }
        return response;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(resolution);
    }
}
