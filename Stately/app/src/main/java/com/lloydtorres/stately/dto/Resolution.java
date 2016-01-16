package com.lloydtorres.stately.dto;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-16.
 */
@Root(name="WA", strict=false)
public class Resolution {

    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?wa=%d&q="
                                        + "resolution+votetrack";

    @Element(name="CATEGORY")
    public String category;
    @Element(name="CREATED")
    public long created;
    @Element(name="DESC")
    public String content;
    @Element(name="NAME")
    public String name;
    @Element(name="OPTION")
    public String target;
    @Element(name="PROPOSED_BY")
    public String proposedBy;
    @Element(name="TOTAL_VOTES_AGAINST")
    public int votesAgainst;
    @Element(name="TOTAL_VOTES_FOR")
    public int votesFor;

    @ElementList(name="VOTE_TRACK_AGAINST")
    public List<Integer> voteHistoryAgainst;
    @ElementList(name="VOTE_TRACK_FOR")
    public List<Integer> voteHistoryFor;
}
