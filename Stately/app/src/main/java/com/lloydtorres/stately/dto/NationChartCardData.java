package com.lloydtorres.stately.dto;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Lloyd on 2016-07-24.
 * A holder for data in chart-based nation cards.
 */
public class NationChartCardData {
    public static final int MODE_PEOPLE = 0;
    public static final int MODE_GOV = 1;
    public static final int MODE_ECON = 2;

    public int mode;
    public LinkedHashMap<String, String> details;
    public List<MortalityCause> mortalityList;
    public GovBudget govBudget;
    public Sectors sectors;

    public NationChartCardData() { super(); }
}
