package com.lloydtorres.stately.nation;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Lloyd on 2016-01-19.
 */
public class EndorsementDialog extends DialogFragment {
    public static final String DIALOG_TAG = "fragment_endorsement_dialog";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private ArrayList<String> endorsements;

    public void setEndorsements(ArrayList<String> ends)
    {
        endorsements = ends;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);
        getDialog().setTitle(getString(R.string.card_overview_wa_endorsements));

        if (savedInstanceState != null && endorsements == null)
        {
            endorsements = savedInstanceState.getStringArrayList("endorsements");
        }

        if (endorsements != null)
        {
            initRecycler(view);
        }

        return view;
    }

    private void initRecycler(View view)
    {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.happenings_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setPadding(0, 20, 0, 0);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        Collections.sort(endorsements);
        mRecyclerAdapter = new EndorsementRecyclerAdapter(getContext(), endorsements);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (endorsements != null)
        {
            outState.putStringArrayList("endorsements", endorsements);
        }
    }
}
