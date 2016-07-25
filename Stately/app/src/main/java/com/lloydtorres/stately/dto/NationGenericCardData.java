package com.lloydtorres.stately.dto;

import java.util.LinkedHashMap;

/**
 * Created by Lloyd on 2016-07-24.
 * A holder for a generic data card in the nation fragment.
 */
public class NationGenericCardData {
    public String title;
    public String mainContent;
    public LinkedHashMap<String, String> items;
    public String nationCensusTarget;
    public int idCensusTarget;

    public NationGenericCardData() { super(); }
}
