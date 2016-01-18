package com.lloydtorres.stately.nation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.HappeningEvent;
import com.lloydtorres.stately.dto.Nation;

import java.util.Collections;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-17.
 */
public class HappeningSubFragment extends Fragment {
    private Nation mNation;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    public void setNation(Nation n)
    {
        mNation = n;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_happenings, container, false);

        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable("mNation");
        }

        if (mNation != null)
        {
            initHappeningsRecycler(view);
        }

        return view;
    }

    private void initHappeningsRecycler(View view)
    {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.happenings_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        List<HappeningEvent> events = mNation.happeningsRoot.events;
        Collections.sort(events);

        mRecyclerAdapter = new HappeningRecyclerAdapter(events);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNation != null)
        {
            outState.putParcelable("mNation", mNation);
        }
    }
}
