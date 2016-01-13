package com.lloydtorres.stately;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-12.
 */
@Root(name="GOVT", strict=false)
public class GovBudget implements Parcelable {

    @Element(name="ADMINISTRATION")
    public double admin;
    @Element(name="DEFENCE")
    public double defense;
    @Element(name="EDUCATION")
    public double education;
    @Element(name="ENVIRONMENT")
    public double environment;
    @Element(name="HEALTHCARE")
    public double healthcare;
    @Element(name="COMMERCE")
    public double industry;
    @Element(name="INTERNATIONALAID")
    public double internationalAid;
    @Element(name="LAWANDORDER")
    public double lawAndOrder;
    @Element(name="PUBLICTRANSPORT")
    public double publicTransport;
    @Element(name="SOCIALEQUALITY")
    public double socialPolicy;
    @Element(name="SPIRITUALITY")
    public double spirituality;
    @Element(name="WELFARE")
    public double welfare;

    public GovBudget() { super(); }

    protected GovBudget(Parcel in) {
        admin = in.readDouble();
        defense = in.readDouble();
        education = in.readDouble();
        environment = in.readDouble();
        healthcare = in.readDouble();
        industry = in.readDouble();
        internationalAid = in.readDouble();
        lawAndOrder = in.readDouble();
        publicTransport = in.readDouble();
        socialPolicy = in.readDouble();
        spirituality = in.readDouble();
        welfare = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(admin);
        dest.writeDouble(defense);
        dest.writeDouble(education);
        dest.writeDouble(environment);
        dest.writeDouble(healthcare);
        dest.writeDouble(industry);
        dest.writeDouble(internationalAid);
        dest.writeDouble(lawAndOrder);
        dest.writeDouble(publicTransport);
        dest.writeDouble(socialPolicy);
        dest.writeDouble(spirituality);
        dest.writeDouble(welfare);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GovBudget> CREATOR = new Parcelable.Creator<GovBudget>() {
        @Override
        public GovBudget createFromParcel(Parcel in) {
            return new GovBudget(in);
        }

        @Override
        public GovBudget[] newArray(int size) {
            return new GovBudget[size];
        }
    };
}
