package com.lloydtorres.stately.region;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Region;

/**
 * Created by Lloyd on 2016-01-24.
 * This is a subfragment of the Region fragment showing information about the Region's community.
 * Accepts a Region object.
 */
public class RegionCommunitySubFragment extends Fragment {
    public static final String REGION_KEY = "mRegion";

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
            mRegion = savedInstanceState.getParcelable(REGION_KEY);
        }

        if (mRegion != null)
        {
            initCommunityRecycler(view);
        }

        return view;
    }

    private void initCommunityRecycler(View view)
    {
        // Setup recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.happenings_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerAdapter = new CommunityRecyclerAdapter(getContext(), mRegion);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        if (mRegion != null)
        {
            outState.putParcelable(REGION_KEY, mRegion);
        }
    }
}
