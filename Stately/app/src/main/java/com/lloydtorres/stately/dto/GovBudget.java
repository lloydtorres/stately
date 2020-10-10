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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-12.
 * A DTO used to track a nation's government spending for each area in percent, as returned
 * by the NationStates API.
 */
@Root(name = "GOVT", strict = false)
public class GovBudget implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GovBudget> CREATOR =
            new Parcelable.Creator<GovBudget>() {
                @Override
                public GovBudget createFromParcel(Parcel in) {
                    return new GovBudget(in);
                }

                @Override
                public GovBudget[] newArray(int size) {
                    return new GovBudget[size];
                }
            };
    @Element(name = "ADMINISTRATION")
    public float admin;
    @Element(name = "DEFENCE")
    public float defense;
    @Element(name = "EDUCATION")
    public float education;
    @Element(name = "ENVIRONMENT")
    public float environment;
    @Element(name = "HEALTHCARE")
    public float healthcare;
    @Element(name = "COMMERCE")
    public float industry;
    @Element(name = "INTERNATIONALAID")
    public float internationalAid;
    @Element(name = "LAWANDORDER")
    public float lawAndOrder;
    @Element(name = "PUBLICTRANSPORT")
    public float publicTransport;
    @Element(name = "SOCIALEQUALITY")
    public float socialPolicy;
    @Element(name = "SPIRITUALITY")
    public float spirituality;
    @Element(name = "WELFARE")
    public float welfare;

    public GovBudget() {
        super();
    }

    protected GovBudget(Parcel in) {
        admin = in.readFloat();
        defense = in.readFloat();
        education = in.readFloat();
        environment = in.readFloat();
        healthcare = in.readFloat();
        industry = in.readFloat();
        internationalAid = in.readFloat();
        lawAndOrder = in.readFloat();
        publicTransport = in.readFloat();
        socialPolicy = in.readFloat();
        spirituality = in.readFloat();
        welfare = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(admin);
        dest.writeFloat(defense);
        dest.writeFloat(education);
        dest.writeFloat(environment);
        dest.writeFloat(healthcare);
        dest.writeFloat(industry);
        dest.writeFloat(internationalAid);
        dest.writeFloat(lawAndOrder);
        dest.writeFloat(publicTransport);
        dest.writeFloat(socialPolicy);
        dest.writeFloat(spirituality);
        dest.writeFloat(welfare);
    }
}
