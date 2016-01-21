package com.lloydtorres.stately.dto;

/**
 * Created by Lloyd on 2016-01-16.
 * A convenience DTO used to track the number of WA members and delegates in a RecyclerView adapter.
 */
public class AssemblyStats {
    public int members;
    public int delegates;

    public AssemblyStats(int m, int d)
    {
        this.members = m;
        this.delegates = d;
    }
}
