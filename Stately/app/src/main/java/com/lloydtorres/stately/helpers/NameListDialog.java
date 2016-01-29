package com.lloydtorres.stately.helpers;

import android.os.Build;
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
 * A dialog showing a list of names a nation has.
 */
public class NameListDialog extends DialogFragment {
    public static final String DIALOG_TAG = "fragment_endorsement_dialog";
    public static final String TITLE_KEY = "title";
    public static final String NAMES_KEY = "names";
    public static final String TARGET_KEY = "target";

    // RecyclerView variables
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private ArrayList<String> names;
    private String title;
    private int target;

    public void setTitle(String s)
    {
        title = s;
    }

    public void setNames(ArrayList<String> ends)
    {
        names = ends;
    }

    public void setTarget(int i)
    {
        target = i;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.AlertDialogCustom);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paddedrecycler, container, false);
        getDialog().setTitle(title);
        getDialog().setCanceledOnTouchOutside(true);

        // Restore saved state
        if (savedInstanceState != null)
        {
            title = savedInstanceState.getString(TITLE_KEY);
            names = savedInstanceState.getStringArrayList(NAMES_KEY);
            target = savedInstanceState.getInt(TARGET_KEY);
        }

        initRecycler(view);

        return view;
    }

    private void initRecycler(View view)
    {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_padded);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        Collections.sort(names);
        mRecyclerAdapter = new NameListRecyclerAdapter(getContext(), this, names, target);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putString(TITLE_KEY, title);
        outState.putStringArrayList(NAMES_KEY, names);
        outState.putInt(TARGET_KEY, target);
    }
}
