package com.lloydtorres.stately.region;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Event;
import com.lloydtorres.stately.dto.Region;
import com.lloydtorres.stately.helpers.EventRecyclerAdapter;

import java.util.Collections;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-22.
 * A sub-fragment of the Region fragment showing recent happenings as well as major historical
 * events, as returned by the NationStates API. Takes in a Region object.
 */
public class RegionHappeningSubFragment extends Fragment {
    private Region mRegion;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    public void setRegion(Region r)
    {
        mRegion = r;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        // Restore save state
        if (savedInstanceState != null && mRegion == null)
        {
            mRegion = savedInstanceState.getParcelable("mRegion");
        }

        if (mRegion != null)
        {
            initHappeningsRecycler(view);
        }

        return view;
    }

    private void initHappeningsRecycler(View view)
    {
        // Setup recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.happenings_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        List<Event> events = mRegion.happenings;
        events.addAll(mRegion.history);
        Collections.sort(events);

        mRecyclerAdapter = new EventRecyclerAdapter(getContext(), events);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        if (mRegion != null)
        {
            outState.putParcelable("mRegion", mRegion);
        }
    }
}
