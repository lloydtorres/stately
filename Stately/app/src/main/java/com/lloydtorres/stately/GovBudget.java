package com.lloydtorres.stately;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Lloyd on 2016-01-12.
 */
@Root(name="GOVT", strict=false)
public class GovBudget {

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
}
